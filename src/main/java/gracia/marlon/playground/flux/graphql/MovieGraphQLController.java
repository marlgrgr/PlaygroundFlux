package gracia.marlon.playground.flux.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.services.MovieService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class MovieGraphQLController {

	private final MovieService movieService;

	@MutationMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<Boolean> loadMovies() {
		return this.movieService.requestForloadMovies().thenReturn(true);
	}

	@QueryMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<PagedResponse<MovieDTO>> getMovieDetailsResponseDTO(@Argument Integer page,
			@Argument Integer pageSize) {
		return this.movieService.getMovieList(page, pageSize);
	}

	@QueryMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public Mono<MovieDTO> getMovieById(@Argument Long movieId) {
		return this.movieService.getMovieById(movieId);
	}
}