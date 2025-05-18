package gracia.marlon.playground.flux.services;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class CacheServiceImpl implements CacheService {

	private final RedissonReactiveClient redisson;

	private final RMapCacheReactive<String, String> cache;

	private final String SPECIAL_CACHE_NAME = "specialCache";

	public CacheServiceImpl(RedissonReactiveClient redisson) {
		this.redisson = redisson;
		this.cache = redisson.getMapCache(this.SPECIAL_CACHE_NAME);

	}

	@Override
	public Mono<Void> evictCache(String cachename) {
		RMapCacheReactive<Object, Object> mapCache = redisson.getMapCache(cachename);
		return mapCache.delete().then();
	}

	@Override
	public Mono<Void> putInSpecialCacheWithTTL(String key, String value, int ttlInSeconds) {
		return this.cache.put(key, value, ttlInSeconds, TimeUnit.SECONDS).then();
	}

	@Override
	public Mono<String> getFromSpecialCache(String key) {
		return this.cache.get(key);
	}

}
