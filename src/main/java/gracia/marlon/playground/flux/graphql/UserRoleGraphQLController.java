package gracia.marlon.playground.flux.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserRoleDTO;
import gracia.marlon.playground.flux.services.UserRoleService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class UserRoleGraphQLController {

	private final UserRoleService userRoleService;

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<PagedResponse<UserRoleDTO>> getUserRoleList(@Argument Integer page, @Argument Integer pageSize) {
		return this.userRoleService.getUserRoles(page, pageSize);
	}

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<PagedResponse<UserRoleDTO>> getUserRoleListByUserId(@Argument Long userId, @Argument Integer page,
			@Argument Integer pageSize) {
		return this.userRoleService.getUserRolesByUser(userId, page, pageSize);
	}

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<PagedResponse<UserRoleDTO>> getUserRoleListByRoleId(@Argument Long roleId, @Argument Integer page,
			@Argument Integer pageSize) {
		return this.userRoleService.getUserRolesByRole(roleId, page, pageSize);
	}

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Flux<UserRoleDTO> getAllUserRoleListByUserId(@Argument Long userId) {
		return this.userRoleService.getAllUserRolesByUser(userId);
	}

	@MutationMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<Boolean> createUserRole(@Argument("userRole") UserRoleDTO userRoleDTO) {
		return this.userRoleService.createUserRole(userRoleDTO).thenReturn(true);
	}

	@MutationMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<Boolean> deleteUserRole(@Argument Long userRoleId) {
		return this.userRoleService.deleteUserRole(userRoleId).thenReturn(true);
	}
}
