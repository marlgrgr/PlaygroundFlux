package gracia.marlon.playground.flux.queue;

import reactor.core.publisher.Mono;

public interface AsyncConsumerService {

	Mono<Void> callAsync(String message);
}
