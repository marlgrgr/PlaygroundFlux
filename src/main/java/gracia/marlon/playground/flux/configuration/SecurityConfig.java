package gracia.marlon.playground.flux.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import gracia.marlon.playground.flux.filter.JWTAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JWTAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(exchanges -> exchanges.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.pathMatchers("/api/v1/auth/**").permitAll().pathMatchers("/api/v1/**").authenticated()
						.anyExchange().permitAll())
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(this.customAuthenticationEntryPoint)
						.accessDeniedHandler(this.customAccessDeniedHandler))
				.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();

	}

}
