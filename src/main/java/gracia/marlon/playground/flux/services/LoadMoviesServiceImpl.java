package gracia.marlon.playground.flux.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import gracia.marlon.playground.flux.dtos.GenreResponseDTO;
import gracia.marlon.playground.flux.dtos.MovieDetailsResponseDTO;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LoadMoviesServiceImpl implements LoadMoviesService {

	private final WebClient webClient;

	private final SaveMoviesService saveMoviesService;

	private final String apiMovieDiscoverPath;

	private final String apiMovieGenrePath;

	private final String apiMovieBaseUrl;

	private final String apiMovieApiKey;

	private ConcurrentMap<Integer, String> genreMap = new ConcurrentHashMap<>();

	public LoadMoviesServiceImpl(WebClient webClient, SaveMoviesService saveMoviesService, Environment env) {
		this.saveMoviesService = saveMoviesService;
		this.webClient = webClient;

		this.apiMovieDiscoverPath = env.getProperty("api.movie.discover.path", "");
		this.apiMovieGenrePath = env.getProperty("api.movie.genre.path", "");
		this.apiMovieBaseUrl = env.getProperty("api.movie.baseUrl", "");
		this.apiMovieApiKey = env.getProperty("api.movie.apiKey", "");
	}

	@Override
	public Mono<Void> loadMovies() {
		if (!validURLParameters()) {
			log.info("Movies can not be loaded, not all parameters are configured");
			return Mono.empty();
		}

		return getMoviesFromService().flatMap(movieDetailsResponseDTO -> {
			return this.saveMoviesService.saveMovieList(movieDetailsResponseDTO);
		}).onErrorResume(e -> {
			log.error("Error while loading movies", e);
			return Mono.empty();
		}).then();
	}

	private Mono<MovieDetailsResponseDTO> getMoviesFromService() {

		String url = this.apiMovieDiscoverPath;
		url = this.getMovieDiscoverUrl(url);

		return webClient.get().uri(url).retrieve()
				.onStatus(httpStatus -> !httpStatus.is2xxSuccessful(), clientResponse -> {
					log.error("An error occurred while retrieving the movie listing. Status code: "
							+ clientResponse.statusCode().value());
					return Mono.error(new RuntimeException("Failed to fetch movie details"));
				}).bodyToMono(MovieDetailsResponseDTO.class).flatMap(response -> {
					if (!validMovieDetailsResponseDTO(response)) {
						log.error("Invalid movie details response");
						return Mono.empty();
					}

					List<Integer> genreList = response.getResults().stream()
							.filter(result -> result.getGenreIds() != null)
							.flatMap(result -> result.getGenreIds().stream()).distinct().collect(Collectors.toList());

					return loadGenres(genreList).thenReturn(response).map(movieDetails -> {
						movieDetails.getResults()
								.forEach(result -> result.setGenres(getGenreList(result.getGenreIds())));
						return movieDetails;
					});
				}).onErrorResume(e -> {
					log.error("An error occurred while retrieving the movie list from the external service", e);
					return Mono.empty();
				});
	}

	private Mono<Void> loadGenres(List<Integer> genreIds) {
		if (genreIds.isEmpty()) {
			return Mono.empty();
		}

		boolean thereAreGenresMissing = genreIds.stream().anyMatch(id -> !genreMap.containsKey(id));
		if (!thereAreGenresMissing) {
			return Mono.empty();
		}

		return webClient.get().uri(this.apiMovieGenrePath).retrieve()
				.onStatus(httpStatus -> !httpStatus.is2xxSuccessful(), clientResponse -> {
					log.error("An error occurred while retrieving the genre list. Status code: "
							+ clientResponse.statusCode().value());
					return Mono.error(new RuntimeException("Failed to fetch Genre List"));
				})
				.bodyToMono(GenreResponseDTO.class)
				.doOnNext(genreResponse -> {
					if (genreResponse.getGenres() == null || genreResponse.getGenres().isEmpty()) {
						return;
					}

					genreMap.clear();
					genreResponse.getGenres().forEach(genre -> genreMap.put(genre.getId(), genre.getName()));
				}).onErrorResume(e -> {
					log.error("An error occurred while retrieving the genres listing", e);
					return Mono.empty();
				}).then();
	}

	private String getMovieDiscoverUrl(String basePath) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate today = LocalDate.now();
		String todayFormatted = today.format(formatter);

		LocalDate aMonthAgo = today.minusMonths(1);
		String aMonthAgoFormatted = aMonthAgo.format(formatter);

		return basePath + "&release_date.gte=" + aMonthAgoFormatted + "&release_date.lte=" + todayFormatted;
	}

	private List<String> getGenreList(List<Integer> genreIds) {
		if (genreIds == null || genreIds.isEmpty()) {
			return new ArrayList<>();
		}

		return genreIds.stream().map(id -> genreMap.get(id)).filter(name -> name != null).collect(Collectors.toList());
	}

	private boolean validURLParameters() {
		return !this.apiMovieBaseUrl.isEmpty() && !this.apiMovieDiscoverPath.isEmpty() && !this.apiMovieApiKey.isEmpty()
				&& !this.apiMovieGenrePath.isEmpty();
	}

	private boolean validMovieDetailsResponseDTO(MovieDetailsResponseDTO movieDetailsResponseDTO) {
		return movieDetailsResponseDTO.getResults() != null && !movieDetailsResponseDTO.getResults().isEmpty();
	}
}