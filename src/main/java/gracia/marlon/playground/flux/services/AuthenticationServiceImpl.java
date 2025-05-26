package gracia.marlon.playground.flux.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import gracia.marlon.playground.flux.dtos.AuthRequestDTO;
import gracia.marlon.playground.flux.dtos.ChangePasswordDTO;
import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.exception.RestException;
import gracia.marlon.playground.flux.util.SharedConstants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final JWTService jwtService;

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final String PASSWORD_REGEX_VALIDATION = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";

	private final Pattern pattern = Pattern.compile(PASSWORD_REGEX_VALIDATION);

	@Override
	public Mono<String> login(AuthRequestDTO request) {
		return this.validateAuthRequestDTO(request).then(Mono.defer(() -> {
			return this.userService.validUser(request).flatMap(valid -> {
				if (!valid) {
					return Mono.error(new RestException("The user/password is not correct", "AUTH-0014",
							HttpStatus.UNAUTHORIZED));
				}

				return this.userService.getUserByUsername(request.getUsername()).flatMap(user -> {
					return this.userRoleService.getAllUserRolesByUser(user.getId()).filter(x -> x.getRole() != null)
							.map(roles -> {
								return roles.getRole().getRole();
							}).collectList().map(list -> list.toArray(new String[0])).map(roleList -> {
								final Map<String, Object> claims = new HashMap<String, Object>();
								claims.put(SharedConstants.CLAIMS_PASSWORD_CHANGE_REQUIRED,
										user.isPasswordChangeRequired());
								claims.put(SharedConstants.CLAIMS_USER_ID, user.getId());
								claims.put(SharedConstants.CLAIMS_USER_FULLNAME, user.getFullname());
								claims.put(SharedConstants.CLAIMS_USER_ROLES, roleList);

								return this.jwtService.generateToken(claims, user);
							});
				});
			});
		}));
	}

	@Override
	public Mono<Void> changePassword(String token, ChangePasswordDTO changePasswordDTO) {
		return Mono.fromCallable(() -> {
			try {
				String username = this.jwtService.extractUsername(token);
				Long userId = this.jwtService.extractUserId(token);
				UserDTO userDTO = new UserDTO();
				userDTO.setUsername(username);
				if(!this.jwtService.isTokenValid(token, userDTO)) {
					throw new RestException("Token is not valid", "AUTH-0016", HttpStatus.BAD_REQUEST);
				}
				return Tuples.of(username, userId);
			} catch (Exception e) {
				throw new RestException("Token is not valid", "AUTH-0016", HttpStatus.BAD_REQUEST);
			}
		}).flatMap(tuple -> {
			String username = tuple.getT1();
			Long userId = tuple.getT2();
			return this.validatePasswordChange(username, changePasswordDTO)
					.then(Mono.defer(()-> {
						return this.userService.changePassword(userId, changePasswordDTO);
					}));
		});
	}

	private Mono<Void> validateAuthRequestDTO(AuthRequestDTO request) {
		if (request == null) {
			return Mono.error(new RestException("Request object not sent", "AUTH-0013", HttpStatus.BAD_REQUEST));
		}

		if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
			return Mono.error(new RestException("No user sent", "AUTH-0002", HttpStatus.BAD_REQUEST));
		}

		if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
			return Mono.error(new RestException("Invalid password", "AUTH-0005", HttpStatus.BAD_REQUEST));
		}

		return Mono.empty();
	}

	private Mono<Void> validatePasswordChange(String username, ChangePasswordDTO changePasswordDTO) {
		if (changePasswordDTO == null) {
			return Mono.error(new RestException("No information about password change sent", "AUTH-0007",
					HttpStatus.BAD_REQUEST));
		}

		if (changePasswordDTO.getNewPassword() == null
				|| !changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
			return Mono.error(new RestException("New password and confirmation password doesn't match", "AUTH-0008",
					HttpStatus.BAD_REQUEST));
		}

		if (changePasswordDTO.getNewPassword().equals(changePasswordDTO.getOldPassword())) {
			return Mono.error(
					new RestException("New password can not be the old password", "AUTH-0018", HttpStatus.BAD_REQUEST));
		}

		if (!this.pattern.matcher(changePasswordDTO.getNewPassword()).matches()) {
			return Mono.error(new RestException("Password doesn't meet minimum requirements", "AUTH-0010",
					HttpStatus.BAD_REQUEST));
		}

		final AuthRequestDTO request = new AuthRequestDTO();
		request.setUsername(username);
		request.setPassword(changePasswordDTO.getOldPassword());

		return this.userService.validUser(request).flatMap(valid -> {
			if (!valid) {
				return Mono.error(
						new RestException("The user/password is not correct", "AUTH-0014", HttpStatus.UNAUTHORIZED));
			}

			return Mono.empty();
		});

	}

}
