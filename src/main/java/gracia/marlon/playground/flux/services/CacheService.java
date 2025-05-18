package gracia.marlon.playground.flux.services;

import reactor.core.publisher.Mono;

public interface CacheService {

	Mono<Void> evictCache(String cachename);

	Mono<Void> putInSpecialCacheWithTTL(String key, String value, int ttlInSeconds);

	Mono<String> getFromSpecialCache(String key);

}
