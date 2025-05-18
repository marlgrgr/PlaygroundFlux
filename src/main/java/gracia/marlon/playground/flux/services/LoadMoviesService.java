package gracia.marlon.playground.flux.services;

import reactor.core.publisher.Mono;

public interface LoadMoviesService {

	Mono<Void> loadMovies();

}
