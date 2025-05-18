package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import reactor.core.publisher.Mono;

public interface MovieService {

	Mono<Void> requestForloadMovies();

	Mono<PagedResponse<MovieDTO>> getMovieList(Integer page, Integer pageSize);

	Mono<MovieDTO> getMovieById(Long movieId);

}
