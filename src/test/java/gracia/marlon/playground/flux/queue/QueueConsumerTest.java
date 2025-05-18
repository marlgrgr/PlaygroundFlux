package gracia.marlon.playground.flux.queue;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gracia.marlon.playground.flux.services.LoadMoviesService;
import reactor.core.Disposable;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public class QueueConsumerTest {

	private final SqsAsyncClient sqsClient;

	private final LoadMoviesService loadMoviesService;

	private final Environment env;

	private final QueueConsumer queueConsumer;

	public QueueConsumerTest() {
		this.sqsClient = Mockito.mock(SqsAsyncClient.class);
		this.loadMoviesService = Mockito.mock(LoadMoviesService.class);
		this.env = Mockito.mock(Environment.class);
		Mockito.when(this.env.getProperty(Mockito.eq("sqs.queue.loadMovies.url"), Mockito.anyString())).thenReturn("0");

		ReceiveMessageResponse receiveMessageResponse = Mockito.mock(ReceiveMessageResponse.class);
		Mockito.when(this.sqsClient.receiveMessage(Mockito.any(ReceiveMessageRequest.class)))
				.thenReturn(CompletableFuture.completedFuture(receiveMessageResponse));
		Mockito.when(receiveMessageResponse.messages()).thenReturn(new ArrayList<Message>());

		this.queueConsumer = new QueueConsumer(this.sqsClient, this.loadMoviesService, this.env);
	}

	@Test
	public void shutdownTest() throws Throwable {
		Disposable mockSubscription = Mockito.mock(Disposable.class);

		Mockito.when(mockSubscription.isDisposed()).thenReturn(true).thenReturn(false);

		try {
			var field = QueueConsumer.class.getDeclaredField("subscription");
			field.setAccessible(true);
			field.set(this.queueConsumer, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		this.queueConsumer.shutdown();

		Mockito.verify(mockSubscription, Mockito.never()).dispose();

		try {
			var field = QueueConsumer.class.getDeclaredField("subscription");
			field.setAccessible(true);
			field.set(this.queueConsumer, mockSubscription);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		this.queueConsumer.shutdown();

		Mockito.verify(mockSubscription, Mockito.never()).dispose();

		this.queueConsumer.shutdown();

		Mockito.verify(mockSubscription).dispose();
	}
}
