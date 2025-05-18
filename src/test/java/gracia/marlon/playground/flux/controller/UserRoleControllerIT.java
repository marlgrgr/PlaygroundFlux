package gracia.marlon.playground.flux.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserRoleDTO;

public class UserRoleControllerIT extends AbstractIntegrationBase {

	@Test
	void userRoleIT() throws Exception {

		PagedResponse<UserRoleDTO> pagedResponse = webTestClient.get().uri("/api/v1/userRoles")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<UserRoleDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(2L, pagedResponse.getTotalResults());

		UserRoleDTO userUserRole = pagedResponse.getResults().stream()
				.filter(userRole -> "ROLE_USER".equalsIgnoreCase(userRole.getRole().getRole())).findFirst().get();
		Long idRoleUser = userUserRole.getId();
		Long userRoleUser = userUserRole.getUser().getId();
		Long roleUser = userUserRole.getRole().getId();

		pagedResponse = webTestClient.get().uri("/api/v1/userRoles/user/" + userRoleUser)
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<UserRoleDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(2L, pagedResponse.getTotalResults());

		List<UserRoleDTO> userRoleList = webTestClient.get().uri("/api/v1/userRoles/user/" + userRoleUser + "/all")
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isOk()
				.expectBodyList(UserRoleDTO.class).returnResult().getResponseBody();

		assertEquals(2L, userRoleList.size());

		pagedResponse = webTestClient.get().uri("/api/v1/userRoles/role/" + roleUser).accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<UserRoleDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(1L, pagedResponse.getTotalResults());

		webTestClient.delete().uri("/api/v1/userRoles/" + idRoleUser)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isNoContent();

		pagedResponse = webTestClient.get().uri("/api/v1/userRoles").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<UserRoleDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(1L, pagedResponse.getTotalResults());

		UserDTO userDTO = new UserDTO();
		userDTO.setId(userRoleUser);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(roleUser);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setUser(userDTO);
		userRoleDTO.setRole(roleDTO);

		webTestClient.post().uri("/api/v1/userRoles").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).bodyValue(userRoleDTO).exchange().expectStatus()
				.isCreated();

		pagedResponse = webTestClient.get().uri("/api/v1/userRoles")
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<UserRoleDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(2L, pagedResponse.getTotalResults());
	}
}
