package gracia.marlon.playground.flux.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public CustomAuthenticationEntryPoint() {
		this.objectMapper = new ObjectMapper();
	}

	public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
		ServerHttpResponse response = exchange.getResponse();

		HttpHeaders headers = response.getHeaders();
		String origin = exchange.getRequest().getHeaders().getOrigin();
		headers.add("Access-Control-Allow-Origin", origin);
		headers.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
		headers.add("Access-Control-Allow-Headers", "Authorization, Content-Type");
		headers.add("Access-Control-Allow-Credentials", "true");

		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("message", "Unauthorized: authentication is required.");
		errorDetails.put("code", "AUTH-0020");
		errorDetails.put("httpCode", 401);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(errorDetails);
			DataBuffer buffer = response.bufferFactory().wrap(bytes);
			return response.writeWith(Mono.just(buffer));
		} catch (JsonProcessingException e) {
			return Mono.error(e);
		}
	}
}