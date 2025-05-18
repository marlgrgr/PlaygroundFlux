package gracia.marlon.playground.flux.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import gracia.marlon.playground.flux.services.WebsocketService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReactiveWebSocketHandler implements WebSocketHandler {

	private final WebsocketService websocketService;

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		websocketService.addSession(session);

		return session.receive().doFinally(signalType -> {
			websocketService.removeSession(session);
		}).then();
	}
}