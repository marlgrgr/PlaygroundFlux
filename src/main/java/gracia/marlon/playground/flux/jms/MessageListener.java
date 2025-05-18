package gracia.marlon.playground.flux.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import gracia.marlon.playground.flux.services.WebsocketService;
import gracia.marlon.playground.flux.util.SharedConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageListener {

	private final WebsocketService websocketService;

	@JmsListener(destination = SharedConstants.TOPIC_NOTIFICATION_NAME, containerFactory = "topicListenerFactory", subscription = "flux-subscription")
	public void receiveMessage(String mensaje) {
		Mono.just(mensaje).publishOn(Schedulers.boundedElastic()).flatMap(this::processMessage).subscribe();
	}

	public Mono<Void> processMessage(String message) {
		log.info("A request for broadcast notification to the user websocket was receive from the topic.");
		return this.websocketService.broadcastMessage(message).onErrorResume(error -> {
			log.error("An error occurred while broadcasting the message", error);
			return Mono.empty();
		});
	}
}
