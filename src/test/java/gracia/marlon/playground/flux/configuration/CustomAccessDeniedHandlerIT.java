package gracia.marlon.playground.flux.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;

public class CustomAccessDeniedHandlerIT extends AbstractIntegrationBase {

	@Test
	void handleForbiddenIT() throws Exception {

		UserWithPasswordDTO userWithPasswordDTO = new UserWithPasswordDTO();
		userWithPasswordDTO.setUsername("newuser");
		userWithPasswordDTO.setFullname("user");
		userWithPasswordDTO.setPassword("pass");

		webTestClient.post().uri("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).bodyValue(userWithPasswordDTO).exchange()
				.expectStatus().isCreated();

		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setUsername("newuser");
		authRequestDTO.setPassword("pass");

		String token = webTestClient.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(authRequestDTO).exchange().expectStatus().isOk().expectBody(String.class).returnResult()
				.getResponseBody();

		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setOldPassword("pass");
		changePasswordDTO.setNewPassword("Newuser*123");
		changePasswordDTO.setConfirmNewPassword("Newuser*123");

		webTestClient.post().uri("/api/v1/auth/changePassword").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).bodyValue(changePasswordDTO).exchange().expectStatus()
				.isNoContent();

		authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setUsername("newuser");
		authRequestDTO.setPassword("Newuser*123");

		token = webTestClient.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(authRequestDTO).exchange().expectStatus().isOk().expectBody(String.class).returnResult()
				.getResponseBody();

		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token).exchange().expectStatus().isForbidden();

		PagedResponse<UserDTO> pagedResponse = webTestClient.get().uri("/api/v1/users")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<UserDTO>>() {
				}).returnResult().getResponseBody();

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		webTestClient.delete().uri("/api/v1/users/" + idFirstResult)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isNoContent();
	}

}
