package gracia.marlon.playground.flux.graphql;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;

public class UserGraphQLControllerIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unchecked")
	@Test
	void userIT() throws Exception {
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

		Map<String, Object> responseMap = webTestClient.post().uri("/graphql")
				.header("Authorization", "Bearer " + this.getToken()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		Map<String, Object> responseData = (Map<String, Object>) responseMap.get("data");
		PagedResponse<UserDTO> pagedResponse = objectMapper.convertValue(responseData.get("getUsers"),
				new TypeReference<PagedResponse<UserDTO>>() {
				});

		assertEquals(1L, pagedResponse.getTotalResults());

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		queryStr = String.format("""
				query GetUserById {
				    getUserById(userId: \"%s\" ) {
				        id
				        username
				        fullname
				        passwordChangeRequired
				    }
				}
				""", idFirstResult);

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		UserDTO user = objectMapper.convertValue(responseData.get("getUserById"), UserDTO.class);
		assertEquals("admin", user.getUsername());

		queryStr = String.format("""
				query GetUserByUsername {
				    getUserByUsername(username: \"%s\" ) {
				        id
				        username
				        fullname
				        passwordChangeRequired
				    }
				}
				""", user.getUsername());

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		user = objectMapper.convertValue(responseData.get("getUserByUsername"), UserDTO.class);

		assertEquals("admin", user.getUsername());

		String mutation = """
				mutation CreateUser {
				    createUser(user: { username: "user", password: "user", fullname: "pass" })
				}
				""";

		query = "{\"query\": \"" + mutation.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		Boolean responseBoolean = (Boolean) responseData.get("createUser");
		assertEquals(true, responseBoolean);

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
		pagedResponse = objectMapper.convertValue(responseData.get("getUsers"),
				new TypeReference<PagedResponse<UserDTO>>() {
				});

		assertEquals(2L, pagedResponse.getTotalResults());

		idFirstResult = pagedResponse.getResults().getFirst().getId();

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
		pagedResponse = objectMapper.convertValue(responseData.get("getUsers"),
				new TypeReference<PagedResponse<UserDTO>>() {
				});

		assertEquals(1L, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId().longValue());
	}

}
