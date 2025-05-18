package gracia.marlon.playground.flux.cache;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class ReactiveCacheableAspect {

	private final RedissonReactiveClient redisson;

	private final ExpressionParser parser = new SpelExpressionParser();

	private final ParameterNameDiscoverer paramDiscoverer;

	private final long redisTTL;

	private final long redisMaxIdleTime;

	public ReactiveCacheableAspect(RedissonReactiveClient redisson, Environment env,
			ParameterNameDiscoverer paramDiscoverer) {
		this.redisson = redisson;
		this.redisMaxIdleTime = Long.parseLong(env.getProperty("redis.config.maxIdleTime", "0"));
		this.redisTTL = Long.parseLong(env.getProperty("redis.config.redisTTL", "0"));
		this.paramDiscoverer = paramDiscoverer != null ? paramDiscoverer : new DefaultParameterNameDiscoverer();
	}

	@SuppressWarnings("unchecked")
	@Around("@annotation(reactiveCacheable)")
	public Object cacheReactive(ProceedingJoinPoint pjp, ReactiveCacheable reactiveCacheable) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		String key = parseKey(reactiveCacheable.key(), method, pjp.getArgs());

		Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType();

		if (Mono.class.isAssignableFrom(returnType)) {
			RMapCacheReactive<Object, Object> mapCache = redisson.getMapCache(reactiveCacheable.cacheName());

			return mapCache.get(key).flatMap(cached -> {
				return Mono.just(cached);
			}).switchIfEmpty(Mono.defer(() -> {
				try {
					return ((Mono<?>) pjp.proceed()).flatMap(result -> mapCache
							.put(key, result, this.redisTTL, TimeUnit.MINUTES, this.redisMaxIdleTime, TimeUnit.MINUTES)
							.thenReturn(result));
				} catch (Throwable e) {
					return Mono.error(e);
				}
			}));
		}

		if (Flux.class.isAssignableFrom(returnType)) {
			RMapCacheReactive<Object, List<Object>> mapCache = redisson.getMapCache(reactiveCacheable.cacheName());
			return mapCache.get(key).flatMapMany(cached -> {
				return Flux.fromIterable(cached);
			}).switchIfEmpty(Flux.defer(() -> {
				try {
					return ((Flux<?>) pjp.proceed()).collectList().flatMap(list -> {

						return mapCache.put(key, (List<Object>) list, this.redisTTL, TimeUnit.MINUTES,
								this.redisMaxIdleTime, TimeUnit.MINUTES).thenReturn(list);
					}).flatMapMany(Flux::fromIterable);
				} catch (Throwable e) {
					return Mono.error(e);
				}
			}));
		}

		throw new UnsupportedOperationException("Return type must be Mono or Flux");
	}

	private String parseKey(String expression, Method method, Object[] args) {
		EvaluationContext context = new StandardEvaluationContext();
		String[] paramNames = paramDiscoverer.getParameterNames(method);
		if (paramNames != null) {
			for (int i = 0; i < paramNames.length; i++) {
				context.setVariable(paramNames[i], args[i]);
			}
		}
		return parser.parseExpression(expression).getValue(context, String.class);
	}
}
