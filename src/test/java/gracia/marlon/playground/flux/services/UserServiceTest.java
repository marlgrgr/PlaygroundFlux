package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.mapper.UserMapper;
import gracia.marlon.playground.flux.model.Users;
import gracia.marlon.playground.flux.repository.UsersRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserServiceTest {

	private final UsersRepository usersRepository;

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	private final CacheService cacheService;

	private final UserService userService;

	public UserServiceTest() {
		this.usersRepository = Mockito.mock(UsersRepository.class);
		this.userMapper = Mockito.mock(UserMapper.class);
		this.passwordEncoder = Mockito.mock(PasswordEncoder.class);
		this.databaseClient = Mockito.mock(DatabaseClient.class);
		this.dbSequenceService = Mockito.mock(DBSequenceService.class);
		this.cacheService = Mockito.mock(CacheService.class);
		this.userService = new UserServiceImpl(this.usersRepository, this.userMapper, this.passwordEncoder,
				this.databaseClient, this.dbSequenceService, this.cacheService);
	}

	@Test
	public void getUsersSuccessful() {
		Users user = new Users();
		user.setId(1L);
		List<Users> content = new ArrayList<Users>();
		content.add(user);
		Flux<Users> fluxUsers = Flux.fromIterable(content);

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		Mockito.when(this.usersRepository.findNotRetiredUsers(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(fluxUsers);
		Mockito.when(this.usersRepository.countNotRetiredUsers()).thenReturn(Mono.just(1L));
		Mockito.when(this.userMapper.toDto(Mockito.any())).thenReturn(userDTO);

		PagedResponse<UserDTO> pagedResponse = this.userService.getUsers(1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId().longValue());
	}

	@Test
	public void getUserByIdSuccessful() {
		Users user = new Users();
		user.setId(1L);

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(user));
		Mockito.when(this.userMapper.toDto(Mockito.any())).thenReturn(userDTO);

		UserDTO response = this.userService.getUserById(1L).block();

		assertEquals(1L, response.getId().longValue());
	}

	@Test
	public void getUserByIdNotExisting() {
		RestException restException = assertThrows(RestException.class,
				() -> this.userService.getUserById(null).block());
		assertEquals("AUTH-0001", restException.getError().getCode());

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());

		restException = assertThrows(RestException.class, () -> this.userService.getUserById(1L).block());
		assertEquals("AUTH-0001", restException.getError().getCode());
	}

	@Test
	public void getUserByUsernameSuccessful() {
		Users user = new Users();
		user.setId(1L);
		List<Users> userList = new ArrayList<Users>();
		userList.add(user);
		Flux<Users> fluxUsers = Flux.fromIterable(userList);

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUsers);
		Mockito.when(this.userMapper.toDto(Mockito.any())).thenReturn(userDTO);

		UserDTO response = this.userService.getUserByUsername("user").block();

		assertEquals(1L, response.getId().longValue());
	}

	@Test
	public void getUserByUsernameNotExisting() {
		RestException restException = assertThrows(RestException.class,
				() -> this.userService.getUserByUsername(null).block());
		assertEquals("AUTH-0001", restException.getError().getCode());

		List<Users> userList = new ArrayList<Users>();
		Flux<Users> fluxUser = Flux.fromIterable(userList);

		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);

		restException = assertThrows(RestException.class, () -> this.userService.getUserByUsername("user").block());
		assertEquals("AUTH-0001", restException.getError().getCode());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createUserSuccessful() {
		UserWithPasswordDTO userWithPasswordDTO = new UserWithPasswordDTO();
		userWithPasswordDTO.setUsername("user");
		userWithPasswordDTO.setPassword("pass");
		userWithPasswordDTO.setFullname("user");

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);
		
		List<Users> userList = new ArrayList<Users>();
		Flux<Users> fluxUser = Flux.fromIterable(userList);
		
		GenericExecuteSpec genericExecuteSpec = Mockito.mock(GenericExecuteSpec.class);
		FetchSpec<Map<String, Object>> fetch = Mockito.mock(FetchSpec.class);

		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);
		Mockito.when(this.passwordEncoder.encode(Mockito.anyString())).thenReturn("passEncode");
		Mockito.when(dbSequenceService.getNext(Mockito.anyString())).thenReturn(Mono.just(1L));
		Mockito.when(databaseClient.sql(Mockito.anyString())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.bind(Mockito.anyString(), Mockito.any())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.fetch()).thenReturn(fetch);
		Mockito.when(fetch.rowsUpdated()).thenReturn(Mono.just(1L));
		Mockito.when(this.userMapper.toDto(Mockito.any())).thenReturn(userDTO);
		Mockito.when(this.cacheService.evictCache(Mockito.anyString())).thenReturn(Mono.empty());

		this.userService.createUser(userWithPasswordDTO).block();

		Mockito.verify(this.cacheService, times(7)).evictCache(Mockito.anyString());

	}

	@Test
	public void createUserValidations() {
		RestException restException = assertThrows(RestException.class,
				() -> this.userService.createUser(null).block());
		assertEquals("AUTH-0002", restException.getError().getCode());

		UserWithPasswordDTO userWithPasswordDTO = new UserWithPasswordDTO();
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0003", restException.getError().getCode());

		userWithPasswordDTO.setUsername("averylongusernamethatdoesntpassthemaxamountofcharvalidations");
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0003", restException.getError().getCode());

		userWithPasswordDTO.setUsername("user");
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0005", restException.getError().getCode());

		userWithPasswordDTO.setPassword("");
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0005", restException.getError().getCode());

		userWithPasswordDTO.setPassword("pass");
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0006", restException.getError().getCode());

		userWithPasswordDTO.setFullname("");
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0006", restException.getError().getCode());
		
		userWithPasswordDTO.setFullname("fullname");
		Users user = new Users();
		user.setId(1L);
		List<Users> userList = new ArrayList<Users>();
		userList.add(user);
		Flux<Users> fluxUser = Flux.fromIterable(userList);
		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);
		restException = assertThrows(RestException.class,
				() -> this.userService.createUser(userWithPasswordDTO).block());
		assertEquals("AUTH-0004", restException.getError().getCode());
	}

	@Test
	public void deleteUserSuccessful() {
		Mockito.when(this.usersRepository.retireById(Mockito.anyLong())).thenReturn(Mono.just(0));
		Mockito.when(this.cacheService.evictCache(Mockito.anyString())).thenReturn(Mono.empty());

		this.userService.deleteUser(1L).block();

		Mockito.verify(this.cacheService, times(7)).evictCache(Mockito.anyString());
	}

	@Test
	public void changePasswordSuccessful() {
		Users user = new Users();
		user.setId(1L);

		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setNewPassword("newPass");

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(user));
		Mockito.when(this.passwordEncoder.encode(Mockito.anyString())).thenReturn("passEncode");
		Mockito.when(this.usersRepository.save(Mockito.any())).thenReturn(Mono.empty());
		Mockito.when(this.cacheService.evictCache(Mockito.anyString())).thenReturn(Mono.empty());

		this.userService.changePassword(1L, changePasswordDTO).block();

		Mockito.verify(this.cacheService, times(7)).evictCache(Mockito.anyString());
	}

	@Test
	public void validUserSuccessful() {
		AuthRequestDTO request = new AuthRequestDTO();
		request.setUsername("user");
		request.setPassword("pass");

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String passEncode = passwordEncoder.encode("pass");

		Users user = new Users();
		user.setId(1L);
		user.setPassword(passEncode);
		List<Users> userList = new ArrayList<Users>();
		userList.add(user);
		Flux<Users> fluxUser = Flux.fromIterable(userList);

		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);

		assertEquals(true, this.userService.validUser(request).block());
	}

	@Test
	public void validUserValidations() {
		assertEquals(false, this.userService.validUser(null).block());

		AuthRequestDTO request = new AuthRequestDTO();
		assertEquals(false, this.userService.validUser(request).block());

		request.setUsername("");
		assertEquals(false, this.userService.validUser(request).block());

		request.setUsername("user");
		assertEquals(false, this.userService.validUser(request).block());

		request.setPassword("");
		assertEquals(false, this.userService.validUser(request).block());

		request.setPassword("pass");
		List<Users> userList = new ArrayList<Users>();
		Flux<Users> fluxUser = Flux.fromIterable(userList);
		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);
		assertEquals(false, this.userService.validUser(request).block());

		Users user = new Users();
		user.setId(1L);
		user.setPassword("passwordEncodedThatDoesntMatch");
		userList = new ArrayList<Users>();
		userList.add(user);
		fluxUser = Flux.fromIterable(userList);
		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);
		assertEquals(false, this.userService.validUser(request).block());
	}
	
	@Test
	public void findByUsernameSuccessful() {
		Users user = new Users();
		user.setId(1L);

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		List<Users> userList = new ArrayList<Users>();
		userList.add(user);
		Flux<Users> fluxUser = Flux.fromIterable(userList);
		Mockito.when(this.usersRepository.findByUsername(Mockito.anyString())).thenReturn(fluxUser);
		Mockito.when(this.userMapper.toDto(Mockito.any())).thenReturn(userDTO);

		List<UserDTO> response = this.userService.findByUsername("user").collectList().block();

		assertEquals(1L, response.getFirst().getId().longValue());
	}
}
