package gracia.marlon.playground.flux.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class CustomAuthenticationEntryPointIT extends AbstractIntegrationBase {

	@Test
	void handleUnauthorizedIT() throws Exception {
		webTestClient.get().uri("/api/v1/users").accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isUnauthorized();
	}
}
