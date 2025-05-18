package gracia.marlon.playground.flux.queue;

import reactor.core.publisher.Mono;

public interface QueuePublisherService {

	public Mono<Void> publish(String message, String queue);

}
