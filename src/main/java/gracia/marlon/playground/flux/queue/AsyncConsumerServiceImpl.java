package gracia.marlon.playground.flux.queue;

import org.springframework.stereotype.Service;

import gracia.marlon.playground.flux.services.LoadMoviesService;
import gracia.marlon.playground.flux.util.SharedConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncConsumerServiceImpl implements AsyncConsumerService {

	private final LoadMoviesService loadMoviesService;

	@Override
	public Mono<Void> callAsync(String message) {

		if (SharedConstants.LOAD_MESSAGE.equalsIgnoreCase(message)) {
			log.info("A Load movies request message was recieve asynchronously");

			return loadMoviesService.loadMovies();
		}

		return Mono.empty();

	}

}
