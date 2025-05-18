package gracia.marlon.playground.flux.configuration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.test.StepVerifier;

public class CustomAccessDeniedHandlerTest {

	private final ObjectMapper objectMapper;

	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	public CustomAccessDeniedHandlerTest() {
		this.objectMapper = Mockito.mock(ObjectMapper.class);
		this.customAccessDeniedHandler = new CustomAccessDeniedHandler(this.objectMapper);
	}

	@Test
	public void throwJsonException() throws JsonProcessingException {
		ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
		ServerHttpResponse serverHttpResponse = Mockito.mock(ServerHttpResponse.class);
		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		JsonProcessingException error = new JsonProcessingException("Serialization failed") {
			private static final long serialVersionUID = 1L;
		};

		Mockito.when(exchange.getResponse()).thenReturn(serverHttpResponse);
		Mockito.when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);
		Mockito.doNothing().when(httpHeaders).setContentType(Mockito.any());

		Mockito.when(this.objectMapper.writeValueAsBytes(Mockito.any())).thenThrow(error);

		StepVerifier.create(this.customAccessDeniedHandler.handle(exchange, null))
				.expectErrorMatches(throwable -> throwable instanceof JsonProcessingException
						&& throwable.getMessage().equals("Serialization failed"))
				.verify();
	}
}
