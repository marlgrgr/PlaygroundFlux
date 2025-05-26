package gracia.marlon.playground.flux.configuration;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAuthenticationEntryPointIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unchecked")
	@Test
	void handleUnauthorizedIT() throws Exception {
		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isUnauthorized();

		String queryStr = """
				query GetUsers {
				    getUsers {
				        page
				        totalPages
				        totalResults
				        results {
				            id
				            passwordChangeRequired
				            fullname
				            username
				        }
				    }
				}
				""";

		String query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		Map<String, Object> responseMap = webTestClient.post().uri("/graphql").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		List<Map<String, Object>> errorList = objectMapper.convertValue(responseMap.get("errors"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		Map<String, Object> extensionsMap = (Map<String, Object>) errorList.getFirst().get("extensions");

		assertEquals("401", extensionsMap.get("httpCode").toString());
	}
}
