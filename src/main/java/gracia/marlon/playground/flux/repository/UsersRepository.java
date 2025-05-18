package gracia.marlon.playground.flux.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import gracia.marlon.playground.flux.model.Users;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsersRepository extends ReactiveCrudRepository<Users, Long> {

	@Query("SELECT * FROM Users WHERE id = :id AND is_retired = false")
	Mono<Users> findById(@Param("id") Long id);

	@Query("SELECT * FROM Users WHERE LOWER(username) = LOWER(:username) AND is_retired = false")
	Flux<Users> findByUsername(@Param("username") String username);

	@Query("SELECT * FROM Users WHERE is_retired = false ORDER BY ID DESC LIMIT :limit OFFSET :offset")
	Flux<Users> findNotRetiredUsers(@Param("limit") int limit, @Param("offset") int offset);

	@Query("SELECT count(1) FROM Users WHERE is_retired = false")
	Mono<Long> countNotRetiredUsers();

	@Query("UPDATE Users SET is_retired = true WHERE id = :id")
	Mono<Integer> retireById(@Param("id") Long id);
}