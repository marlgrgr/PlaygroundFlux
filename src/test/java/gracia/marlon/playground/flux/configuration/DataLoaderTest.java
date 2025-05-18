package gracia.marlon.playground.flux.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.env.Environment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.security.crypto.password.PasswordEncoder;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.services.RoleService;
import gracia.marlon.playground.flux.services.UserRoleService;
import gracia.marlon.playground.flux.services.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DataLoaderTest {

	private final DatabaseClient databaseClient;

	private final UserService userService;

	private final RoleService roleService;

	private final UserRoleService userRoleService;

	private final PasswordEncoder passwordEncoder;

	private final RedissonReactiveClient redisson;

	private final Environment env;

	private final DataLoader dataLoader;

	public DataLoaderTest() {
		this.databaseClient = Mockito.mock(DatabaseClient.class);
		this.userService = Mockito.mock(UserService.class);
		this.roleService = Mockito.mock(RoleService.class);
		this.userRoleService = Mockito.mock(UserRoleService.class);
		this.passwordEncoder = Mockito.mock(PasswordEncoder.class);
		this.redisson = Mockito.mock(RedissonReactiveClient.class);
		this.env = Mockito.mock(Environment.class);
		Mockito.when(this.env.getProperty(Mockito.eq("admin.default.user"), Mockito.anyString())).thenReturn("admin");
		Mockito.when(this.env.getProperty(Mockito.eq("admin.default.password"), Mockito.anyString()))
				.thenReturn("admin123");
		this.dataLoader = new DataLoader(this.databaseClient, this.userService, this.roleService, this.userRoleService,
				this.passwordEncoder, this.redisson, this.env);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void runSuccessful() throws Exception {

		GenericExecuteSpec genericExecuteSpec = Mockito.mock(GenericExecuteSpec.class);
		FetchSpec<Map<String, Object>> fetch = Mockito.mock(FetchSpec.class);
		Map<String, Object> row = new HashMap<String, Object>();
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);

		UserDTO userDTO = new UserDTO();
		userDTO.setId(1L);

		List<UserDTO> userDTOList = new ArrayList<UserDTO>();
		userDTOList.add(userDTO);
		Flux<UserDTO> fluxUserDTO = Flux.fromIterable(userDTOList);

		Mockito.when(this.redisson.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.just(true));
		Mockito.when(databaseClient.sql(Mockito.anyString())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.fetch()).thenReturn(fetch);
		Mockito.when(fetch.first()).thenReturn(Mono.error(new RuntimeException())).thenReturn(Mono.just(row));
		Mockito.when(this.roleService.findByRole(Mockito.anyString())).thenReturn(Mono.just(roleDTO));
		Mockito.when(this.userService.findByUsername(Mockito.anyString())).thenReturn(fluxUserDTO);

		this.dataLoader.run("");

		Mockito.verify(this.userRoleService, Mockito.never()).saveUserRole(Mockito.any());
	}

	@Test
	public void runNoLocked() throws Exception {
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		Mockito.when(this.redisson.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.just(false));

		this.dataLoader.run("");

		Mockito.verify(this.userRoleService, Mockito.never()).saveUserRole(Mockito.any());
	}

	@Test
	public void runLockException() throws Exception {
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		Mockito.when(this.redisson.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.error(new RuntimeException()));

		this.dataLoader.run("");

		Mockito.verify(this.userRoleService, Mockito.never()).saveUserRole(Mockito.any());
	}
}
