package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserRoleDTO;
import gracia.marlon.playground.flux.model.UserRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleService {

	Mono<PagedResponse<UserRoleDTO>> getUserRoles(Integer page, Integer pageSize);

	Mono<PagedResponse<UserRoleDTO>> getUserRolesByUser(Long userId, Integer page, Integer pageSize);

	Mono<PagedResponse<UserRoleDTO>> getUserRolesByRole(Long roleId, Integer page, Integer pageSize);

	Flux<UserRoleDTO> getAllUserRolesByUser(Long userId);

	Mono<Void> createUserRole(UserRoleDTO userRoleDTO);

	Mono<Void> deleteUserRole(Long id);
	
	Mono<UserRole> saveUserRole(UserRole userRole);

}
