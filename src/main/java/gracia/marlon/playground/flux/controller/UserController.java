package gracia.marlon.playground.flux.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gracia.marlon.playground.flux.dtos.PagedResponse;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.dtos.UserWithPasswordDTO;
import gracia.marlon.playground.flux.exception.ApiError;
import gracia.marlon.playground.flux.services.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User", description = "Operations to retrieve users, create and delete them")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserService userService;

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve users"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<PagedResponse<UserDTO>> getUserList(@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		final Mono<PagedResponse<UserDTO>> pagedResponse = this.userService.getUsers(page, pageSize);
		return pagedResponse;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users/{userId}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve user"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "404", description = "User not found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<UserDTO> getUserByID(@PathVariable Long userId) {
		final Mono<UserDTO> user = this.userService.getUserById(userId);
		return user;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users/username/{username}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve user"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "404", description = "User not found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<UserDTO> getUserByUsername(@PathVariable String username) {
		final Mono<UserDTO> user = this.userService.getUserByUsername(username);
		return user;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Successfully user created"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<Void> createUser(@RequestBody UserWithPasswordDTO userDto) {
		return this.userService.createUser(userDto);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Operation complete"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<Void> deleteUser(@PathVariable Long userId) {
		return this.userService.deleteUser(userId);
	}
}
