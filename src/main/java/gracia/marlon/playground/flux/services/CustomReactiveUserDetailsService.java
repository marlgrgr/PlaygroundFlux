package gracia.marlon.playground.flux.services;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

	private final UserService userService;

	private final UserRoleService userRoleService;

	@Override
	public Mono<UserDetails> findByUsername(String username) {

		return this.userService.getUserByUsername(username).flatMap(user -> {
			Mono<String[]> monoRoleArray = this.userRoleService.getAllUserRolesByUser(user.getId())
					.filter(x -> x.getRole() != null && x.getRole().getRole() != null).map(roles -> {
						return roles.getRole().getRole().replaceAll("\\bROLE_", "");
					}).collectList().map(list -> list.toArray(new String[0]));

			return monoRoleArray.map(roleArray -> {
				return User.withUsername(user.getUsername()).password(user.getUsername()).roles(roleArray).build();
			});

		});
	}

}
