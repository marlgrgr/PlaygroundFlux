package gracia.marlon.playground.flux.graphql;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.repository.MovieRepository;

public class MovieGraphQLControllerIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

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

	@SuppressWarnings("unchecked")
	@Test
	void movieIT() throws Exception {
		String mutation = """
				mutation LoadMovies {
				    loadMovies
				}
				""";

		String query = "{\"query\": \"" + mutation.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		Map<String, Object> responseMap = webTestClient.post().uri("/graphql")
				.header("Authorization", "Bearer " + this.getToken()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		Map<String, Object> responseData = (Map<String, Object>) responseMap.get("data");
		Boolean responseBoolean = (Boolean) responseData.get("loadMovies");
		assertEquals(true, responseBoolean);

		String queryStr = """
				query GetMovieDetailsResponseDTO {
				    getMovieDetailsResponseDTO {
				        page
				        totalPages
				        totalResults
				        results {
				            id
				            title
				        }
				    }
				}
				""";

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		PagedResponse<MovieDTO> pagedResponse = objectMapper.convertValue(
				responseData.get("getMovieDetailsResponseDTO"), new TypeReference<PagedResponse<MovieDTO>>() {
				});

		Long idFirstResult = pagedResponse.getResults().getFirst().getId();

		assertEquals(1L, pagedResponse.getTotalResults());
		assertEquals("tittle", pagedResponse.getResults().getFirst().getTitle());

		queryStr = String.format("""
				query GetMovieById {
				    getMovieById(movieId: \"%s\" ) {
				        id
				        title
				    }
				}
				""", idFirstResult);

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		MovieDTO movieDTO = objectMapper.convertValue(responseData.get("getMovieById"), MovieDTO.class);
		assertEquals("tittle", movieDTO.getTitle());
	}

}
