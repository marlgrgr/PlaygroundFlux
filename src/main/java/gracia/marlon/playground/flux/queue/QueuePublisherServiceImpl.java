package gracia.marlon.playground.flux.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@RequiredArgsConstructor
@Slf4j
public class QueuePublisherServiceImpl implements QueuePublisherService {

	private final SqsAsyncClient sqsClient;

	@Override
	public Mono<Void> publish(String message, String queue) {

		SendMessageRequest sendMessageRequest = SendMessageRequest.builder().queueUrl(queue).messageBody(message)
				.build();

		return Mono.fromFuture(() -> sqsClient.sendMessage(sendMessageRequest))
				.doOnSuccess(unused -> log.info("A Load movies request message was sent to the queue")).then();
	}

}
