package gracia.marlon.playground.flux.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.graphql.execution.ErrorType;
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

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
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

		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		if (exchange.getRequest().getURI().getPath().contains("/graphql")) {
			Map<String, Object> extensions = new HashMap<>();
			extensions.put("message", "Unauthorized: authentication is required.");
			extensions.put("code", "AUTH-0020");
			extensions.put("httpCode", 401);

			ErrorType errorType = ErrorType.UNAUTHORIZED;

			GraphQLError graphError = GraphqlErrorBuilder.newError()
					.message("Unauthorized: authentication is required.").errorType(errorType).extensions(extensions)
					.build();

			Map<String, Object> errorGraphDetails = graphError.toSpecification();
			Map<String, Object> responseMap = new HashMap<String, Object>();
			List<Object> errorMap = new ArrayList<Object>();
			errorMap.add(errorGraphDetails);
			responseMap.put("errors", errorMap);
			response.setStatusCode(HttpStatus.OK);
			try {
				byte[] bytes = objectMapper.writeValueAsBytes(responseMap);
				DataBuffer buffer = response.bufferFactory().wrap(bytes);
				return response.writeWith(Mono.just(buffer));
			} catch (JsonProcessingException e) {
				return Mono.error(e);
			}
		}

		response.setStatusCode(HttpStatus.UNAUTHORIZED);

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