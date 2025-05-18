package gracia.marlon.playground.flux.queue;

import java.time.Duration;
import java.util.List;

import org.springframework.core.env.Environment;

import gracia.marlon.playground.flux.services.LoadMoviesService;
import gracia.marlon.playground.flux.util.SharedConstants;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Slf4j
public class QueueConsumer {

	private final SqsAsyncClient sqsClient;

	private final LoadMoviesService loadMoviesService;

	private final String queueUrl;

	private Disposable subscription;

	public QueueConsumer(SqsAsyncClient sqsClient, LoadMoviesService loadMoviesService, Environment env) {
		this.sqsClient = sqsClient;
		this.loadMoviesService = loadMoviesService;
		this.queueUrl = env.getProperty("sqs.queue.loadMovies.url", "");
		startListening();
	}

	public void startListening() {
		subscription = Flux.defer(this::pollAndProcess).repeat().subscribe();
	}

	private Flux<Void> pollAndProcess() {
		return pollMessages().flatMap(this::processAndDeleteMessage)
				.switchIfEmpty(Mono.delay(Duration.ofSeconds(1)).then(Mono.empty()));
	}

	private Flux<Message> pollMessages() {
		ReceiveMessageRequest request = ReceiveMessageRequest.builder().queueUrl(queueUrl).maxNumberOfMessages(10)
				.waitTimeSeconds(10).visibilityTimeout(20).build();

		return Mono.fromFuture(() -> sqsClient.receiveMessage(request)).flatMapMany(response -> {
			List<Message> messages = response.messages();
			return Flux.fromIterable(messages);
		});
	}

	private Mono<Void> processAndDeleteMessage(Message message) {
		DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
				.receiptHandle(message.receiptHandle()).build();

		if (SharedConstants.LOAD_MESSAGE.equalsIgnoreCase(message.body())) {
			log.info("A Load movies request message was receive from the queue");

			return loadMoviesService.loadMovies().then(Mono.fromFuture(() -> sqsClient.deleteMessage(deleteRequest)))
					.then();
		}

		return Mono.fromFuture(() -> sqsClient.deleteMessage(deleteRequest)).then();
	}

	@PreDestroy
	public void shutdown() {
		if (subscription != null && !subscription.isDisposed()) {
			log.info("Disposing SQS listener subscription...");
			subscription.dispose();
		}
	}

}
