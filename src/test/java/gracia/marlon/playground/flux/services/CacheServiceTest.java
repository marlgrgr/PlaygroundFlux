package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;

import reactor.core.publisher.Mono;

public class CacheServiceTest {

	private final RedissonReactiveClient redissonClient;

	private final RMapCacheReactive<String, String> cache;

	private final CacheService cacheService;

	@SuppressWarnings("unchecked")
	public CacheServiceTest() {
		this.redissonClient = Mockito.mock(RedissonReactiveClient.class);
		this.cache = Mockito.mock(RMapCacheReactive.class);
		Mockito.when(this.redissonClient.<String, String>getMapCache(Mockito.anyString())).thenReturn(this.cache);

		this.cacheService = new CacheServiceImpl(this.redissonClient);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void evictCacheSuccessful() {
		RMapCacheReactive<Object, Object> returnCache = Mockito.mock(RMapCacheReactive.class);

		Mockito.when(redissonClient.getMapCache(Mockito.anyString())).thenReturn(returnCache);
		Mockito.when(returnCache.delete()).thenReturn(Mono.just(true));

		this.cacheService.evictCache("cache-name").block();

		Mockito.verify(returnCache).delete();

	}

	@Test
	public void putInSpecialCacheWithTTLSuccessful() {
		Mockito.when(this.cache.put(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any()))
				.thenReturn(Mono.empty());

		this.cacheService.putInSpecialCacheWithTTL("key", "value", 600).block();

		Mockito.verify(this.cache).put(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any());

	}

	@Test
	public void getFromSpecialCacheSuccessful() {
		Mockito.when(this.cache.get(Mockito.anyString())).thenReturn(Mono.just("value"));

		String value = this.cacheService.getFromSpecialCache("key").block();

		assertEquals("value", value);

	}

}
