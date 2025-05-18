package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.mapper.MovieMapper;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.queue.QueuePublisherService;
import gracia.marlon.playground.flux.repository.MovieRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MovieServiceTest {

	private final CacheService cacheService;

	private final RedissonReactiveClient redissonClient;

	private final QueuePublisherService queuePublisherService;

	private final MovieRepository movieRepository;

	private final ReactiveMongoTemplate mongoTemplate;

	private final MovieMapper movieMapper;

	private final Environment env;

	private final MovieService movieService;

	private final String STATUS_NOT_AVAILABLE = "NOT_AVAILABLE";

	public MovieServiceTest() {
		this.cacheService = Mockito.mock(CacheService.class);
		this.redissonClient = Mockito.mock(RedissonReactiveClient.class);
		this.queuePublisherService = Mockito.mock(QueuePublisherService.class);
		this.movieRepository = Mockito.mock(MovieRepository.class);
		this.mongoTemplate = Mockito.mock(ReactiveMongoTemplate.class);
		this.movieMapper = Mockito.mock(MovieMapper.class);
		this.env = Mockito.mock(Environment.class);
		Mockito.when(this.env.getProperty("sqs.queue.loadMovies.name", "")).thenReturn("loadMovies.name");
		this.movieService = new MovieServiceImpl(this.cacheService, this.redissonClient, this.queuePublisherService,
				this.movieRepository, this.mongoTemplate, this.movieMapper, this.env);
	}

	@Test
	public void requestForloadMoviesSuccessful() {
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		Mockito.when(this.cacheService.getFromSpecialCache(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(this.redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.just(true));
		Mockito.when(this.queuePublisherService.publish(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Mono.empty());
		Mockito.when(
				this.cacheService.putInSpecialCacheWithTTL(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(Mono.empty());

		this.movieService.requestForloadMovies().block();

		Mockito.verify(this.cacheService).putInSpecialCacheWithTTL(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyInt());
	}

	@Test
	public void requestForloadMoviesSuccessfulWithAvailable() {
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		Mockito.when(this.cacheService.getFromSpecialCache(Mockito.anyString())).thenReturn(Mono.just("AVAILABLE"));
		Mockito.when(this.redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.just(true));
		Mockito.when(this.queuePublisherService.publish(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Mono.empty());
		Mockito.when(
				this.cacheService.putInSpecialCacheWithTTL(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(Mono.empty());

		this.movieService.requestForloadMovies().block();

		Mockito.verify(this.cacheService).putInSpecialCacheWithTTL(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyInt());
	}

	@Test
	public void requestForloadMoviesNotComplete() {
		Mockito.when(this.cacheService.getFromSpecialCache(Mockito.anyString()))
				.thenReturn(Mono.just(STATUS_NOT_AVAILABLE));
		this.movieService.requestForloadMovies().block();
		Mockito.verify(this.cacheService, Mockito.never()).putInSpecialCacheWithTTL(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());

		RLockReactive lock = Mockito.mock(RLockReactive.class);
		Mockito.when(this.cacheService.getFromSpecialCache(Mockito.anyString())).thenReturn(Mono.empty())
				.thenReturn(Mono.just(STATUS_NOT_AVAILABLE));
		Mockito.when(this.redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.just(true));

		this.movieService.requestForloadMovies().block();

		Mockito.verify(this.cacheService, Mockito.never()).putInSpecialCacheWithTTL(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());
	}

	@Test
	public void requestForloadMoviesException() {
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		Mockito.when(this.cacheService.getFromSpecialCache(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(this.redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.error(new RuntimeException("lock exception")));

		this.movieService.requestForloadMovies().block();

		Mockito.verify(this.cacheService, Mockito.never()).putInSpecialCacheWithTTL(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());
	}

	@Test
	public void requestForloadMoviesNotLocked() {
		RLockReactive lock = Mockito.mock(RLockReactive.class);

		Mockito.when(this.cacheService.getFromSpecialCache(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(this.redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
		Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Mono.just(false));

		this.movieService.requestForloadMovies().block();

		Mockito.verify(this.cacheService, Mockito.never()).putInSpecialCacheWithTTL(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());
	}

	@Test
	public void getMovieListSuccessful() {
		Movie movie = new Movie();
		movie.setId(1L);
		List<Movie> content = new ArrayList<Movie>();
		content.add(movie);

		Flux<Movie> fluxMovie = Flux.fromIterable(content);

		MovieDTO movieDTO = new MovieDTO();
		movieDTO.setId(1L);

		Mockito.when(this.mongoTemplate.find(Mockito.any(), Mockito.eq(Movie.class))).thenReturn(fluxMovie);
		Mockito.when(this.mongoTemplate.count(Mockito.any(), Mockito.eq(Movie.class))).thenReturn(Mono.just(1L));
		Mockito.when(this.movieMapper.toDto(Mockito.any())).thenReturn(movieDTO);

		PagedResponse<MovieDTO> pagedResponse = this.movieService.getMovieList(1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals(1L, pagedResponse.getResults().getFirst().getId());
	}

	@Test
	public void getMovieByIdSuccessful() {
		Movie movie = new Movie();
		movie.setId(1L);

		MovieDTO movieDTO = new MovieDTO();
		movieDTO.setId(1L);

		Mockito.when(this.movieRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(movie));
		Mockito.when(this.movieMapper.toDto(Mockito.any())).thenReturn(movieDTO);

		MovieDTO response = this.movieService.getMovieById(1L).block();

		assertEquals(1L, response.getId());
	}

	@Test
	public void getMovieByIdNotExisting() {
		RestException restException = assertThrows(RestException.class,
				() -> this.movieService.getMovieById(null).block());
		assertEquals("MOVIE-0005", restException.getError().getCode());

		Mockito.when(this.movieRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());

		restException = assertThrows(RestException.class, () -> this.movieService.getMovieById(1L).block());
		assertEquals("MOVIE-0005", restException.getError().getCode());
	}

}
