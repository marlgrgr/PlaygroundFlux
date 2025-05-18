package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.mapper.MovieReviewMapper;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.model.MovieReview;
import gracia.marlon.playground.flux.repository.MovieRepository;
import gracia.marlon.playground.flux.repository.MovieReviewRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MovieReviewServiceTest {

	private final MovieReviewRepository movieReviewRepository;

	private final MovieRepository movieRepository;

	private final CacheService cacheService;

	private final ReactiveMongoTemplate mongoTemplate;

	private final MovieReviewMapper movieReviewMapper;

	private final MovieReviewService movieReviewService;

	public MovieReviewServiceTest() {
		this.movieReviewRepository = Mockito.mock(MovieReviewRepository.class);
		this.movieRepository = Mockito.mock(MovieRepository.class);
		this.cacheService = Mockito.mock(CacheService.class);
		this.mongoTemplate = Mockito.mock(ReactiveMongoTemplate.class);
		this.movieReviewMapper = Mockito.mock(MovieReviewMapper.class);
		this.movieReviewService = new MovieReviewServiceImpl(this.movieReviewRepository, this.movieRepository,
				this.cacheService, this.mongoTemplate, this.movieReviewMapper);
	}

	@Test
	public void getMovieReviewsSuccessful() {
		MovieReview movieReview = new MovieReview();
		movieReview.setId("_id1");
		List<MovieReview> content = new ArrayList<MovieReview>();
		content.add(movieReview);

		Flux<MovieReview> fluxMovieReview = Flux.fromIterable(content);

		MovieReviewDTO movieReviewDTO = new MovieReviewDTO();
		movieReviewDTO.setId("_id1");

		Mockito.when(this.mongoTemplate.find(Mockito.any(), Mockito.eq(MovieReview.class))).thenReturn(fluxMovieReview);
		Mockito.when(this.mongoTemplate.count(Mockito.any(), Mockito.eq(MovieReview.class))).thenReturn(Mono.just(1L));
		Mockito.when(this.movieReviewMapper.toDto(Mockito.any())).thenReturn(movieReviewDTO);

		PagedResponse<MovieReviewDTO> pagedResponse = this.movieReviewService.getMovieReviews(1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals("_id1", pagedResponse.getResults().getFirst().getId());
	}

	@Test
	public void getMovieReviewsByMovieSuccessful() {

		MovieReview movieReview = new MovieReview();
		movieReview.setId("_id1");
		List<MovieReview> content = new ArrayList<MovieReview>();
		content.add(movieReview);

		Flux<MovieReview> fluxMovieReview = Flux.fromIterable(content);

		MovieReviewDTO movieReviewDTO = new MovieReviewDTO();
		movieReviewDTO.setId("_id1");

		Mockito.when(this.mongoTemplate.find(Mockito.any(), Mockito.eq(MovieReview.class))).thenReturn(fluxMovieReview);
		Mockito.when(this.mongoTemplate.count(Mockito.any(), Mockito.eq(MovieReview.class))).thenReturn(Mono.just(1L));
		Mockito.when(this.movieReviewMapper.toDto(Mockito.any())).thenReturn(movieReviewDTO);

		PagedResponse<MovieReviewDTO> pagedResponse = this.movieReviewService.getMovieReviewsByMovie(1L, 1, 10).block();

		assertEquals(1, pagedResponse.getPage());
		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals("_id1", pagedResponse.getResults().getFirst().getId());
	}

	@Test
	public void getMovieReviewByIDSuccessful() {
		MovieReview movieReview = new MovieReview();
		movieReview.setId("_id1");

		MovieReviewDTO movieReviewDTO = new MovieReviewDTO();
		movieReviewDTO.setId("_id1");

		Mockito.when(this.movieReviewRepository.findById(Mockito.anyString())).thenReturn(Mono.just(movieReview));
		Mockito.when(this.movieReviewMapper.toDto(Mockito.any())).thenReturn(movieReviewDTO);

		MovieReviewDTO movieReviewDTOResponse = this.movieReviewService.getMovieReviewByID("_id1").block();

		assertEquals("_id1", movieReviewDTOResponse.getId());
	}

	@Test
	public void getMovieReviewByIDEmpty() {
		Mockito.when(this.movieReviewRepository.findById(Mockito.anyString())).thenReturn(Mono.empty());

		RestException restException = assertThrows(RestException.class,
				() -> this.movieReviewService.getMovieReviewByID("_id1").block());

		assertEquals("MOVIE-0006", restException.getError().getCode());
	}

	@Test
	public void saveReviewSuccessful() {
		MovieReviewDTO movieReviewDTO = new MovieReviewDTO();
		movieReviewDTO.setId("_id1");
		movieReviewDTO.setMovieId(1L);
		movieReviewDTO.setReview("was good");
		movieReviewDTO.setScore(4.5);

		MovieReview movieReview = new MovieReview();
		Movie movie = new Movie();

		Mockito.when(this.movieReviewMapper.toEntity(Mockito.any())).thenReturn(movieReview);
		Mockito.when(this.movieRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(movie));
		Mockito.when(this.movieReviewRepository.save(Mockito.any())).thenReturn(Mono.empty());
		Mockito.when(this.cacheService.evictCache(Mockito.anyString())).thenReturn(Mono.empty());

		this.movieReviewService.saveReview(movieReviewDTO).block();

		Mockito.verify(this.cacheService, times(6)).evictCache(Mockito.anyString());

	}

	@Test
	public void saveReviewValidations() {
		RestException restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(null).block());
		assertEquals("MOVIE-0001", restException.getError().getCode());

		MovieReviewDTO movieReviewDTO = new MovieReviewDTO();
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0002", restException.getError().getCode());

		movieReviewDTO.setMovieId(1L);
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0003", restException.getError().getCode());

		movieReviewDTO.setReview("");
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0003", restException.getError().getCode());

		movieReviewDTO.setReview("good movie");
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0004", restException.getError().getCode());

		movieReviewDTO.setScore(-1.0);
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0004", restException.getError().getCode());

		movieReviewDTO.setScore(6.0);
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0004", restException.getError().getCode());

		movieReviewDTO.setScore(4.5);
		Mockito.when(this.movieRepository.findById(Mockito.anyLong())).thenReturn(Mono.empty());
		restException = assertThrows(RestException.class,
				() -> this.movieReviewService.saveReview(movieReviewDTO).block());
		assertEquals("MOVIE-0005", restException.getError().getCode());

	}

}
