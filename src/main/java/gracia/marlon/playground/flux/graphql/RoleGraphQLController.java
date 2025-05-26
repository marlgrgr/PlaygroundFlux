package gracia.marlon.playground.flux.graphql;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.services.RoleService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class RoleGraphQLController {

	private final RoleService roleService;

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Flux<RoleDTO> getRoleList() {
		return this.roleService.getRoles();
	}
}
