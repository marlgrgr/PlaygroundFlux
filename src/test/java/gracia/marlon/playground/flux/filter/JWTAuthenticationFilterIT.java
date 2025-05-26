package gracia.marlon.playground.flux.filter;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;
import gracia.marlon.playground.flux.util.SharedConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JWTAuthenticationFilterIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void JWTWithoutSubject() throws Exception {
		final Map<String, Object> claims = new HashMap<String, Object>();
		claims.put(SharedConstants.CLAIMS_PASSWORD_CHANGE_REQUIRED, false);
		claims.put(SharedConstants.CLAIMS_USER_ID, 1L);
		claims.put(SharedConstants.CLAIMS_USER_FULLNAME, "user");

		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("user");

		String token = generateJWTWithoutSubject(claims, user);

		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token).exchange().expectStatus().isUnauthorized();
	}

	@SuppressWarnings("unchecked")
	@Test
	void JWTChangePasswordRequired() throws Exception {

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

		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token).exchange().expectStatus().isForbidden();

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
				.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		List<Map<String, Object>> errorList = objectMapper.convertValue(responseMap.get("errors"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		Map<String, Object> extensionsMap = (Map<String, Object>) errorList.getFirst().get("extensions");

		assertEquals("403", extensionsMap.get("httpCode").toString());

		PagedResponse<UserDTO> pagedResponse = webTestClient.get().uri("/api/v1/users")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<UserDTO>>() {
				}).returnResult().getResponseBody();

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		webTestClient.delete().uri("/api/v1/users/" + idFirstResult)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isNoContent();
	}

	@Test
	void JWTTokenException() throws Exception {
		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer nonvalidtoken").exchange().expectStatus().isUnauthorized();
	}

	@Test
	void JWTExpiredToken() throws Exception {
		final Map<String, Object> claims = new HashMap<String, Object>();
		claims.put(SharedConstants.CLAIMS_PASSWORD_CHANGE_REQUIRED, false);
		claims.put(SharedConstants.CLAIMS_USER_ID, 1L);
		claims.put(SharedConstants.CLAIMS_USER_FULLNAME, "admin");

		UserDTO user = new UserDTO();
		user.setId(1L);
		user.setUsername("admin");

		String token = generateExpiredJWT(claims, user);

		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token).exchange().expectStatus().isUnauthorized();
	}

	private String generateJWTWithoutSubject(Map<String, Object> extraClaims, UserDTO user) {
		String secretKey = "5v8y/B?E(H+MbQeThWmZq4t7w!z%C&F)";

		return Jwts.builder().setClaims(extraClaims).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 3600000))
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}

	private String generateExpiredJWT(Map<String, Object> extraClaims, UserDTO user) {
		String secretKey = "5v8y/B?E(H+MbQeThWmZq4t7w!z%C&F)";

		return Jwts.builder().setClaims(extraClaims).setSubject(user.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis() - 5000))
				.setExpiration(new Date(System.currentTimeMillis() - 1000))
				.signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}
}
