package gracia.marlon.playground.flux.controller;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;

public class UserControllerIT extends AbstractIntegrationBase {

	@Test
	void userIT() throws Exception {

		PagedResponse<UserDTO> pagedResponse = webTestClient.get().uri("/api/v1/users")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<UserDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(1L, pagedResponse.getTotalResults());

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		UserDTO user = webTestClient.get().uri("/api/v1/users/" + idFirstResult).accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectBody(UserDTO.class).returnResult().getResponseBody();

		assertEquals("admin", user.getUsername());

		user = webTestClient.get().uri("/api/v1/users/username/" + user.getUsername())
				.header("Authorization", "Bearer " + this.getToken()).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectBody(UserDTO.class).returnResult().getResponseBody();

		assertEquals("admin", user.getUsername());

		UserWithPasswordDTO userWithPasswordDTO = new UserWithPasswordDTO();
		userWithPasswordDTO.setUsername("user");
		userWithPasswordDTO.setFullname("user");
		userWithPasswordDTO.setPassword("pass");

		webTestClient.post().uri("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).bodyValue(userWithPasswordDTO).exchange()
				.expectStatus().isCreated();

		pagedResponse = webTestClient.get().uri("/api/v1/users").header("Authorization", "Bearer " + this.getToken())
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<UserDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(2L, pagedResponse.getTotalResults());

		idFirstResult = pagedResponse.getResults().getFirst().getId();

		webTestClient.delete().uri("/api/v1/users/" + idFirstResult)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isNoContent();

		pagedResponse = webTestClient.get().uri("/api/v1/users").header("Authorization", "Bearer " + this.getToken())
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<UserDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(1L, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId().longValue());
	}

}
