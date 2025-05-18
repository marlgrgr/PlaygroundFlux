package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import gracia.marlon.playground.flux.dtos.GenreDTO;
import gracia.marlon.playground.flux.dtos.GenreResponseDTO;
import gracia.marlon.playground.flux.dtos.MovieDetailsDTO;
import gracia.marlon.playground.flux.dtos.MovieDetailsResponseDTO;
import reactor.core.publisher.Mono;

public class LoadMoviesServiceTest {

	private final WebClient webClient;

	private final Environment env;

	private LoadMoviesService loadMoviesService;

	public LoadMoviesServiceTest() {
		this.webClient = Mockito.mock(WebClient.class);
		this.env = Mockito.mock(Environment.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesSuccessful() {

		final SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);

		MovieDetailsResponseDTO movieDetailsResponseDTO = this.getMovieDetailsResponseDTO();
		GenreResponseDTO genreResponseDTO = this.getGenreResponseDTO();

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.just(genreResponseDTO));
		Mockito.when(saveMoviesService.saveMovieList(Mockito.any())).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		ArgumentCaptor<MovieDetailsResponseDTO> captor = ArgumentCaptor.forClass(MovieDetailsResponseDTO.class);
		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(2, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());
	}

	@Test
	public void loadMoviesValidURLParams() {
		final SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		Mockito.when(this.env.getProperty("api.movie.baseUrl", "")).thenReturn("");

		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		Mockito.when(this.env.getProperty("api.movie.baseUrl", "")).thenReturn("baseUrl");
		Mockito.when(this.env.getProperty("api.movie.discover.path", "")).thenReturn("");

		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		Mockito.when(this.env.getProperty("api.movie.discover.path", "")).thenReturn("discover.path");
		Mockito.when(this.env.getProperty("api.movie.apiKey", "")).thenReturn("");

		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		Mockito.when(this.env.getProperty("api.movie.apiKey", "")).thenReturn("apiKey");
		Mockito.when(this.env.getProperty("api.movie.genre.path", "")).thenReturn("");

		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesMovieDetailResponse() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class)).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		final ResponseSpec responseAnswerSpec = Mockito.mock(ResponseSpec.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseAnswerSpec);
		Mockito.when(responseAnswerSpec.onStatus(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
			Predicate<HttpStatus> statusPredicate = invocation.getArgument(0);
			Function<ClientResponse, Mono<? extends Throwable>> errorFunction = invocation.getArgument(1);

			boolean matches = statusPredicate.test(HttpStatus.BAD_GATEWAY);

			ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
			Mockito.when(mockClientResponse.statusCode()).thenReturn(HttpStatus.BAD_GATEWAY);

			if (matches) {
				errorFunction.apply(mockClientResponse);
			}

			return responseAnswerSpec;
		});
		Mockito.when(responseAnswerSpec.bodyToMono(MovieDetailsResponseDTO.class)).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		final ResponseSpec responseAnswerSpecAccepted = Mockito.mock(ResponseSpec.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseAnswerSpecAccepted);
		Mockito.when(responseAnswerSpecAccepted.onStatus(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
			Predicate<HttpStatus> statusPredicate = invocation.getArgument(0);
			Function<ClientResponse, Mono<? extends Throwable>> errorFunction = invocation.getArgument(1);

			boolean matches = statusPredicate.test(HttpStatus.ACCEPTED);

			ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
			Mockito.when(mockClientResponse.statusCode()).thenReturn(HttpStatus.ACCEPTED);

			if (matches) {
				errorFunction.apply(mockClientResponse);
			}

			return responseAnswerSpecAccepted;
		});
		Mockito.when(responseAnswerSpecAccepted.bodyToMono(MovieDetailsResponseDTO.class)).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		responseSpec = Mockito.mock(ResponseSpec.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.error(new RuntimeException("error calling the service")));

		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesMovieDetailValidation() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		MovieDetailsResponseDTO movieDetailsResponseDTO = new MovieDetailsResponseDTO();
		movieDetailsResponseDTO.setResults(null);
		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);
		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);
		List<MovieDetailsDTO> results = new ArrayList<MovieDetailsDTO>();
		movieDetailsResponseDTO.setResults(results);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		this.loadMoviesService.loadMovies().block();
		Mockito.verify(saveMoviesService, never()).saveMovieList(Mockito.any());

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesExceptionAtRestCall() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(this.webClient, saveMoviesService, this.env);

		MovieDetailsResponseDTO movieDetailsResponseDTO = this.getMovieDetailsResponseDTO();
		GenreResponseDTO genreResponseDTO = this.getGenreResponseDTO();

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.just(genreResponseDTO));
		Mockito.when(saveMoviesService.saveMovieList(Mockito.any()))
				.thenReturn(Mono.error(new RuntimeException("error saving the movie")));

		this.loadMoviesService.loadMovies().block();

		ArgumentCaptor<MovieDetailsResponseDTO> captor = ArgumentCaptor.forClass(MovieDetailsResponseDTO.class);
		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(2, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesValidGenresList() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		WebClient localWebClient = Mockito.mock(WebClient.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		List<Integer> genreIds = null;

		MovieDetailsDTO movieDetailsDTO = new MovieDetailsDTO();
		movieDetailsDTO.setId(1L);
		movieDetailsDTO.setGenreIds(genreIds);

		List<MovieDetailsDTO> results = new ArrayList<MovieDetailsDTO>();
		results.add(movieDetailsDTO);

		MovieDetailsResponseDTO movieDetailsResponseDTO = new MovieDetailsResponseDTO();
		movieDetailsResponseDTO.setResults(results);

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));

		this.loadMoviesService.loadMovies().block();

		ArgumentCaptor<MovieDetailsResponseDTO> captor = ArgumentCaptor.forClass(MovieDetailsResponseDTO.class);
		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		movieDetailsDTO.setGenreIds(new ArrayList<Integer>());
		results = new ArrayList<MovieDetailsDTO>();
		results.add(movieDetailsDTO);

		movieDetailsResponseDTO = new MovieDetailsResponseDTO();
		movieDetailsResponseDTO.setResults(results);

		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

		this.loadMoviesService.loadMovies().block();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesWithoutMissingGenres() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		WebClient localWebClient = Mockito.mock(WebClient.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		MovieDetailsResponseDTO movieDetailsResponseDTO = this.getMovieDetailsResponseDTO();
		GenreResponseDTO genreResponseDTO = this.getGenreResponseDTO();

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.just(genreResponseDTO));

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(responseSpec).bodyToMono(GenreResponseDTO.class);

		localWebClient = Mockito.mock(WebClient.class);
		responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.just(genreResponseDTO));

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(responseSpec, never()).bodyToMono(GenreResponseDTO.class);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesGenreResponse() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		WebClient localWebClient = Mockito.mock(WebClient.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		MovieDetailsResponseDTO movieDetailsResponseDTO = this.getMovieDetailsResponseDTO();

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		ArgumentCaptor<MovieDetailsResponseDTO> captor = ArgumentCaptor.forClass(MovieDetailsResponseDTO.class);
		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		final ResponseSpec responseAnswerSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseAnswerSpec);
		Mockito.when(responseAnswerSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec)
				.thenAnswer(invocation -> {
					Predicate<HttpStatus> statusPredicate = invocation.getArgument(0);
					Function<ClientResponse, Mono<? extends Throwable>> errorFunction = invocation.getArgument(1);

					boolean matches = statusPredicate.test(HttpStatus.BAD_GATEWAY);

					ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
					Mockito.when(mockClientResponse.statusCode()).thenReturn(HttpStatus.BAD_GATEWAY);

					if (matches) {
						errorFunction.apply(mockClientResponse);
					}

					return responseAnswerSpec;
				});
		Mockito.when(responseAnswerSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseAnswerSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);
		final ResponseSpec responseAnswerSpecAccepted = Mockito.mock(ResponseSpec.class);

		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseAnswerSpecAccepted);
		Mockito.when(responseAnswerSpecAccepted.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec)
				.thenAnswer(invocation -> {
					Predicate<HttpStatus> statusPredicate = invocation.getArgument(0);
					Function<ClientResponse, Mono<? extends Throwable>> errorFunction = invocation.getArgument(1);

					boolean matches = statusPredicate.test(HttpStatus.ACCEPTED);

					ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
					Mockito.when(mockClientResponse.statusCode()).thenReturn(HttpStatus.ACCEPTED);

					if (matches) {
						errorFunction.apply(mockClientResponse);
					}

					return responseAnswerSpecAccepted;
				});
		Mockito.when(responseAnswerSpecAccepted.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseAnswerSpecAccepted.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.empty());

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		responseSpec = Mockito.mock(ResponseSpec.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);
		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class))
				.thenReturn(Mono.error(new RuntimeException("error calling the service")));

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesGenreResponseValidation() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		WebClient localWebClient = Mockito.mock(WebClient.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		MovieDetailsResponseDTO movieDetailsResponseDTO = this.getMovieDetailsResponseDTO();
		GenreResponseDTO genreResponseDTO = this.getGenreResponseDTO();
		genreResponseDTO.setGenres(null);

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.just(genreResponseDTO));

		this.loadMoviesService.loadMovies().block();

		ArgumentCaptor<MovieDetailsResponseDTO> captor = ArgumentCaptor.forClass(MovieDetailsResponseDTO.class);
		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

		saveMoviesService = Mockito.mock(SaveMoviesService.class);
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		genreResponseDTO.setGenres(new ArrayList<GenreDTO>());

		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class)).thenReturn(Mono.just(genreResponseDTO));

		this.loadMoviesService.loadMovies().block();

		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void loadMoviesExceptionGenres() {
		SaveMoviesService saveMoviesService = Mockito.mock(SaveMoviesService.class);
		WebClient localWebClient = Mockito.mock(WebClient.class);
		this.MockEnvProperties();
		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);

		MovieDetailsResponseDTO movieDetailsResponseDTO = this.getMovieDetailsResponseDTO();
		GenreResponseDTO genreResponseDTO = this.getGenreResponseDTO();
		genreResponseDTO.setGenres(null);

		RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RequestHeadersUriSpec.class);
		RequestHeadersSpec requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

		this.loadMoviesService = new LoadMoviesServiceImpl(localWebClient, saveMoviesService, this.env);
		Mockito.when(localWebClient.get()).thenReturn(requestHeadersUriSpec);
		Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
		Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
		Mockito.when(responseSpec.bodyToMono(MovieDetailsResponseDTO.class))
				.thenReturn(Mono.just(movieDetailsResponseDTO));
		Mockito.when(responseSpec.bodyToMono(GenreResponseDTO.class))
				.thenReturn(Mono.error(new RuntimeException("error calling the service")));

		this.loadMoviesService.loadMovies().block();

		ArgumentCaptor<MovieDetailsResponseDTO> captor = ArgumentCaptor.forClass(MovieDetailsResponseDTO.class);
		Mockito.verify(saveMoviesService).saveMovieList(captor.capture());

		movieDetailsResponseDTO = captor.getValue();

		assertEquals(1, movieDetailsResponseDTO.getResults().size());
		assertEquals(0, movieDetailsResponseDTO.getResults().getFirst().getGenres().size());

	}

	private void MockEnvProperties() {
		Mockito.when(this.env.getProperty("api.movie.baseUrl", "")).thenReturn("baseUrl");
		Mockito.when(this.env.getProperty("api.movie.discover.path", "")).thenReturn("discover.path");
		Mockito.when(this.env.getProperty("api.movie.apiKey", "")).thenReturn("apiKey");
		Mockito.when(this.env.getProperty("api.movie.genre.path", "")).thenReturn("genre.path");
	}

	private MovieDetailsResponseDTO getMovieDetailsResponseDTO() {
		List<Integer> genreIds = new ArrayList<Integer>();
		genreIds.add(1);
		genreIds.add(2);

		MovieDetailsDTO movieDetailsDTO = new MovieDetailsDTO();
		movieDetailsDTO.setId(1L);
		movieDetailsDTO.setGenreIds(genreIds);

		List<MovieDetailsDTO> results = new ArrayList<MovieDetailsDTO>();
		results.add(movieDetailsDTO);

		MovieDetailsResponseDTO movieDetailsResponseDTO = new MovieDetailsResponseDTO();
		movieDetailsResponseDTO.setResults(results);

		return movieDetailsResponseDTO;
	}

	private GenreResponseDTO getGenreResponseDTO() {
		GenreDTO genreDTO1 = new GenreDTO();
		genreDTO1.setId(1);
		genreDTO1.setName("genre1");

		GenreDTO genreDTO2 = new GenreDTO();
		genreDTO2.setId(2);
		genreDTO2.setName("genre2");

		List<GenreDTO> genres = new ArrayList<GenreDTO>();
		genres.add(genreDTO1);
		genres.add(genreDTO2);

		GenreResponseDTO genreResponseDTO = new GenreResponseDTO();
		genreResponseDTO.setGenres(genres);

		return genreResponseDTO;
	}
}
