package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import reactor.core.publisher.Mono;

public interface MovieReviewService {

	Mono<PagedResponse<MovieReviewDTO>> getMovieReviews(Integer page, Integer pageSize);

	Mono<PagedResponse<MovieReviewDTO>> getMovieReviewsByMovie(Long movieId, Integer page, Integer pageSize);

	Mono<MovieReviewDTO> getMovieReviewByID(String id);

	Mono<Void> saveReview(MovieReviewDTO movieReviewDTO);

}
