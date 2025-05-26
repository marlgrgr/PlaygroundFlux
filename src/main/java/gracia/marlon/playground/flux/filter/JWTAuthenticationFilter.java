package gracia.marlon.playground.flux.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.services.JWTService;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter implements WebFilter {

	private final JWTService jwtService;

	private final ReactiveUserDetailsService userDetailsService;

	private final String PATH_TO_CHANGE_PASSWORD = "/api/v1/auth/changePassword";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String token = resolveToken(exchange.getRequest());
		if (token == null) {
			return chain.filter(exchange);
		}

		try {
			final String username = jwtService.extractUsername(token);

			final UserDTO user = new UserDTO();
			user.setUsername(username);

			if (this.jwtService.isTokenValid(token, user)) {

				final boolean passwordChangeRequired = jwtService.extractPasswordChangeRequired(token);
				if (passwordChangeRequired
						&& !exchange.getRequest().getURI().getPath().contains(PATH_TO_CHANGE_PASSWORD)) {
					if (exchange.getRequest().getURI().getPath().contains("/graphql")) {
						Map<String, Object> extensions = new HashMap<>();
						extensions.put("message", "You must change your password before using any other endpoint");
						extensions.put("code", "AUTH-0017");
						extensions.put("httpCode", 403);

						ErrorType errorType = ErrorType.FORBIDDEN;

						GraphQLError graphError = GraphqlErrorBuilder.newError()
								.message("You must change your password before using any other endpoint")
								.errorType(errorType).extensions(extensions).build();

						Map<String, Object> errorGraphDetails = graphError.toSpecification();
						Map<String, Object> responseMap = new HashMap<String, Object>();
						List<Object> errorMap = new ArrayList<Object>();
						errorMap.add(errorGraphDetails);
						responseMap.put("errors", errorMap);
						exchange.getResponse().setStatusCode(HttpStatus.OK);
						exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
						final ObjectMapper mapper = new ObjectMapper();

						return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
								.wrap(mapper.writeValueAsString(responseMap).getBytes())));
					} else {
						exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
						exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

						String errorJson = "{\"message\":\"You must change your password before using any other endpoint\",\"code\":\"AUTH-0017\",\"httpCode\":403}";
						return exchange.getResponse().writeWith(
								Mono.just(exchange.getResponse().bufferFactory().wrap(errorJson.getBytes())));
					}
				}

				return this.userDetailsService.findByUsername(username).flatMap(userDetails -> {
					Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
							userDetails.getAuthorities());

					SecurityContext context = new SecurityContextImpl(authentication);

					return chain.filter(exchange)
							.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
				});

			}
		} catch (Exception e) {
			log.error("An error occurred while verifying the security token", e);
		}
		return chain.filter(exchange);
	}

	private String resolveToken(ServerHttpRequest request) {
		String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
