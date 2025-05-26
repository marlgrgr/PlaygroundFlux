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

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;

public class GraphQLExceptionHandlerIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unchecked")
	@Test
	void handleForbiddenIT() throws Exception {

		String mutation = """
				mutation CreateUser {
				    createUser(user: { username: "newuser", password: "", fullname: "pass" })
				}
				""";

		String query = "{\"query\": \"" + mutation.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		Map<String, Object> responseMap = webTestClient.post().uri("/graphql")
				.header("Authorization", "Bearer " + this.getToken()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		List<Map<String, Object>> errorList = objectMapper.convertValue(responseMap.get("errors"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		Map<String, Object> extensionsMap = (Map<String, Object>) errorList.getFirst().get("extensions");

		assertEquals("400", extensionsMap.get("httpCode").toString());

		mutation = """
				mutation CreateUser {
				    createUser(user: { username: "newuser", password: "pass", fullname: "pass" })
				}
				""";

		query = "{\"query\": \"" + mutation.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		Map<String, Object> responseData = (Map<String, Object>) responseMap.get("data");
		Boolean responseBoolean = (Boolean) responseData.get("createUser");
		assertEquals(true, responseBoolean);

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

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		errorList = objectMapper.convertValue(responseMap.get("errors"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		extensionsMap = (Map<String, Object>) errorList.getFirst().get("extensions");

		assertEquals("403", extensionsMap.get("httpCode").toString());

		queryStr = """
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

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		PagedResponse<UserDTO> pagedResponse = objectMapper.convertValue(responseData.get("getUsers"),
				new TypeReference<PagedResponse<UserDTO>>() {
				});

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		mutation = String.format("""
					mutation DeleteUser {
					    deleteUser(userId: \"%s\" )
					}
				""", idFirstResult);

		query = "{\"query\": \"" + mutation.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		responseBoolean = (Boolean) responseData.get("deleteUser");
		assertEquals(true, responseBoolean);

	}

}
