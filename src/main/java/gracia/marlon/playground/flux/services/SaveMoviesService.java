package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.MovieDetailsResponseDTO;
import reactor.core.publisher.Mono;

public interface SaveMoviesService {

	Mono<Void> saveMovieList(MovieDetailsResponseDTO movieDetailsResponseDTO);

}
