package gracia.marlon.playground.flux.graphql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.RoleDTO;

public class RoleGraphQLControllerIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unchecked")
	@Test
	void roleIT() throws Exception {

		String queryStr = """
				query GetRoleList {
				    getRoleList {
				        id
				        role
				    }
				}
				""";

		String query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		Map<String, Object> responseMap = webTestClient.post().uri("/graphql")
				.header("Authorization", "Bearer " + this.getToken()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		Map<String, Object> responseData = (Map<String, Object>) responseMap.get("data");
		List<RoleDTO> roleList = objectMapper.convertValue(responseData.get("getRoleList"),
				new TypeReference<List<RoleDTO>>() {
				});

		assertEquals(2, roleList.size());

		List<String> roleNameList = roleList.stream().map(role -> role.getRole()).collect(Collectors.toList());

		assertTrue(roleNameList.contains("ROLE_ADMIN"));
		assertTrue(roleNameList.contains("ROLE_USER"));
	}
}
