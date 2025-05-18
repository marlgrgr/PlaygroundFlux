package gracia.marlon.playground.flux.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import gracia.marlon.playground.flux.model.Role;
import reactor.core.publisher.Flux;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
	Flux<Role> findByRole(String role);
}
