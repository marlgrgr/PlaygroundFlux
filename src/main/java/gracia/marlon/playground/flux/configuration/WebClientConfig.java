package gracia.marlon.playground.flux.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	private final String apiMovieBaseUrl;

	private final String apiMovieApiKey;

	public WebClientConfig(Environment env) {
		this.apiMovieBaseUrl = env.getProperty("api.movie.baseUrl", "");
		this.apiMovieApiKey = env.getProperty("api.movie.apiKey", "");
	}

	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.baseUrl(this.apiMovieBaseUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.apiMovieApiKey).build();
	}
}
