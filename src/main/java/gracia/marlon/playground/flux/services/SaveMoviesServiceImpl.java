package gracia.marlon.playground.flux.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gracia.marlon.playground.flux.dtos.MovieDetailsDTO;
import gracia.marlon.playground.flux.dtos.MovieDetailsResponseDTO;
import gracia.marlon.playground.flux.mapper.MovieDetailsMapper;
import gracia.marlon.playground.flux.model.Movie;
import gracia.marlon.playground.flux.repository.MovieRepository;
import gracia.marlon.playground.flux.util.SharedConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveMoviesServiceImpl implements SaveMoviesService {

	private final MovieRepository movieRepository;

	private final CacheService cacheService;

	private final MessageProducerService messageProducerService;

	private final MovieDetailsMapper movieDetailsMapper;

	@Override
	public Mono<Void> saveMovieList(MovieDetailsResponseDTO movieDetailsResponseDTO) {
		if (!this.validMovieDetailsResponseDTO(movieDetailsResponseDTO)) {
			return Mono.empty();
		}

		final Date createdOn = new Date();
		movieDetailsResponseDTO.getResults().stream().forEach(x -> x.setCreateOn(createdOn));

		return this.saveMovies(movieDetailsResponseDTO.getResults())
				.then(Mono.defer(()-> this.evictMovieCaches()))
				.then(Mono.defer(()->this.messageProducerService.publishMessage(SharedConstants.TOPIC_NOTIFICATION_NAME,
						SharedConstants.TOPIC_NOTIFICATION_NEW_MOVIE)))
				.doOnSuccess(unused -> log.info("The movie list was succesfully persisted on the DB"))
				.onErrorResume(error -> {
					log.error("An error occurred while saving the movie list", error);
					return Mono.empty();
				});

	}

	private boolean validMovieDetailsResponseDTO(MovieDetailsResponseDTO movieDetailsResponseDTO) {
		return movieDetailsResponseDTO != null && movieDetailsResponseDTO.getResults() != null
				&& !movieDetailsResponseDTO.getResults().isEmpty();
	}

	private Mono<Void> saveMovies(List<MovieDetailsDTO> movieDetailsDTOList) {
		final List<Movie> movieList = movieDetailsDTOList.stream().map(this.movieDetailsMapper::toEntity)
				.collect(Collectors.toList());
		return this.movieRepository.saveAll(movieList).then();
	}

	private Mono<Void> evictMovieCaches() {
		return Mono.when(this.cacheService.evictCache("Flux_MovieService_getMovieList"),
				this.cacheService.evictCache("Flux_MovieService_getMovieById"),
				this.cacheService.evictCache("MovieService_getMovieList"),
				this.cacheService.evictCache("MovieService_getMovieById"));
	}

}
