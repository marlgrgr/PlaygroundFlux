package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleService {

	Flux<RoleDTO> getRoles();
	
	Mono<RoleDTO> findByRole(String role);
	
	Mono<RoleDTO> saveRole(Role role);
}
