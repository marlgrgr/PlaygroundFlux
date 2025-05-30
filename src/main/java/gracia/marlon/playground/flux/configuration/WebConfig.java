package gracia.marlon.playground.flux.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

	private final String[] allowedOrigins;

	public WebConfig(Environment env) {
		this.allowedOrigins = env.getProperty("cors.allowed-origins", "").split(",");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins(this.allowedOrigins).allowedMethods("*")
				.allowedHeaders("Content-Type", "Authorization").allowCredentials(true);
	}
}