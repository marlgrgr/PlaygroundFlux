package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;
import gracia.marlon.playground.flux.model.Users;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
	
	Mono<PagedResponse<UserDTO>> getUsers(Integer page, Integer pageSize);

	Mono<UserDTO> getUserById(Long userId);

	Mono<UserDTO> getUserByUsername(String username);
	
	Flux<UserDTO> findByUsername(String username);

	Mono<Void> createUser(UserWithPasswordDTO user);

	Mono<Void> deleteUser(Long userId);

	Mono<Void> changePassword(Long userId, ChangePasswordDTO changePasswordDTO);

	Mono<Boolean> validUser(AuthRequestDTO request);
	
	Mono<UserDTO> saveUser(Users user);

}
