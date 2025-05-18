package gracia.marlon.playground.flux.services;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerServiceImpl implements MessageProducerService {

	private final JmsTemplate jmsTemplate;

	@Override
	public Mono<Void> publishMessage(String destination, String message) {
		return Mono.fromRunnable(() -> jmsTemplate.convertAndSend(destination, message)).doOnSuccess(
				unused -> log.info("A request for broadcast notification to the user websocket was sent to the topic."))
				.subscribeOn(Schedulers.boundedElastic()).then();
	}

}
