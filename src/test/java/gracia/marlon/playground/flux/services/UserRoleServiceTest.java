package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.FetchSpec;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserRoleServiceTest {

	private final UserRoleRepository userRoleRepository;

	private final UsersRepository usersRepository;

	private final RoleRepository roleRepository;

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	private final CacheService cacheService;

	private final UserRoleService userRoleService;

	public UserRoleServiceTest() {
		this.userRoleRepository = Mockito.mock(UserRoleRepository.class);
		this.usersRepository = Mockito.mock(UsersRepository.class);
		this.roleRepository = Mockito.mock(RoleRepository.class);
		this.databaseClient = Mockito.mock(DatabaseClient.class);
		this.dbSequenceService = Mockito.mock(DBSequenceService.class);
		this.cacheService = Mockito.mock(CacheService.class);
		this.userRoleService = new UserRoleServiceImpl(this.userRoleRepository, this.usersRepository,
				this.roleRepository, this.databaseClient, this.dbSequenceService, this.cacheService);
	}

	@Test
	public void getUserRolesSuccessful() {

		UserRoleRow userRoleRow = new UserRoleRow();
		userRoleRow.setUserRoleId(1L);
		List<UserRoleRow> listUserRoleRow = new ArrayList<UserRoleRow>();
		listUserRoleRow.add(userRoleRow);
		Flux<UserRoleRow> fluxUserRoleRow = Flux.fromIterable(listUserRoleRow);

		Mockito.when(this.userRoleRepository.findAllActiveUserRolesWithDetails(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(fluxUserRoleRow);
		Mockito.when(this.userRoleRepository.countAllActiveUserRoles()).thenReturn(Mono.just(1L));

		PagedResponse<UserRoleDTO> pagedResponse = this.userRoleService.getUserRoles(1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId().longValue());
	}

	@Test
	public void getUserRolesByUserSuccessful() {

		UserRoleRow userRoleRow = new UserRoleRow();
		userRoleRow.setUserRoleId(1L);
		userRoleRow.setUserPasswordChangeRequired(false);
		List<UserRoleRow> listUserRoleRow = new ArrayList<UserRoleRow>();
		listUserRoleRow.add(userRoleRow);
		Flux<UserRoleRow> fluxUserRoleRow = Flux.fromIterable(listUserRoleRow);

		Mockito.when(this.userRoleRepository.findByUserId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(fluxUserRoleRow);
		Mockito.when(this.userRoleRepository.countByUserId(Mockito.anyLong())).thenReturn(Mono.just(1L));

		PagedResponse<UserRoleDTO> pagedResponse = this.userRoleService.getUserRolesByUser(1L, 1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId().longValue());
	}

	@Test
	public void getUserRolesByRoleSuccessful() {
		UserRoleRow userRoleRow = new UserRoleRow();
		userRoleRow.setUserRoleId(1L);
		userRoleRow.setUserPasswordChangeRequired(true);
		List<UserRoleRow> listUserRoleRow = new ArrayList<UserRoleRow>();
		listUserRoleRow.add(userRoleRow);
		Flux<UserRoleRow> fluxUserRoleRow = Flux.fromIterable(listUserRoleRow);

		Mockito.when(this.userRoleRepository.findByRoleId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(fluxUserRoleRow);
		Mockito.when(this.userRoleRepository.countByRoleId(Mockito.anyLong())).thenReturn(Mono.just(1L));

		PagedResponse<UserRoleDTO> pagedResponse = this.userRoleService.getUserRolesByRole(1L, 1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId().longValue());
	}

	@Test
	public void getAllUserRolesByUserSuccessful() {
		UserRoleRow userRoleRow = new UserRoleRow();
		userRoleRow.setUserRoleId(1L);
		List<UserRoleRow> listUserRoleRow = new ArrayList<UserRoleRow>();
		listUserRoleRow.add(userRoleRow);
		Flux<UserRoleRow> fluxUserRoleRow = Flux.fromIterable(listUserRoleRow);

		Mockito.when(this.userRoleRepository.findAllByUserId(Mockito.anyLong())).thenReturn(fluxUserRoleRow);

		List<UserRoleDTO> userRoleDTOList = this.userRoleService.getAllUserRolesByUser(1L).collectList().block();

		assertEquals(1, userRoleDTOList.size());
		assertEquals(1L, userRoleDTOList.getFirst().getId().longValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createUserRoleSuccessful() {

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setId(1L);
		userRoleDTO.setUser(userDTO);
		userRoleDTO.setRole(roleDTO);

		Users user = new Users();
		Role role = new Role();

		GenericExecuteSpec genericExecuteSpec = Mockito.mock(GenericExecuteSpec.class);

		FetchSpec<Map<String, Object>> fetch = Mockito.mock(FetchSpec.class);

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(user));
		Mockito.when(this.roleRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(role));
		Mockito.when(this.userRoleRepository.countByUserAndRoleId(Mockito.anyLong(), Mockito.anyLong()))
				.thenReturn(Mono.just(0L));
		Mockito.when(dbSequenceService.getNext(Mockito.anyString())).thenReturn(Mono.just(1L));
		Mockito.when(databaseClient.sql(Mockito.anyString())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.bind(Mockito.anyString(), Mockito.any())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.fetch()).thenReturn(fetch);
		Mockito.when(fetch.rowsUpdated()).thenReturn(Mono.just(1L));
		Mockito.when(this.cacheService.evictCache(Mockito.anyString())).thenReturn(Mono.empty());

		this.userRoleService.createUserRole(userRoleDTO).block();

		Mockito.verify(this.cacheService, Mockito.times(8)).evictCache(Mockito.anyString());
	}

	@Test
	public void createUserRoleValidations() {
		RestException restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(null).block());
		assertEquals("AUTH-0012", restException.getError().getCode());

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0012", restException.getError().getCode());

		RoleDTO roleDTO = new RoleDTO();
		userRoleDTO.setRole(roleDTO);
		restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0012", restException.getError().getCode());

		roleDTO.setId(1L);
		userRoleDTO.setRole(roleDTO);
		restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0012", restException.getError().getCode());

		UserDTO userDTO = new UserDTO();
		userRoleDTO.setUser(userDTO);
		restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0012", restException.getError().getCode());
	}

	@Test
	public void createUserRoleEmptyUserRole() {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setId(1L);
		userRoleDTO.setUser(userDTO);
		userRoleDTO.setRole(roleDTO);

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());

		RestException restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0001", restException.getError().getCode());

		Users user = new Users();

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(user));
		Mockito.when(this.roleRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());

		restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0011", restException.getError().getCode());
	}

	@Test
	public void createUserRoleExisting() {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setId(1L);
		userRoleDTO.setUser(userDTO);
		userRoleDTO.setRole(roleDTO);

		Users user = new Users();
		Role role = new Role();

		UserRole userRole = new UserRole();

		List<UserRole> content = new ArrayList<UserRole>();
		content.add(userRole);

		Mockito.when(this.usersRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(user));
		Mockito.when(this.roleRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(role));
		Mockito.when(this.userRoleRepository.countByUserAndRoleId(Mockito.anyLong(), Mockito.anyLong()))
				.thenReturn(Mono.just(1L));

		RestException restException = assertThrows(RestException.class,
				() -> this.userRoleService.createUserRole(userRoleDTO).block());
		assertEquals("AUTH-0011", restException.getError().getCode());
	}

	@Test
	public void deleteUserRoleSuccessful() {
		Mockito.when(this.userRoleRepository.deleteById(Mockito.anyLong())).thenReturn(Mono.empty());
		Mockito.when(this.cacheService.evictCache(Mockito.anyString())).thenReturn(Mono.empty());

		this.userRoleService.deleteUserRole(1L).block();

		Mockito.verify(this.cacheService, Mockito.times(8)).evictCache(Mockito.anyString());
	}
}
