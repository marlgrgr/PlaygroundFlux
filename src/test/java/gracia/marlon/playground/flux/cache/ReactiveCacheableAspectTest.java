package gracia.marlon.playground.flux.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.env.Environment;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.services.RoleServiceImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ReactiveCacheableAspectTest {

	private final RedissonReactiveClient redisson;

	private final Environment env;

	private final ReactiveCacheableAspect reactiveCacheableAspect;

	public ReactiveCacheableAspectTest() {
		this.redisson = Mockito.mock(RedissonReactiveClient.class);
		this.env = Mockito.mock(Environment.class);
		Mockito.when(this.env.getProperty(Mockito.eq("redis.config.maxIdleTime"), Mockito.anyString())).thenReturn("0");
		Mockito.when(this.env.getProperty(Mockito.eq("redis.config.redisTTL"), Mockito.anyString())).thenReturn("3");
		this.reactiveCacheableAspect = new ReactiveCacheableAspect(this.redisson, this.env, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void cacheReactiveSuccesfulMonoCached() throws Throwable {
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		RMapCacheReactive<Object, Object> cache = Mockito.mock(RMapCacheReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		Method methodRoles = RoleServiceImpl.class.getMethod("findByRole", String.class);
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) Mono.class);
		Mockito.when(reactiveCacheable.cacheName()).thenReturn("role-cache");
		Mockito.when(this.redisson.getMapCache(Mockito.anyString())).thenReturn(cache);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(Mono.just(roleDTO));

		Mono<RoleDTO> resultMono = (Mono<RoleDTO>) this.reactiveCacheableAspect.cacheReactive(pjp, reactiveCacheable);
		assertEquals(1L, resultMono.block().getId());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void cacheReactiveSuccesfulMonoNotCached() throws Throwable {
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		RMapCacheReactive<Object, Object> cache = Mockito.mock(RMapCacheReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		Method methodRoles = RoleServiceImpl.class.getMethod("findByRole", String.class);
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) Mono.class);
		Mockito.when(reactiveCacheable.cacheName()).thenReturn("role-cache");
		Mockito.when(this.redisson.getMapCache(Mockito.anyString())).thenReturn(cache);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(pjp.proceed()).thenReturn(Mono.just(roleDTO));
		Mockito.when(cache.put(Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.any(), Mockito.anyLong(),
				Mockito.any())).thenReturn(Mono.empty());

		Mono<RoleDTO> resultMono = (Mono<RoleDTO>) this.reactiveCacheableAspect.cacheReactive(pjp, reactiveCacheable);
		assertEquals(1L, resultMono.block().getId());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void cacheReactiveMonoException() throws Throwable {
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		RMapCacheReactive<Object, Object> cache = Mockito.mock(RMapCacheReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		Method methodRoles = RoleServiceImpl.class.getMethod("findByRole", String.class);
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) Mono.class);
		Mockito.when(reactiveCacheable.cacheName()).thenReturn("role-cache");
		Mockito.when(this.redisson.getMapCache(Mockito.anyString())).thenReturn(cache);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(pjp.proceed()).thenThrow(new RuntimeException("error"));

		Mono<Throwable> resultMono = (Mono<Throwable>) this.reactiveCacheableAspect.cacheReactive(pjp,
				reactiveCacheable);

		StepVerifier.create(resultMono)
				.expectErrorMatches(
						throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("error"))
				.verify();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void cacheReactiveSuccesfulFluxCached() throws Throwable {
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		RMapCacheReactive<Object, Object> cache = Mockito.mock(RMapCacheReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		List<RoleDTO> listRoleDTO = new ArrayList<RoleDTO>();
		listRoleDTO.add(roleDTO);
		Method methodRoles = RoleServiceImpl.class.getMethod("findByRole", String.class);
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) Flux.class);
		Mockito.when(reactiveCacheable.cacheName()).thenReturn("role-cache");
		Mockito.when(this.redisson.getMapCache(Mockito.anyString())).thenReturn(cache);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(Mono.just(listRoleDTO));

		Flux<RoleDTO> resultFlux = (Flux<RoleDTO>) this.reactiveCacheableAspect.cacheReactive(pjp, reactiveCacheable);
		assertEquals(1L, resultFlux.collectList().block().getFirst().getId());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void cacheReactiveSuccesfulFluxNotCached() throws Throwable {
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		RMapCacheReactive<Object, Object> cache = Mockito.mock(RMapCacheReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		List<RoleDTO> listRoleDTO = new ArrayList<RoleDTO>();
		listRoleDTO.add(roleDTO);
		Method methodRoles = RoleServiceImpl.class.getMethod("findByRole", String.class);
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) Flux.class);
		Mockito.when(reactiveCacheable.cacheName()).thenReturn("role-cache");
		Mockito.when(this.redisson.getMapCache(Mockito.anyString())).thenReturn(cache);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(pjp.proceed()).thenReturn(Flux.fromIterable(listRoleDTO));
		Mockito.when(cache.put(Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.any(), Mockito.anyLong(),
				Mockito.any())).thenReturn(Mono.empty());

		Flux<RoleDTO> resultFlux = (Flux<RoleDTO>) this.reactiveCacheableAspect.cacheReactive(pjp, reactiveCacheable);
		assertEquals(1L, resultFlux.collectList().block().getFirst().getId());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void cacheReactiveFluxException() throws Throwable {
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		RMapCacheReactive<Object, Object> cache = Mockito.mock(RMapCacheReactive.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		List<RoleDTO> listRoleDTO = new ArrayList<RoleDTO>();
		listRoleDTO.add(roleDTO);
		Method methodRoles = RoleServiceImpl.class.getMethod("findByRole", String.class);
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) Flux.class);
		Mockito.when(reactiveCacheable.cacheName()).thenReturn("role-cache");
		Mockito.when(this.redisson.getMapCache(Mockito.anyString())).thenReturn(cache);
		Mockito.when(cache.get(Mockito.anyString())).thenReturn(Mono.empty());
		Mockito.when(pjp.proceed()).thenThrow(new RuntimeException("error"));

		Flux<RoleDTO> resultFlux = (Flux<RoleDTO>) this.reactiveCacheableAspect.cacheReactive(pjp, reactiveCacheable);

		StepVerifier.create(resultFlux)
				.expectErrorMatches(
						throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("error"))
				.verify();
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void cacheReactiveClassNotSupported() throws Throwable {

		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		ReactiveCacheable reactiveCacheable = Mockito.mock(ReactiveCacheable.class);
		MethodSignature signature = Mockito.mock(MethodSignature.class);
		ParameterNameDiscoverer paramDiscoverer = Mockito.mock(ParameterNameDiscoverer.class);

		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setId(1L);
		List<RoleDTO> listRoleDTO = new ArrayList<RoleDTO>();
		listRoleDTO.add(roleDTO);
		Method methodRoles = RoleServiceImpl.class.getMethod("getRoles");
		String[] param = { "ROLE_ADMIN" };

		Mockito.when(paramDiscoverer.getParameterNames(Mockito.any(Method.class))).thenReturn(null);
		Mockito.when(pjp.getSignature()).thenReturn(signature);
		Mockito.when(signature.getMethod()).thenReturn(methodRoles);
		Mockito.when(reactiveCacheable.key()).thenReturn("#role");
		Mockito.when(pjp.getArgs()).thenReturn(param);
		Mockito.when(signature.getReturnType()).thenReturn((Class) String.class);

		ReactiveCacheableAspect localReactiveCacheableAspect = new ReactiveCacheableAspect(this.redisson, this.env,
				paramDiscoverer);

		assertThrows(UnsupportedOperationException.class,
				() -> localReactiveCacheableAspect.cacheReactive(pjp, reactiveCacheable));
	}

}
