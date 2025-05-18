package gracia.marlon.playground.flux.services;

import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

public interface WebsocketService {

	void addSession(WebSocketSession session);

	void removeSession(WebSocketSession session);

	Mono<Void> broadcastMessage(String message);

}
