package gracia.marlon.playground.flux.services;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WebsocketServiceImpl implements WebsocketService {

	private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

	@Override
	public void addSession(WebSocketSession session) {
		this.sessions.add(session);
	}

	@Override
	public void removeSession(WebSocketSession session) {
		this.sessions.remove(session);
	}

	@Override
	public Mono<Void> broadcastMessage(String message) {
		List<Mono<Void>> sendMonos = sessions.stream().filter(WebSocketSession::isOpen).map(session -> {
			WebSocketMessage webSocketMessage = session.textMessage(message);
			return session.send(Mono.just(webSocketMessage));
		}).collect(Collectors.toList());

		log.info("Broadcasting message to {} open websocket sessions", sendMonos.size());

		return Mono.when(sendMonos);
	}
}
