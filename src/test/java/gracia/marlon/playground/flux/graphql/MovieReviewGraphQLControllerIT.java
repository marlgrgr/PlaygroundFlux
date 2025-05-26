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
import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.repository.MovieRepository;
import gracia.marlon.playground.flux.repository.MovieReviewRepository;
import gracia.marlon.playground.flux.services.CacheService;

public class MovieReviewGraphQLControllerIT extends AbstractIntegrationBase {

	@Autowired
	private ObjectMapper objectMapper;

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

	@SuppressWarnings("unchecked")
	@Test
	void movieReviewIT() throws Exception {

		String queryStr = """
				query GetMovieReviewResponseDTO {
				    getMovieReviewResponseDTO {
				        page
				        totalPages
				        totalResults
				        results {
				            id
				            review
				            score
				            movieId
				            createOn
				        }
				    }
				}
				""";

		String query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		Map<String, Object> responseMap = webTestClient.post().uri("/graphql")
				.header("Authorization", "Bearer " + this.getToken()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		Map<String, Object> responseData = (Map<String, Object>) responseMap.get("data");
		PagedResponse<MovieReviewDTO> pagedResponse = objectMapper.convertValue(
				responseData.get("getMovieReviewResponseDTO"), new TypeReference<PagedResponse<MovieReviewDTO>>() {
				});

		assertEquals(0, pagedResponse.getTotalResults());

		String mutation = """
				mutation CreateMovieReview {
				    createMovieReview(
				        movieReviewDTO: { movieId: 1, score: 0.0, review: "review", id: "id" }
				    )
				}
				""";

		query = "{\"query\": \"" + mutation.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		Boolean responseBoolean = (Boolean) responseData.get("createMovieReview");
		assertEquals(true, responseBoolean);

		queryStr = """
				query GetMovieReviewResponseDTO {
				    getMovieReviewResponseDTO {
				        page
				        totalPages
				        totalResults
				    }
				}
				""";

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		pagedResponse = objectMapper.convertValue(responseData.get("getMovieReviewResponseDTO"),
				new TypeReference<PagedResponse<MovieReviewDTO>>() {
				});

		assertEquals(1, pagedResponse.getTotalResults());

		queryStr = """
				query GetMovieReviewResponseDTOByID {
				    getMovieReviewResponseDTOByID(movieReviewId: "id") {
				        review
				    }
				}
				""";

		query = "{\"query\": \"" + queryStr.replace("\"", "\\\"").replace("\t", " ").replace("\n", "") + "\"}";

		responseMap = webTestClient.post().uri("/graphql").header("Authorization", "Bearer " + this.getToken())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(query).exchange().expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
				}).returnResult().getResponseBody();

		responseData = (Map<String, Object>) responseMap.get("data");
		MovieReviewDTO movieReviewDTOResponse = objectMapper
				.convertValue(responseData.get("getMovieReviewResponseDTOByID"), MovieReviewDTO.class);
		assertEquals("review", movieReviewDTOResponse.getReview());

		queryStr = """
				query GetMovieReviewResponseDTOByMovieID {
				    getMovieReviewResponseDTOByMovieID(movieId: "1") {
				        page
				        totalPages
				        totalResults
				        results {
				            review
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
		pagedResponse = objectMapper.convertValue(responseData.get("getMovieReviewResponseDTOByMovieID"),
				new TypeReference<PagedResponse<MovieReviewDTO>>() {
				});

		assertEquals(1, pagedResponse.getTotalResults());
		assertEquals("review", pagedResponse.getResults().getFirst().getReview());
	}
}
