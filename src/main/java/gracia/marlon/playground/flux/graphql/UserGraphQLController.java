package gracia.marlon.playground.flux.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;
import gracia.marlon.playground.flux.services.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class UserGraphQLController {

	private final UserService userService;

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<PagedResponse<UserDTO>> getUsers(@Argument Integer page, @Argument Integer pageSize) {
		return userService.getUsers(page, pageSize);
	}

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<UserDTO> getUserById(@Argument Long userId) {
		return userService.getUserById(userId);
	}

	@QueryMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<UserDTO> getUserByUsername(@Argument String username) {
		return userService.getUserByUsername(username);
	}

	@MutationMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<Boolean> createUser(@Argument("user") UserWithPasswordDTO userDto) {
		return userService.createUser(userDto).thenReturn(true);
	}

	@MutationMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<Boolean> deleteUser(@Argument Long userId) {
		return userService.deleteUser(userId).thenReturn(true);
	}
}