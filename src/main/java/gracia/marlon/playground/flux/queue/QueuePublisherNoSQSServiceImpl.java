package gracia.marlon.playground.flux.queue;

import gracia.marlon.playground.flux.util.SharedConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Slf4j
public class QueuePublisherNoSQSServiceImpl implements QueuePublisherService {

	private final AsyncConsumerService asyncConsumerService;

	@Override
	public Mono<Void> publish(String message, String queue) {
		if (SharedConstants.LOAD_MESSAGE.equalsIgnoreCase(message)) {
			log.info("A Load movies request message was request to be done async");

			Mono.just(message).publishOn(Schedulers.boundedElastic()).flatMap(asyncConsumerService::callAsync)
					.subscribe();
		}

		return Mono.empty();
	}

}
