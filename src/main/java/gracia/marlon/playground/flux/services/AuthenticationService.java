package gracia.marlon.playground.flux.services;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

	Mono<String> login(AuthRequestDTO request);

	Mono<Void> changePassword(String token, ChangePasswordDTO changePasswordDTO);

}
