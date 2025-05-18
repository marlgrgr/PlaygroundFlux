package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.FetchSpec;

import gracia.marlon.playground.flux.mapper.RoleMapper;
import gracia.marlon.playground.flux.model.Role;
import gracia.marlon.playground.flux.repository.RoleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RoleServiceTest {

	private final RoleRepository roleRepository;

	private final RoleMapper roleMapper;

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	private final RoleService RoleService;

	public RoleServiceTest() {
		this.roleRepository = Mockito.mock(RoleRepository.class);
		this.roleMapper = Mappers.getMapper(RoleMapper.class);
		this.databaseClient = Mockito.mock(DatabaseClient.class);
		this.dbSequenceService = Mockito.mock(DBSequenceService.class);
		this.RoleService = new RoleServiceImpl(roleRepository, roleMapper, databaseClient, dbSequenceService);
	}

	@Test
	public void getRoles() {
		List<Role> testRole = new ArrayList<Role>();
		Role role1 = new Role(1L, "role1");
		Role role2 = new Role(2L, "role2");
		testRole.add(role1);
		testRole.add(role2);
		Flux<Role> fluxRole = Flux.fromIterable(testRole);

		Mockito.when(roleRepository.findAll()).thenReturn(fluxRole);
		assertEquals(2, RoleService.getRoles().collectList().block().size());
	}

	@Test
	public void findByRole() {
		List<Role> testRole = new ArrayList<Role>();
		Role role1 = new Role(1L, "role1");
		testRole.add(role1);
		Flux<Role> fluxRole = Flux.fromIterable(testRole);

		Mockito.when(roleRepository.findByRole(Mockito.anyString())).thenReturn(fluxRole);
		assertEquals("role1", RoleService.findByRole("role1").block().getRole());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saveRole() {
		Role role = new Role();

		GenericExecuteSpec genericExecuteSpec = Mockito.mock(GenericExecuteSpec.class);

		FetchSpec<Map<String, Object>> fetch = Mockito.mock(FetchSpec.class);

		Mockito.when(dbSequenceService.getNext(Mockito.anyString())).thenReturn(Mono.just(1L));
		Mockito.when(databaseClient.sql(Mockito.anyString())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.bind(Mockito.anyString(), Mockito.any())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.fetch()).thenReturn(fetch);
		Mockito.when(fetch.rowsUpdated()).thenReturn(Mono.just(1L));

		assertEquals(1L, RoleService.saveRole(role).block().getId());
	}

}
