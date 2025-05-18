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
import gracia.marlon.playground.flux.dtos.UserRoleDTO;
import gracia.marlon.playground.flux.exception.ApiError;
import gracia.marlon.playground.flux.services.UserRoleService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Roles", description = "Operations to retrieve roles user have, create and delete them")
@SecurityRequirement(name = "bearerAuth")
public class UserRoleController {

	private final UserRoleService userRoleService;

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/userRoles")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve user roles"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<PagedResponse<UserRoleDTO>> getUserRoleList(
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		final Mono<PagedResponse<UserRoleDTO>> pagedResponse = this.userRoleService.getUserRoles(page, pageSize);
		return pagedResponse;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/userRoles/user/{userId}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve user roles"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<PagedResponse<UserRoleDTO>> getUserRoleListByUserId(@PathVariable Long userId,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		final Mono<PagedResponse<UserRoleDTO>> pagedResponse = this.userRoleService.getUserRolesByUser(userId, page,
				pageSize);
		return pagedResponse;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/userRoles/role/{roleId}")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve user roles"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<PagedResponse<UserRoleDTO>> getUserRoleListByRoleId(@PathVariable Long roleId,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
		final Mono<PagedResponse<UserRoleDTO>> pagedResponse = this.userRoleService.getUserRolesByRole(roleId, page,
				pageSize);
		return pagedResponse;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/userRoles/user/{userId}/all")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve all user roles"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Flux<UserRoleDTO> getUserRoleListByUserId(@PathVariable Long userId) {
		final Flux<UserRoleDTO> userRoleList = this.userRoleService.getAllUserRolesByUser(userId);
		return userRoleList;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/userRoles")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Successfully user role created"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<Void> createUserRole(@RequestBody UserRoleDTO userRoleDTO) {
		return this.userRoleService.createUserRole(userRoleDTO);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/userRoles/{userRoleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Operation complete"),
			@ApiResponse(responseCode = "400", description = "validations are not met", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Mono<Void> deleteUserRole(@PathVariable Long userRoleId) {
		return this.userRoleService.deleteUserRole(userRoleId);
	}
}
