package gracia.marlon.playground.flux.websocket;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class WebsocketHandlerIT extends AbstractIntegrationBase {

	@LocalServerPort
	private int port;

	private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

	@Test
	void testConnectSendAndDisconnect() {
		URI uri = URI.create("ws://localhost:" + port + "/ws");

		Mono<Void> sessionMono = client.execute(uri,
				session -> session.send(Mono.just(session.textMessage("Websocket message"))).then());

		StepVerifier.create(sessionMono).expectComplete().verify();
	}
}
