package gracia.marlon.playground.flux.services;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gracia.marlon.playground.flux.cache.ReactiveCacheable;
import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.PageDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.mapper.UserMapper;
import gracia.marlon.playground.flux.model.Users;
import gracia.marlon.playground.flux.repository.UsersRepository;
import gracia.marlon.playground.flux.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UsersRepository usersRepository;

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	private final CacheService cacheService;

	private final int MIN_USER_LENGTH = 2;

	private final int MAX_USER_LENGTH = 20;

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserService_getUsers", key = "#page + '-' + #pageSize")
	public Mono<PagedResponse<UserDTO>> getUsers(Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Flux<Users> userList = this.usersRepository.findNotRetiredUsers(pageDTO.getPageSize(), pageDTO.getOffset());
		Mono<Long> totalUserList = this.usersRepository.countNotRetiredUsers();

		return userList.collectList().flatMap(user -> totalUserList.map(total -> PageableUtil.getPagedResponse(pageDTO,
				total, user.stream().map(userMapper::toDto).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserService_getUserById", key = "#userId")
	public Mono<UserDTO> getUserById(Long userId) {
		if (userId == null) {
			return Mono.error(new RestException("User not found", "AUTH-0001", HttpStatus.NOT_FOUND));
		}

		final Mono<Users> userOpt = this.usersRepository.findById(userId);

		return userOpt.map(user -> this.userMapper.toDto(user))
				.switchIfEmpty(Mono.error(new RestException("User not found", "AUTH-0001", HttpStatus.NOT_FOUND)));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserService_getUserByUsername", key = "#username")
	public Mono<UserDTO> getUserByUsername(String username) {
		if (username == null) {
			return Mono.error(new RestException("User not found", "AUTH-0001", HttpStatus.NOT_FOUND));
		}

		final Mono<Users> userOpt = this.usersRepository.findByUsername(username.trim().toLowerCase()).next();

		return userOpt.map(user -> this.userMapper.toDto(user))
				.switchIfEmpty(Mono.error(new RestException("User not found", "AUTH-0001", HttpStatus.NOT_FOUND)));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserService_findByUsername", key = "#username")
	public Flux<UserDTO> findByUsername(String username) {
		return this.usersRepository.findByUsername(username).map(userMapper::toDto);
	}

	@Override
	@Transactional
	public Mono<Void> createUser(UserWithPasswordDTO userDto) {
		return this.validateUser(userDto).then(Mono.defer(() -> {

			final Users user = new Users();
			user.setUsername(userDto.getUsername().trim().toLowerCase());
			user.setPassword(passwordEncoder.encode(userDto.getPassword()));
			user.setFullname(userDto.getFullname());
			user.setPasswordChangeRequired(true);

			return this.saveUser(user).then();
		})).then(evictUserCaches());

	}

	@Override
	@Transactional
	public Mono<Void> deleteUser(Long userId) {
		return this.usersRepository.retireById(userId).then(evictUserCaches());
	}

	@Override
	@Transactional
	public Mono<Void> changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
		final Mono<Users> monoUser = this.usersRepository.findById(userId);
		return monoUser.flatMap(user -> {
			user.setPassword(this.passwordEncoder.encode(changePasswordDTO.getNewPassword()));
			user.setPasswordChangeRequired(false);
			return this.usersRepository.save(user);
		}).then(evictUserCaches());
	}

	@Override
	public Mono<Boolean> validUser(AuthRequestDTO request) {
		if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()
				|| request.getPassword() == null || request.getPassword().trim().isEmpty()) {
			return Mono.just(false);
		}

		final Mono<Users> userList = this.usersRepository.findByUsername(request.getUsername()).next();

		return userList.map(user -> {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
				return true;
			}

			return false;
		}).switchIfEmpty(Mono.just(false));
	}

	@Override
	@Transactional
	public Mono<UserDTO> saveUser(Users user) {
		Mono<Long> userSeq = this.dbSequenceService.getNext("users_seq");

		return userSeq.flatMap(seq -> {
			user.setId(seq);

			return databaseClient.sql(
					"INSERT INTO users (id, username, password, fullname, password_change_required, is_retired) VALUES (:id, :username, :password, :fullname, :password_change_required, :is_retired)")
					.bind("id", user.getId()).bind("username", user.getUsername()).bind("password", user.getPassword())
					.bind("fullname", user.getFullname())
					.bind("password_change_required", user.isPasswordChangeRequired())
					.bind("is_retired", user.isRetired()).fetch().rowsUpdated().thenReturn(user).map(userMapper::toDto);
		});
	}

	private Mono<Void> validateUser(UserWithPasswordDTO userDto) {
		if (userDto == null) {
			return Mono.error(new RestException("No user sent", "AUTH-0002", HttpStatus.BAD_REQUEST));
		}

		final int userLength = userDto.getUsername() == null ? 0 : userDto.getUsername().trim().length();

		if (userLength < this.MIN_USER_LENGTH || userLength > this.MAX_USER_LENGTH) {
			return Mono.error(
					new RestException("Username doesn't have required length", "AUTH-0003", HttpStatus.BAD_REQUEST));
		}

		if (userDto.getPassword() == null || userDto.getPassword().trim().length() == 0) {
			return Mono.error(new RestException("Invalid password", "AUTH-0005", HttpStatus.BAD_REQUEST));
		}

		if (userDto.getFullname() == null || userDto.getFullname().trim().length() == 0) {
			return Mono.error(new RestException("Fullname can not be empty", "AUTH-0006", HttpStatus.BAD_REQUEST));
		}

		final Mono<Users> userOpt = this.usersRepository.findByUsername(userDto.getUsername().trim().toLowerCase())
				.next();

		return userOpt.flatMap(
				user -> Mono.error(new RestException("Username already exist", "AUTH-0004", HttpStatus.BAD_REQUEST)))
				.then();

	}

	private Mono<Void> evictUserCaches() {
		return Mono.when(this.cacheService.evictCache("Flux_UserService_getUsers"),
				this.cacheService.evictCache("Flux_UserService_getUserById"),
				this.cacheService.evictCache("Flux_UserService_getUserByUsername"),
				this.cacheService.evictCache("Flux_UserService_findByUsername"),
				this.cacheService.evictCache("UserService_getUsers"),
				this.cacheService.evictCache("UserService_getUserById"),
				this.cacheService.evictCache("UserService_getUserByUsername"));
	}

}
