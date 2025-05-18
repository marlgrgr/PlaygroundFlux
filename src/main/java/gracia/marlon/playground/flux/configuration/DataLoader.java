package gracia.marlon.playground.flux.configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.model.Role;
import gracia.marlon.playground.flux.model.UserRole;
import gracia.marlon.playground.flux.model.Users;
import gracia.marlon.playground.flux.services.RoleService;
import gracia.marlon.playground.flux.services.UserRoleService;
import gracia.marlon.playground.flux.services.UserService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class DataLoader implements CommandLineRunner {

	private final DatabaseClient databaseClient;

	private final UserService userService;

	private final RoleService roleService;

	private final UserRoleService userRoleService;

	private final PasswordEncoder passwordEncoder;

	private final RedissonReactiveClient redisson;

	private final String defaultAdminUser;

	private final String defaultAdminPassword;

	private final String LOAD_DATA_LOCK = "load-data-lock";

	private final int MAX_WAIT_FOR_LOCK_TIME = 10;

	private final int MAX_TIME_WITH_LOCK = 60;

	public DataLoader(DatabaseClient databaseClient, UserService userService, RoleService roleService,
			UserRoleService userRoleService, PasswordEncoder passwordEncoder, RedissonReactiveClient redisson,
			Environment env) {
		this.userService = userService;
		this.roleService = roleService;
		this.userRoleService = userRoleService;
		this.passwordEncoder = passwordEncoder;
		this.redisson = redisson;
		this.databaseClient = databaseClient;
		this.defaultAdminUser = env.getProperty("admin.default.user", "admin");
		this.defaultAdminPassword = env.getProperty("admin.default.password", "admin123");
	}

	@Override
	public void run(String... args) throws Exception {
		RLockReactive lock = redisson.getLock(LOAD_DATA_LOCK);

		lock.tryLock(MAX_WAIT_FOR_LOCK_TIME, MAX_TIME_WITH_LOCK, TimeUnit.SECONDS).flatMap(locked -> {
			if (locked) {
				return waitForTables("users", "role", "user_role").then(insertDefaultUserRoles());
			}

			return Mono.empty();
		}).onErrorResume(error -> {
			log.error("An error occurred while creating the starting data", error);
			return Mono.empty();
		}).subscribe();

	}

	private Mono<Void> waitForTables(String... tableNames) {
		return Flux.fromArray(tableNames).flatMap(this::waitForTable).then();
	}

	private Mono<Void> waitForTable(String tableName) {
		return Mono.defer(() -> databaseClient.sql("SELECT 1 FROM " + tableName + " LIMIT 1").fetch().first().then()
				.onErrorResume(e -> {
					return Mono.delay(Duration.ofMillis(500)).then(waitForTable(tableName));
				}));
	}

	private Mono<UserDTO> insertDefaultUserRoles() {
		Mono<RoleDTO> monoAdminRole = this.roleService.findByRole("ROLE_ADMIN")
				.switchIfEmpty(Mono.defer(() -> createNewRole("ROLE_ADMIN")));
		Mono<RoleDTO> monoUserRole = this.roleService.findByRole("ROLE_USER")
				.switchIfEmpty(Mono.defer(() -> createNewRole("ROLE_USER")));

		return monoAdminRole.zipWith(monoUserRole).flatMap(tuple -> {
			RoleDTO adminRole = tuple.getT1();
			RoleDTO userRole = tuple.getT2();

			return this.userService.findByUsername(defaultAdminUser)
					.switchIfEmpty(Mono.defer(() -> createUserWithRoles(defaultAdminUser, adminRole, userRole))).next();
		});

	}

	private Mono<RoleDTO> createNewRole(String roleName) {
		Role role = new Role(null, roleName);
		return this.roleService.saveRole(role);
	}

	private Mono<UserDTO> createUserWithRoles(String user, RoleDTO... roles) {
		Users users = new Users(null, this.defaultAdminUser, this.passwordEncoder.encode(this.defaultAdminPassword),
				this.defaultAdminUser, true, false);

		Mono<UserDTO> userCreated = this.userService.saveUser(users);

		return userCreated.flatMap(newUser -> {
			List<Mono<UserRole>> userRolesCreated = Arrays.stream(roles).map(role -> {
				UserRole userRole = new UserRole(null, newUser.getId(), role.getId());
				return this.userRoleService.saveUserRole(userRole);
			}).toList();

			return Mono.when(userRolesCreated).thenReturn(newUser);
		});
	}
}
