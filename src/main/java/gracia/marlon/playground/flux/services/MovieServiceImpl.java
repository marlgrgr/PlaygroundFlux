package gracia.marlon.playground.flux.services;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import gracia.marlon.playground.flux.cache.ReactiveCacheable;
import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PageDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.mapper.MovieMapper;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.queue.QueuePublisherService;
import gracia.marlon.playground.flux.repository.MovieRepository;
import gracia.marlon.playground.flux.util.PageableUtil;
import gracia.marlon.playground.flux.util.SharedConstants;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MovieServiceImpl implements MovieService {

	private final CacheService cacheService;

	private final RedissonReactiveClient redisson;

	private final QueuePublisherService queuePublisherService;

	private final MovieRepository movieRepository;

	private final ReactiveMongoTemplate mongoTemplate;

	private final MovieMapper movieMapper;

	private final String LOAD_KEY = "LOAD_MOVIE_STATUS";

	private final String STATUS_NOT_AVAILABLE = "NOT_AVAILABLE";

	private final String LOAD_MOVIE_LOCK = "load-movie-lock";

	private final String TASK_DONE = "TASK_DONE";

	private final String loadMoviesQueueName;

	private final int MAX_WAIT_FOR_LOCK_TIME = 10;

	private final int MAX_TIME_WITH_LOCK = 60;

	private final int TTL_SPECIAL_CACHE = 60;

	public MovieServiceImpl(CacheService cacheService, RedissonReactiveClient redisson,
			QueuePublisherService queuePublisherService, MovieRepository movieRepository,
			ReactiveMongoTemplate mongoTemplate, MovieMapper movieMapper, Environment env) {
		this.cacheService = cacheService;
		this.redisson = redisson;
		this.queuePublisherService = queuePublisherService;
		this.movieRepository = movieRepository;
		this.mongoTemplate = mongoTemplate;
		this.movieMapper = movieMapper;
		this.loadMoviesQueueName = env.getProperty("sqs.queue.loadMovies.name", "");
	}

	@Override
	public Mono<Void> requestForloadMovies() {
		return cacheService.getFromSpecialCache(LOAD_KEY).flatMap(value -> {
			if (value.equals(STATUS_NOT_AVAILABLE)) {
				return Mono.just(STATUS_NOT_AVAILABLE);
			}
			return reviewLock().thenReturn(Mono.just(TASK_DONE));
		}).switchIfEmpty(Mono.defer(() -> {
			return reviewLock();
		})).then();
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_MovieService_getMovieList", key = "#page + '-' + #pageSize")
	public Mono<PagedResponse<MovieDTO>> getMovieList(Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Query query = new Query().with(Sort.by(Sort.Order.desc("createOn"), Sort.Order.desc("popularity")))
				.skip(pageDTO.getOffset()).limit(pageDTO.getPageSize());

		Flux<Movie> movieList = this.mongoTemplate.find(query, Movie.class);
		Mono<Long> totalMovieList = this.mongoTemplate.count(new Query(), Movie.class);

		return movieList.collectList()
				.flatMap(movie -> totalMovieList.map(total -> PageableUtil.getPagedResponse(pageDTO, total,
						movie.stream().map(movieMapper::toDto).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_MovieService_getMovieById", key = "#movieId")
	public Mono<MovieDTO> getMovieById(Long movieId) {
		if (movieId == null) {
			return Mono.error(new RestException("The movie doesn't exist", "MOVIE-0005", HttpStatus.NOT_FOUND));
		}

		final Mono<Movie> movieOpt = this.movieRepository.findById(movieId);

		return movieOpt.map(movie -> this.movieMapper.toDto(movie)).switchIfEmpty(
				Mono.error(new RestException("The movie doesn't exist", "MOVIE-0005", HttpStatus.NOT_FOUND)));
	}

	private Mono<Void> reviewLock() {

		RLockReactive lock = redisson.getLock(LOAD_MOVIE_LOCK);
		return lock.tryLock(MAX_WAIT_FOR_LOCK_TIME, MAX_TIME_WITH_LOCK, TimeUnit.SECONDS).flatMap(locked -> {
			if (locked) {
				return cacheService.getFromSpecialCache(LOAD_KEY).flatMap(value -> {
					if (value.equals(STATUS_NOT_AVAILABLE)) {
						return Mono.just(STATUS_NOT_AVAILABLE);
					}
					return sentLoadRequest().thenReturn(Mono.just(TASK_DONE));
				}).switchIfEmpty(Mono.defer(() -> {
					return sentLoadRequest();
				}));
			}

			return Mono.empty();
		}).then().onErrorResume(error -> {
			log.error("An error occurred creating the request for movies to be load", error);
			return Mono.empty();
		});

	}

	private Mono<Void> sentLoadRequest() {
		return this.queuePublisherService.publish(SharedConstants.LOAD_MESSAGE, this.loadMoviesQueueName)
				.then(this.cacheService.putInSpecialCacheWithTTL(LOAD_KEY, STATUS_NOT_AVAILABLE, TTL_SPECIAL_CACHE));

	}

}
