package gracia.marlon.playground.flux.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.model.MovieReview;
import gracia.marlon.playground.flux.repository.MovieRepository;
import gracia.marlon.playground.flux.repository.MovieReviewRepository;
import gracia.marlon.playground.flux.services.CacheService;

public class MovieReviewControllerIT extends AbstractIntegrationBase {
	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private MovieReviewRepository movieReviewRepository;

	@Autowired
	private CacheService cacheService;

	@BeforeAll
	private void setUpMongoRepository() {
		this.movieReviewRepository.deleteAll().block();
		this.movieRepository.deleteAll().block();

		List<Integer> genreIdsList = new ArrayList<Integer>();
		List<String> genresList = new ArrayList<String>();

		Movie movie = new Movie();
		movie.setCreateOn(new Date());
		movie.setGenreIds(genreIdsList);
		movie.setGenres(genresList);
		movie.setId(1L);
		movie.setOriginalLanguage("originalLanguage");
		movie.setOriginalTitle("originalTitle");
		movie.setOverview("overview");
		movie.setPopularity(0.0);
		movie.setPosterPath("posterPath");
		movie.setReleaseDate("releaseDate");
		movie.setTitle("tittle");
		movie.setVoteAverage(0.0);
		movie.setVoteCount(0.0);

		this.movieRepository.save(movie).block();
		this.cacheService.evictCache("MovieReviewService_getMovieReviews").block();
		this.cacheService.evictCache("MovieReviewService_getMovieReviewsByMovie").block();
		this.cacheService.evictCache("MovieReviewService_getMovieReviewByID").block();
		this.cacheService.evictCache("Flux_MovieReviewService_getMovieReviews").block();
		this.cacheService.evictCache("Flux_MovieReviewService_getMovieReviewsByMovie").block();
		this.cacheService.evictCache("Flux_MovieReviewService_getMovieReviewByID").block();
	}

	@Test
	void movieReviewIT() throws Exception {

		PagedResponse<MovieReviewDTO> pagedResponse = webTestClient.get().uri("/api/v1/movieReview")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<MovieReviewDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(0, pagedResponse.getTotalResults());

		MovieReview movieReview = new MovieReview();
		movieReview.setCreateOn(new Date());
		movieReview.setId("id");
		movieReview.setMovieId(1L);
		movieReview.setReview("review");
		movieReview.setScore(0.0);

		webTestClient.post().uri("/api/v1/movieReview").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).bodyValue(movieReview).exchange().expectStatus()
				.isCreated();

		pagedResponse = webTestClient.get().uri("/api/v1/movieReview").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<MovieReviewDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(1, pagedResponse.getTotalResults());

		MovieReviewDTO movieReviewDTOResponse = webTestClient.get().uri("/api/v1/movieReview/id")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken())
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectBody(MovieReviewDTO.class)
				.returnResult().getResponseBody();

		assertEquals("review", movieReviewDTOResponse.getReview());

		pagedResponse = webTestClient.get().uri("/api/v1/movieReview/movie/1").accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<PagedResponse<MovieReviewDTO>>() {
				}).returnResult().getResponseBody();

		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals("review", pagedResponse.getResults().getFirst().getReview());
	}
}
