package gracia.marlon.playground.flux.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebsocketConfig {

	private final ReactiveWebSocketHandler webSocketHandler;

	@Bean
	HandlerMapping webSocketHandlerMapping() {
		Map<String, WebSocketHandler> map = new HashMap<>();
		map.put("/ws", webSocketHandler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(1);
		handlerMapping.setUrlMap(map);
		return handlerMapping;
	}

	@Bean
	WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}