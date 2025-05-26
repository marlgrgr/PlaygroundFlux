package gracia.marlon.playground.flux.configuration;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.test.StepVerifier;

public class CustomAuthenticationEntryPointTest {

	private final ObjectMapper objectMapper;

	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	public CustomAuthenticationEntryPointTest() {
		this.objectMapper = Mockito.mock(ObjectMapper.class);
		this.customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint(this.objectMapper);
	}

	@SuppressWarnings("serial")
	@Test
	public void throwJsonException() throws JsonProcessingException {
		ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
		ServerHttpResponse serverHttpResponse = Mockito.mock(ServerHttpResponse.class);
		ServerHttpRequest serverHttpRequest = Mockito.mock(ServerHttpRequest.class);
		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		URI uri = Mockito.mock(URI.class);
		JsonProcessingException error = new JsonProcessingException("Serialization failed") {
		};

		Mockito.when(exchange.getResponse()).thenReturn(serverHttpResponse);
		Mockito.when(exchange.getRequest()).thenReturn(serverHttpRequest);
		Mockito.when(serverHttpRequest.getHeaders()).thenReturn(httpHeaders);
		Mockito.when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);
		Mockito.when(serverHttpRequest.getURI()).thenReturn(uri);
		Mockito.when(uri.getPath()).thenReturn("/otherUri").thenReturn("/graphql");
		Mockito.doNothing().when(httpHeaders).setContentType(Mockito.any());
		Mockito.doNothing().when(httpHeaders).add(Mockito.any(), Mockito.any());

		Mockito.when(this.objectMapper.writeValueAsBytes(Mockito.any())).thenThrow(error);

		StepVerifier.create(this.customAuthenticationEntryPoint.commence(exchange, null))
				.expectErrorMatches(throwable -> throwable instanceof JsonProcessingException
						&& throwable.getMessage().equals("Serialization failed"))
				.verify();

		StepVerifier.create(this.customAuthenticationEntryPoint.commence(exchange, null))
				.expectErrorMatches(throwable -> throwable instanceof JsonProcessingException
						&& throwable.getMessage().equals("Serialization failed"))
				.verify();
	}
}
