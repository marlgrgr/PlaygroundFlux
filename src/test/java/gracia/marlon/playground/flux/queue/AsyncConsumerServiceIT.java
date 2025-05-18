package gracia.marlon.playground.flux.queue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import gracia.marlon.playground.flux.services.LoadMoviesService;
import gracia.marlon.playground.flux.util.SharedConstants;
import reactor.core.publisher.Mono;

public class AsyncConsumerServiceIT {

	@Test
	void asyncCall() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {

			ConfigurableEnvironment env = new StandardEnvironment();
			Map<String, Object> props = new HashMap<>();
			props.put("spring.cloud.aws.sqs.enabled", "false");
			env.getPropertySources().addFirst(new MapPropertySource("testProps", props));
			context.setEnvironment(env);

			LoadMoviesService mockLoadMoviesService = Mockito.mock(LoadMoviesService.class);
			Mockito.when(mockLoadMoviesService.loadMovies()).thenReturn(Mono.empty());

			context.registerBean(LoadMoviesService.class, () -> mockLoadMoviesService);
			context.register(AsyncConsumerServiceImpl.class);
			context.register(QueueConfig.class);
			context.refresh();

			QueuePublisherService queueService = context.getBean(QueuePublisherService.class);
			AsyncConsumerServiceImpl asyncConsumerServiceImpl = context.getBean(AsyncConsumerServiceImpl.class);

			assertDoesNotThrow(() -> {
				queueService.publish("message", null).block();
			});

			assertDoesNotThrow(() -> {
				queueService.publish(SharedConstants.LOAD_MESSAGE, null).block();
			});

			assertDoesNotThrow(() -> {
				asyncConsumerServiceImpl.callAsync("message").block();
			});

		}
	}
}
