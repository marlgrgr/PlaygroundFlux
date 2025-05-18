package gracia.marlon.playground.flux.queue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gracia.marlon.playground.flux.configuration.AbstractIntegrationBase;

public class QueueConsumerIT extends AbstractIntegrationBase {

	@Autowired
	private QueuePublisherService queuePublisherService;

	@Test
	void sqsMessage() {

		assertDoesNotThrow(() -> {
			this.queuePublisherService.publish("message", "test_queue").block();
		});

	}

}
