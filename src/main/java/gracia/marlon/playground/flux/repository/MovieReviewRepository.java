package gracia.marlon.playground.flux.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import gracia.marlon.playground.flux.model.MovieReview;

@Repository
public interface MovieReviewRepository extends ReactiveCrudRepository<MovieReview, String> {

}
