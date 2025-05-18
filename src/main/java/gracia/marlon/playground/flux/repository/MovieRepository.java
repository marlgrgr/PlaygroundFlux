package gracia.marlon.playground.flux.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import gracia.marlon.playground.flux.model.Movie;

@Repository
public interface MovieRepository extends ReactiveCrudRepository<Movie, Long> {

}
