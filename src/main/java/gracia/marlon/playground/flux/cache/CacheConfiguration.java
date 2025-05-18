package gracia.marlon.playground.flux.cache;

import java.io.File;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfiguration {

	private final String redissonConfigPath;

	public CacheConfiguration(Environment env) {
		this.redissonConfigPath = env.getProperty("redis.config.path", "");
	}

	@Bean
	RedissonClient redissonClient() {
		RedissonClient redissonClient = null;
		try {
			final Config config = Config.fromYAML(new File(this.redissonConfigPath));
			config.setCodec(new JsonJacksonCodec());
			redissonClient = Redisson.create(config);
		} catch (Exception e) {
			log.error("An error occurred while creating the redisson client", e);
		}
		return redissonClient;
	}

	@Bean
	RedissonReactiveClient redissonReactiveClient(RedissonClient redissonClient) {
		return redissonClient.reactive();
	}
}
