package gracia.marlon.playground.flux.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.exception.ApiError;
import gracia.marlon.playground.flux.services.MovieService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Operations to load the movies and retrieve them")
@SecurityRequirement(name = "bearerAuth")
public class MovieController {

	private final MovieService movieService;

	@PostMapping("/movie/load")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Request accepted"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<Void> loadMovies() {
		return this.movieService.requestForloadMovies();
	}

	@GetMapping("/movie")
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve movies"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<PagedResponse<MovieDTO>> getMovieDetailsResponseDTO(
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		final Mono<PagedResponse<MovieDTO>> pagedResponse = this.movieService.getMovieList(page, pageSize);
		return pagedResponse;
	}

	@GetMapping("/movie/{movieId}")
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve movie"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "404", description = "Movie not found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<MovieDTO> getMovieById(@PathVariable Long movieId) {
		final Mono<MovieDTO> movieDTO = this.movieService.getMovieById(movieId);
		return movieDTO;
	}
}
