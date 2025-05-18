package gracia.marlon.playground.flux.services;

import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import gracia.marlon.playground.flux.cache.ReactiveCacheable;
import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.dtos.PageDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.mapper.MovieReviewMapper;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.model.MovieReview;
import gracia.marlon.playground.flux.repository.MovieRepository;
import gracia.marlon.playground.flux.repository.MovieReviewRepository;
import gracia.marlon.playground.flux.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MovieReviewServiceImpl implements MovieReviewService {

	private final MovieReviewRepository movieReviewRepository;

	private final MovieRepository movieRepository;

	private final CacheService cacheService;

	private final ReactiveMongoTemplate mongoTemplate;

	private final MovieReviewMapper movieReviewMapper;

	private final double MIN_SCORE = 0;

	private final double MAX_SCORE = 5;

	@Override
	@ReactiveCacheable(cacheName = "Flux_MovieReviewService_getMovieReviews", key = "#page + '-' + #pageSize")
	public Mono<PagedResponse<MovieReviewDTO>> getMovieReviews(Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Query query = new Query().with(Sort.by(Sort.Order.desc("createOn"))).skip(pageDTO.getOffset())
				.limit(pageDTO.getPageSize());

		Flux<MovieReview> movieReviewList = this.mongoTemplate.find(query, MovieReview.class);
		Mono<Long> totalMovieReviewList = this.mongoTemplate.count(new Query(), MovieReview.class);

		return movieReviewList.collectList()
				.flatMap(movieReview -> totalMovieReviewList.map(total -> PageableUtil.getPagedResponse(pageDTO, total,
						movieReview.stream().map(movieReviewMapper::toDto).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_MovieReviewService_getMovieReviewsByMovie", key = "#movieId + '-' + #page + '-' + #pageSize")
	public Mono<PagedResponse<MovieReviewDTO>> getMovieReviewsByMovie(Long movieId, Integer page, Integer pageSize) {
		PageDTO pageDTO = PageableUtil.getPageable(page, pageSize);

		Query query = new Query().addCriteria(Criteria.where("movieId").is(movieId))
				.with(Sort.by(Sort.Order.desc("createOn"))).skip(pageDTO.getOffset()).limit(pageDTO.getPageSize());

		Flux<MovieReview> movieReviewList = this.mongoTemplate.find(query, MovieReview.class);
		Mono<Long> totalMovieReviewList = this.mongoTemplate.count(new Query(), MovieReview.class);

		return movieReviewList.collectList()
				.flatMap(movieReview -> totalMovieReviewList.map(total -> PageableUtil.getPagedResponse(pageDTO, total,
						movieReview.stream().map(movieReviewMapper::toDto).collect(Collectors.toList()))));
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_MovieReviewService_getMovieReviewByID", key = "#id")
	public Mono<MovieReviewDTO> getMovieReviewByID(String id) {
		final Mono<MovieReview> movieReviewOpt = this.movieReviewRepository.findById(id);

		return movieReviewOpt.map(movieReview -> this.movieReviewMapper.toDto(movieReview)).switchIfEmpty(
				Mono.error(new RestException("The movie review doesn't exist", "MOVIE-0006", HttpStatus.NOT_FOUND)));
	}

	@Override
	public Mono<Void> saveReview(MovieReviewDTO movieReviewDTO) {
		return this.validateMovieReviewDTO(movieReviewDTO)
				.then(Mono.defer(() -> {
					movieReviewDTO.setCreateOn(new Date());
					return this.movieReviewRepository.save(this.movieReviewMapper.toEntity(movieReviewDTO));
				})).then(Mono.defer(() -> this.evictMovieReviewCaches()));
	}

	private Mono<Void> validateMovieReviewDTO(MovieReviewDTO movieReviewDTO) {

		if (movieReviewDTO == null) {
			return Mono.error(new RestException("No movie review sent", "MOVIE-0001", HttpStatus.BAD_REQUEST));
		}

		if (movieReviewDTO.getMovieId() == null) {
			return Mono.error(new RestException("No movie sent", "MOVIE-0002", HttpStatus.BAD_REQUEST));
		}

		if (movieReviewDTO.getReview() == null || movieReviewDTO.getReview().trim().isEmpty()) {
			return Mono.error(new RestException("The review can not be empty", "MOVIE-0003", HttpStatus.BAD_REQUEST));
		}

		if (movieReviewDTO.getScore() == null || movieReviewDTO.getScore() < MIN_SCORE
				|| movieReviewDTO.getScore() > MAX_SCORE) {
			return Mono.error(new RestException("The score is not valid", "MOVIE-0004", HttpStatus.BAD_REQUEST));
		}

		final Mono<Movie> movie = this.movieRepository.findById(movieReviewDTO.getMovieId());

		return movie
				.switchIfEmpty(
						Mono.error(new RestException("The movie doesn't exist", "MOVIE-0005", HttpStatus.NOT_FOUND)))
				.then();

	}

	private Mono<Void> evictMovieReviewCaches() {
		return Mono.when(this.cacheService.evictCache("Flux_MovieReviewService_getMovieReviews"),
				this.cacheService.evictCache("Flux_MovieReviewService_getMovieReviewsByMovie"),
				this.cacheService.evictCache("Flux_MovieReviewService_getMovieReviewByID"),
				this.cacheService.evictCache("MovieReviewService_getMovieReviews"),
				this.cacheService.evictCache("MovieReviewService_getMovieReviewsByMovie"),
				this.cacheService.evictCache("MovieReviewService_getMovieReviewByID"));
	}

}
