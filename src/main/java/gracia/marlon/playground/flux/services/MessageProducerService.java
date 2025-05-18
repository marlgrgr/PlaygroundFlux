package gracia.marlon.playground.flux.services;

import reactor.core.publisher.Mono;

public interface MessageProducerService {

	Mono<Void> publishMessage(String destination, String message);

}
