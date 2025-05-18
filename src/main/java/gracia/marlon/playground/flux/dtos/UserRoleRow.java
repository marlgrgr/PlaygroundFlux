package gracia.marlon.playground.flux.dtos;

import org.springframework.data.relational.core.mapping.Column;

import lombok.Data;

@Data
public class UserRoleRow {

	@Column("userRoleId")
	Long userRoleId;

	@Column("userId")
	Long userId;

	@Column("userUsername")
	String userUsername;

	@Column("userFullname")
	String userFullname;

	@Column("userPasswordChangeRequired")
	Boolean userPasswordChangeRequired;

	@Column("roleId")
	Long roleId;

	@Column("role")
	String role;

}
