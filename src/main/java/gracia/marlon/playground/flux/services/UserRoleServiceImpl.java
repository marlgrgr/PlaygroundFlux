package gracia.marlon.playground.flux.services;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gracia.marlon.playground.flux.cache.ReactiveCacheable;
import gracia.marlon.playground.flux.dtos.PageDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserRoleDTO;
import gracia.marlon.playground.flux.dtos.UserRoleRow;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.model.Role;
import gracia.marlon.playground.flux.model.UserRole;
import gracia.marlon.playground.flux.model.Users;
import gracia.marlon.playground.flux.repository.RoleRepository;
import gracia.marlon.playground.flux.repository.UserRoleRepository;
import gracia.marlon.playground.flux.repository.UsersRepository;
import gracia.marlon.playground.flux.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

	private final UserRoleRepository userRoleRepository;

	private final UsersRepository usersRepository;

	private final RoleRepository roleRepository;

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	private final CacheService cacheService;

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserRoleService_getUserRoles", key = "#page + '-' + #pageSize")
	public Mono<PagedResponse<UserRoleDTO>> getUserRoles(Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Flux<UserRoleRow> userRoleList = this.userRoleRepository
				.findAllActiveUserRolesWithDetails(pageDTO.getPageSize(), pageDTO.getOffset());
		Mono<Long> totalUserRoleList = this.userRoleRepository.countAllActiveUserRoles();

		return userRoleList.collectList()
				.flatMap(userRole -> totalUserRoleList.map(total -> PageableUtil.getPagedResponse(pageDTO, total,
						userRole.stream().map(this::UserRoleRowToUserRoleDTO).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserRoleService_getUserRolesByUser", key = "#userId + '-' + #page + '-' + #pageSize")
	public Mono<PagedResponse<UserRoleDTO>> getUserRolesByUser(Long userId, Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Flux<UserRoleRow> userRoleList = this.userRoleRepository.findByUserId(userId, pageDTO.getPageSize(),
				pageDTO.getOffset());
		Mono<Long> totalUserRoleList = this.userRoleRepository.countByUserId(userId);

		return userRoleList.collectList()
				.flatMap(userRole -> totalUserRoleList.map(total -> PageableUtil.getPagedResponse(pageDTO, total,
						userRole.stream().map(this::UserRoleRowToUserRoleDTO).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserRoleService_getUserRolesByRole", key = "#roleId + '-' + #page + '-' + #pageSize")
	public Mono<PagedResponse<UserRoleDTO>> getUserRolesByRole(Long roleId, Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Flux<UserRoleRow> userRoleList = this.userRoleRepository.findByRoleId(roleId, pageDTO.getPageSize(),
				pageDTO.getOffset());
		Mono<Long> totalUserRoleList = this.userRoleRepository.countByRoleId(roleId);

		return userRoleList.collectList()
				.flatMap(userRole -> totalUserRoleList.map(total -> PageableUtil.getPagedResponse(pageDTO, total,
						userRole.stream().map(this::UserRoleRowToUserRoleDTO).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_UserRoleService_getAllUserRolesByUser", key = "#userId")
	public Flux<UserRoleDTO> getAllUserRolesByUser(Long userId) {
		Flux<UserRoleRow> userRoleList = this.userRoleRepository.findAllByUserId(userId);

		return userRoleList.map(this::UserRoleRowToUserRoleDTO);
	}

	@Override
	public Mono<Void> createUserRole(UserRoleDTO userRoleDTO) {
		return this.validateUserRoleDTO(userRoleDTO).then(Mono.defer(() -> {
			final Mono<Users> monoUser = this.usersRepository.findById(userRoleDTO.getUser().getId());
			final Mono<Role> monoRole = this.roleRepository.findById(userRoleDTO.getRole().getId());

			return monoUser
					.switchIfEmpty(Mono.error(new RestException("User not found", "AUTH-0001", HttpStatus.NOT_FOUND)))
					.flatMap(user -> {
						return monoRole
								.switchIfEmpty(Mono
										.error(new RestException("Role not found", "AUTH-0011", HttpStatus.NOT_FOUND)))
								.flatMap(role -> {

									Mono<Long> countByUserAndRoleId = this.userRoleRepository.countByUserAndRoleId(
											userRoleDTO.getUser().getId(), userRoleDTO.getRole().getId());

									return countByUserAndRoleId.flatMap(count -> {

										if (count > 0) {
											return Mono.error(new RestException("User-Role already exist", "AUTH-0011",
													HttpStatus.BAD_REQUEST));
										}

										final UserRole userRole = new UserRole(null, user.getId(), role.getId());

										return this.saveUserRole(userRole);
									});
								});
					}).then();
		})).then(evictUserRoleCaches());

	}

	@Override
	@Transactional
	public Mono<Void> deleteUserRole(Long id) {
		return this.userRoleRepository.deleteById(id).then(evictUserRoleCaches());
	}

	private UserRoleDTO UserRoleRowToUserRoleDTO(UserRoleRow UserRoleRow) {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(UserRoleRow.getUserId());
		userDTO.setFullname(UserRoleRow.getUserFullname());

		Boolean userPasswordChangeRequired = UserRoleRow.getUserPasswordChangeRequired();
		userDTO.setPasswordChangeRequired(userPasswordChangeRequired == null ? false : userPasswordChangeRequired);

		userDTO.setUsername(UserRoleRow.getUserUsername());

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(UserRoleRow.getRoleId());
		roleDTO.setRole(UserRoleRow.getRole());

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setId(UserRoleRow.getUserRoleId());
		userRoleDTO.setUser(userDTO);
		userRoleDTO.setRole(roleDTO);

		return userRoleDTO;
	}

	private Mono<Void> validateUserRoleDTO(UserRoleDTO userRoleDTO) {
		if (userRoleDTO == null || userRoleDTO.getRole() == null || userRoleDTO.getRole().getId() == null
				|| userRoleDTO.getUser() == null || userRoleDTO.getUser().getId() == null) {
			return Mono
					.error(new RestException("The user-object sent is not valid", "AUTH-0012", HttpStatus.BAD_REQUEST));
		}

		return Mono.empty();
	}

	@Override
	@Transactional
	public Mono<UserRole> saveUserRole(UserRole userRole) {
		Mono<Long> userRoleSeq = this.dbSequenceService.getNext("user_role_seq");

		return userRoleSeq.flatMap(seq -> {
			userRole.setId(seq);
			return databaseClient.sql("INSERT INTO user_role (id, user_id, role_id) VALUES (:id, :user_id, :role_id)")
					.bind("id", userRole.getId()).bind("user_id", userRole.getUserId())
					.bind("role_id", userRole.getRoleId()).fetch().rowsUpdated().thenReturn(userRole);
		});
	}

	private Mono<Void> evictUserRoleCaches() {
		return Mono.when(this.cacheService.evictCache("Flux_UserRoleService_getUserRoles"),
				this.cacheService.evictCache("Flux_UserRoleService_getUserRolesByUser"),
				this.cacheService.evictCache("Flux_UserRoleService_getUserRolesByRole"),
				this.cacheService.evictCache("Flux_UserRoleService_getAllUserRolesByUser"),
				this.cacheService.evictCache("UserRoleService_getUserRoles"),
				this.cacheService.evictCache("UserRoleService_getUserRolesByUser"),
				this.cacheService.evictCache("UserRoleService_getUserRolesByRole"),
				this.cacheService.evictCache("UserRoleService_getAllUserRolesByUser"));
	}

}
