package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserRoleDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomReactiveUserDetailsServiceTest {

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final ReactiveUserDetailsService reactiveUserDetailsService;

	public CustomReactiveUserDetailsServiceTest() {
		this.userService = Mockito.mock(UserService.class);
		this.userRoleService = Mockito.mock(UserRoleService.class);
		this.reactiveUserDetailsService = new CustomReactiveUserDetailsService(this.userService, this.userRoleService);
	}

	@Test
	public void findByUsernameSuccessful() {

		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setRole("myrole");

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRole(roleDTO);

		List<UserRoleDTO> userRoles = new ArrayList<UserRoleDTO>();
		userRoles.add(userRoleDTO);

		Flux<UserRoleDTO> fluxUserRoles = Flux.fromIterable(userRoles);

		Mockito.when(this.userService.getUserByUsername(Mockito.anyString())).thenReturn(Mono.just(user));
		Mockito.when(this.userRoleService.getAllUserRolesByUser(Mockito.anyLong())).thenReturn(fluxUserRoles);

		UserDetails userDetails = this.reactiveUserDetailsService.findByUsername("user").block();

		assertEquals("user", userDetails.getUsername());
		assertEquals(1, userDetails.getAuthorities().size());
	}

	@Test
	public void findByUsernameRoleNull() {

		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");

		UserRoleDTO userRoleDTORoleDTONull = new UserRoleDTO();
		userRoleDTORoleDTONull.setRole(null);

		List<UserRoleDTO> userRolesDTONull = new ArrayList<UserRoleDTO>();
		userRolesDTONull.add(userRoleDTORoleDTONull);

		Flux<UserRoleDTO> fluxUserRolesDTONull = Flux.fromIterable(userRolesDTONull);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setRole(null);
		UserRoleDTO userRoleDTORoleNull = new UserRoleDTO();
		userRoleDTORoleNull.setRole(roleDTO);

		List<UserRoleDTO> userRolesNull = new ArrayList<UserRoleDTO>();
		userRolesNull.add(userRoleDTORoleNull);

		Flux<UserRoleDTO> fluxUserRolesNull = Flux.fromIterable(userRolesNull);

		Mockito.when(this.userService.getUserByUsername(Mockito.anyString())).thenReturn(Mono.just(user));
		Mockito.when(this.userRoleService.getAllUserRolesByUser(Mockito.anyLong())).thenReturn(fluxUserRolesDTONull)
				.thenReturn(fluxUserRolesNull).thenReturn(Flux.empty());

		UserDetails userDetails = this.reactiveUserDetailsService.findByUsername("user").block();

		assertEquals("user", userDetails.getUsername());
		assertEquals(0, userDetails.getAuthorities().size());

		userDetails = this.reactiveUserDetailsService.findByUsername("user").block();

		assertEquals("user", userDetails.getUsername());
		assertEquals(0, userDetails.getAuthorities().size());
	}

	@Test
	public void findByUsernameUserEmptyRole() {

		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRole(null);

		List<UserRoleDTO> userRoles = new ArrayList<UserRoleDTO>();
		Flux<UserRoleDTO> fluxUserRoles = Flux.fromIterable(userRoles);

		Mockito.when(this.userService.getUserByUsername(Mockito.anyString())).thenReturn(Mono.just(user));
		Mockito.when(this.userRoleService.getAllUserRolesByUser(Mockito.anyLong())).thenReturn(fluxUserRoles);

		UserDetails userDetails = this.reactiveUserDetailsService.findByUsername("user").block();

		assertEquals("user", userDetails.getUsername());
		assertEquals(0, userDetails.getAuthorities().size());
	}

}
