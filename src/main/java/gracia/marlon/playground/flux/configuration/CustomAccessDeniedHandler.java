package gracia.marlon.playground.flux.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public CustomAccessDeniedHandler() {
		this.objectMapper = new ObjectMapper();
	}

	public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
		ServerHttpResponse response = exchange.getResponse();

		response.setStatusCode(HttpStatus.FORBIDDEN);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("message", "Access denied: you do not have the required permissions.");
		errorDetails.put("code", "AUTH-0019");
		errorDetails.put("httpCode", 403);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(errorDetails);
			DataBuffer buffer = response.bufferFactory().wrap(bytes);
			return response.writeWith(Mono.just(buffer));
		} catch (JsonProcessingException e) {
			return Mono.error(e);
		}
	}
}
