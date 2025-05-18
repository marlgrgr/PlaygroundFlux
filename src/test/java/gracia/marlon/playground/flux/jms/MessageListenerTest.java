package gracia.marlon.playground.flux.jms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gracia.marlon.playground.flux.services.WebsocketService;
import reactor.core.publisher.Mono;

public class MessageListenerTest {

	private final WebsocketService websocketService;

	private final MessageListener messageListener;

	public MessageListenerTest() {
		this.websocketService = Mockito.mock(WebsocketService.class);
		this.messageListener = new MessageListener(this.websocketService);
	}

	@Test
	public void receiveMessageException() throws IOException {

		Mockito.when(this.websocketService.broadcastMessage(Mockito.anyString()))
				.thenReturn(Mono.error(new RuntimeException()));

		assertDoesNotThrow(() -> {
			this.messageListener.processMessage("message").block();
		});
	}

}
