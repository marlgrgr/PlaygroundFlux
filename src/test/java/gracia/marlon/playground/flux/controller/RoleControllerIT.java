package gracia.marlon.playground.flux.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.RoleDTO;

public class RoleControllerIT extends AbstractIntegrationBase {

	@Test
	void roleIT() {
		List<RoleDTO> roleList = webTestClient.get().uri("/api/v1/roles")
				.header("Authorization", "Bearer " + this.getToken()).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectBodyList(RoleDTO.class).returnResult().getResponseBody();

		assertEquals(2, roleList.size());

		List<String> roleNames = roleList.stream().map(RoleDTO::getRole).collect(Collectors.toList());

		assertTrue(roleNames.contains("ROLE_ADMIN"));
		assertTrue(roleNames.contains("ROLE_USER"));
	}
}
