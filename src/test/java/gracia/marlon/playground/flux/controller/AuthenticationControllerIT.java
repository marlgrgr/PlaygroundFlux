package gracia.marlon.playground.flux.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;

public class AuthenticationControllerIT extends AbstractIntegrationBase {

	@Test
	void changePasswordNoToken() throws Exception {
		ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
		changePasswordDTO.setOldPassword("admin123");
		changePasswordDTO.setNewPassword("Admin*123");
		changePasswordDTO.setConfirmNewPassword("Admin*123");

		webTestClient.post().uri("/api/v1/auth/changePassword").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(changePasswordDTO).exchange().expectStatus().isBadRequest();

		webTestClient.post().uri("/api/v1/auth/changePassword").header("Authorization", "doesn't start with bearer")
				.contentType(MediaType.APPLICATION_JSON).bodyValue(changePasswordDTO).exchange().expectStatus()
				.isBadRequest();
	}
}
