package gracia.marlon.playground.flux.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserRoleDTO;
import gracia.marlon.playground.flux.exception.RestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AuthenticationServiceTest {

	private final JWTService jwtService;

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final AuthenticationService authenticationService;

	public AuthenticationServiceTest() {
		this.jwtService = Mockito.mock(JWTService.class);
		this.userService = Mockito.mock(UserService.class);
		this.userRoleService = Mockito.mock(UserRoleService.class);
		this.authenticationService = new AuthenticationServiceImpl(this.jwtService, this.userService,
				this.userRoleService);
	}

	@Test
	public void loginSuccessful() {
		AuthRequestDTO request = new AuthRequestDTO();
		request.setUsername("user");
		request.setPassword("user*123");

		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		List<UserRoleDTO> userRoleList = new ArrayList<UserRoleDTO>();
		userRoleList.add(userRoleDTO);

		Flux<UserRoleDTO> fluxUserRoleDTO = Flux.fromIterable(userRoleList);

		Mockito.when(this.userService.validUser(request)).thenReturn(Mono.just(true));
		Mockito.when(this.userService.getUserByUsername("user")).thenReturn(Mono.just(user));
		Mockito.when(this.userRoleService.getAllUserRolesByUser(Mockito.anyLong())).thenReturn(fluxUserRoleDTO);
		Mockito.when(this.jwtService.generateToken(Mockito.anyMap(), Mockito.any())).thenReturn("token123");

		assertEquals("token123", this.authenticationService.login(request).block());

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setRole("role");
		userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRole(roleDTO);
		userRoleList = new ArrayList<UserRoleDTO>();
		userRoleList.add(userRoleDTO);

		fluxUserRoleDTO = Flux.fromIterable(userRoleList);

		Mockito.when(this.userRoleService.getAllUserRolesByUser(Mockito.anyLong())).thenReturn(fluxUserRoleDTO);

		assertEquals("token123", this.authenticationService.login(request).block());

	}

	@Test
	public void loginInvalidRequest() {
		final AuthRequestDTO nullRequest = null;

		RestException restException = assertThrows(RestException.class,
				() -> this.authenticationService.login(nullRequest).block());

		assertEquals("AUTH-0013", restException.getError().getCode());

		final AuthRequestDTO requestNullUser = new AuthRequestDTO();
		requestNullUser.setPassword("pass");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.login(requestNullUser).block());

		assertEquals("AUTH-0002", restException.getError().getCode());

		final AuthRequestDTO requestEmptyUser = new AuthRequestDTO();
		requestEmptyUser.setUsername("");
		requestEmptyUser.setPassword("pass");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.login(requestEmptyUser).block());

		assertEquals("AUTH-0002", restException.getError().getCode());

		final AuthRequestDTO requestNullPass = new AuthRequestDTO();
		requestNullPass.setUsername("user");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.login(requestNullPass).block());

		assertEquals("AUTH-0005", restException.getError().getCode());

		final AuthRequestDTO requestEmptyPass = new AuthRequestDTO();
		requestEmptyPass.setUsername("user");
		requestEmptyPass.setPassword("");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.login(requestEmptyPass).block());

		assertEquals("AUTH-0005", restException.getError().getCode());

	}

	@Test
	public void loginFailUserValidation() {
		AuthRequestDTO request = new AuthRequestDTO();
		request.setUsername("user");
		request.setPassword("user*123");

		Mockito.when(this.userService.validUser(request)).thenReturn(Mono.just(false));

		RestException restException = assertThrows(RestException.class,
				() -> this.authenticationService.login(request).block());

		assertEquals("AUTH-0014", restException.getError().getCode());
	}

	@Test
	public void changePasswordSuccessful() {
		String token = "token123";

		Mockito.when(this.jwtService.extractUsername(token)).thenReturn("user");
		Mockito.when(this.jwtService.extractUserId(token)).thenReturn(1L);
		Mockito.when(this.jwtService.isTokenValid(Mockito.anyString(), Mockito.any())).thenReturn(true);
		Mockito.when(this.userService.validUser(Mockito.any())).thenReturn(Mono.just(true));
		Mockito.when(this.userService.changePassword(Mockito.anyLong(), Mockito.any())).thenReturn(Mono.empty());

		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setConfirmNewPassword("newPass*123");
		changePasswordDTO.setNewPassword("newPass*123");
		changePasswordDTO.setOldPassword("oldPass");

		this.authenticationService.changePassword(token, changePasswordDTO).block();

		Mockito.verify(this.userService).changePassword(Mockito.anyLong(), Mockito.any());

	}

	@Test
	public void changePasswordJWTFails() {
		String token = "token123";

		Mockito.when(this.jwtService.extractUsername(token)).thenThrow(new RuntimeException());

		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setConfirmNewPassword("newPass*123");
		changePasswordDTO.setNewPassword("newPass*123");
		changePasswordDTO.setOldPassword("oldPass");

		RestException restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTO).block());

		assertEquals("AUTH-0016", restException.getError().getCode());

	}

	@Test
	public void changePasswordFailValidations() {
		String token = "token123";

		Mockito.when(this.jwtService.extractUsername(token)).thenReturn("user");
		Mockito.when(this.jwtService.extractUserId(token)).thenReturn(1L);
		Mockito.when(this.jwtService.isTokenValid(Mockito.anyString(), Mockito.any())).thenReturn(true);

		final ChangePasswordDTO changePasswordDTONull = null;

		RestException restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTONull).block());
		assertEquals("AUTH-0007", restException.getError().getCode());

		final ChangePasswordDTO changePasswordDTONullNewPass = new ChangePasswordDTO();
		changePasswordDTONullNewPass.setConfirmNewPassword("newPass*123");
		changePasswordDTONullNewPass.setNewPassword(null);
		changePasswordDTONullNewPass.setOldPassword("oldPass");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTONullNewPass).block());
		assertEquals("AUTH-0008", restException.getError().getCode());

		final ChangePasswordDTO changePasswordDTONotEquals = new ChangePasswordDTO();
		changePasswordDTONotEquals.setConfirmNewPassword("newPass*1234");
		changePasswordDTONotEquals.setNewPassword("newPass*123");
		changePasswordDTONotEquals.setOldPassword("oldPass");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTONotEquals).block());
		assertEquals("AUTH-0008", restException.getError().getCode());

		final ChangePasswordDTO changePasswordDTOOldEqualsNew = new ChangePasswordDTO();
		changePasswordDTOOldEqualsNew.setConfirmNewPassword("newPass*123");
		changePasswordDTOOldEqualsNew.setNewPassword("newPass*123");
		changePasswordDTOOldEqualsNew.setOldPassword("newPass*123");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTOOldEqualsNew).block());
		assertEquals("AUTH-0018", restException.getError().getCode());

		final ChangePasswordDTO changePasswordDTONotValidPass = new ChangePasswordDTO();
		changePasswordDTONotValidPass.setConfirmNewPassword("newPass");
		changePasswordDTONotValidPass.setNewPassword("newPass");
		changePasswordDTONotValidPass.setOldPassword("oldPass");

		restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTONotValidPass).block());
		assertEquals("AUTH-0010", restException.getError().getCode());

	}

	@Test
	public void changePasswordNotValidUser() {
		String token = "token123";

		Mockito.when(this.jwtService.extractUsername(token)).thenReturn("user");
		Mockito.when(this.jwtService.extractUserId(token)).thenReturn(1L);
		Mockito.when(this.jwtService.isTokenValid(Mockito.anyString(), Mockito.any())).thenReturn(true);
		Mockito.when(this.userService.validUser(Mockito.any())).thenReturn(Mono.just(false));

		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setConfirmNewPassword("newPass*123");
		changePasswordDTO.setNewPassword("newPass*123");
		changePasswordDTO.setOldPassword("oldPass");

		RestException restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTO).block());
		assertEquals("AUTH-0014", restException.getError().getCode());
	}
	
	@Test
	public void changePasswordNotValidJWT() {
		String token = "token123";

		Mockito.when(this.jwtService.extractUsername(token)).thenReturn("user");
		Mockito.when(this.jwtService.extractUserId(token)).thenReturn(1L);
		Mockito.when(this.jwtService.isTokenValid(Mockito.anyString(), Mockito.any())).thenReturn(false);

		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setConfirmNewPassword("newPass*123");
		changePasswordDTO.setNewPassword("newPass*123");
		changePasswordDTO.setOldPassword("oldPass");

		RestException restException = assertThrows(RestException.class,
				() -> this.authenticationService.changePassword(token, changePasswordDTO).block());
		assertEquals("AUTH-0016", restException.getError().getCode());
	}
}
