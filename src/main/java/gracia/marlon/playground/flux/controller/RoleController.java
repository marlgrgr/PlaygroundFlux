package gracia.marlon.playground.flux.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.exception.ApiError;
import gracia.marlon.playground.flux.services.RoleService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Operations to get the roles from the app")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

	private final RoleService roleService;

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/roles")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieve roles"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: authentication is required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
			@ApiResponse(responseCode = "403", description = "Access denied: you do not have the required permissions.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))) })
	public Flux<RoleDTO> getRoleList() {
		return roleService.getRoles();
	}
}