package gracia.marlon.playground.flux.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.exception.ApiError;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.services.AuthenticationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations to login on the app and also to change user password")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	@PostMapping("/auth/login")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful login"),
			@ApiResponse(responseCode = "401", description = "User/pass not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<String> login(@RequestBody AuthRequestDTO request) {
		final Mono<String> token = authenticationService.login(request);
		return token;
	}

	@PostMapping("/auth/changePassword")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Password changed successfully"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "User/pass not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))

	})
	public Mono<Void> changePassword(@RequestHeader("Authorization") String authHeader,
			@RequestBody ChangePasswordDTO changePasswordDTO) {

		if (!authHeader.trim().toLowerCase().startsWith("bearer ")) {
			return Mono.error(new RestException("Token is required", "AUTH-0015", HttpStatus.BAD_REQUEST));
		}

		final String token = authHeader.substring(7);
		return this.authenticationService.changePassword(token, changePasswordDTO);
	}
}
