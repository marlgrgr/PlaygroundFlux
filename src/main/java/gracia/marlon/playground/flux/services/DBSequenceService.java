package gracia.marlon.playground.flux.services;

import reactor.core.publisher.Mono;

public interface DBSequenceService {

	Mono<Long> getNext(String sequenceName);
}
