package gracia.marlon.playground.flux.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.services.MovieReviewService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class MovieReviewGraphQLController {

	private final MovieReviewService movieReviewService;

	@QueryMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<PagedResponse<MovieReviewDTO>> getMovieReviewResponseDTO(@Argument Integer page,
			@Argument Integer pageSize) {
		return this.movieReviewService.getMovieReviews(page, pageSize);
	}

	@QueryMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<MovieReviewDTO> getMovieReviewResponseDTOByID(@Argument String movieReviewId) {
		return this.movieReviewService.getMovieReviewByID(movieReviewId);
	}

	@QueryMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<PagedResponse<MovieReviewDTO>> getMovieReviewResponseDTOByMovieID(@Argument Long movieId,
			@Argument Integer page, @Argument Integer pageSize) {
		return this.movieReviewService.getMovieReviewsByMovie(movieId, page, pageSize);
	}

	@MutationMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<Boolean> createMovieReview(@Argument("movieReviewDTO") MovieReviewDTO movieReviewDTO) {
		return this.movieReviewService.saveReview(movieReviewDTO).thenReturn(true);
	}

}