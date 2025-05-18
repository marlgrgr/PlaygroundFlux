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
import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.repository.MovieRepository;

public class MovieControllerIT extends AbstractIntegrationBase {

	@Autowired
	private MovieRepository movieRepository;

	@BeforeAll
	private void setUpMongoRepository() {
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
	}

	@Test
	void movieIT() throws Exception {

		webTestClient.post().uri("/api/v1/movie/load").header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isAccepted();

		PagedResponse<MovieDTO> pagedResponse = webTestClient.get().uri("/api/v1/movie")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.getToken()).exchange()
				.expectStatus().isOk().expectBody(new ParameterizedTypeReference<PagedResponse<MovieDTO>>() {
				}).returnResult().getResponseBody();

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		assertEquals(1L, pagedResponse.getTotalResults());
		assertEquals("tittle", pagedResponse.getResults().getFirst().getTitle());

		MovieDTO movieDTO = webTestClient.get().uri("/api/v1/movie/" + idFirstResult).accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.getToken()).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectBody(MovieDTO.class).returnResult().getResponseBody();

		assertEquals("tittle", movieDTO.getTitle());
	}

}
