package gracia.marlon.playground.flux.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.model.Users;

public class UserMapperTest {

	@Test
	public void toDtoSuccessful() {
		UserDTO userDTO = UserMapper.INSTANCE.toDto(null);
		assertEquals(null, userDTO);

		Users user = new Users();
		user.setId(1L);
		user.setFullname("fullname");
		user.setPassword("password");
		user.setPasswordChangeRequired(true);
		user.setRetired(false);
		user.setUsername("user");

		userDTO = UserMapper.INSTANCE.toDto(user);
		assertEquals(1L, userDTO.getId().longValue());
		assertEquals("user", userDTO.getUsername());
	}
}
