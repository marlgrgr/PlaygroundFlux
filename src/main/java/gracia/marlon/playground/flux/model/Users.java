package gracia.marlon.playground.flux.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class Users {

	@Id
	private Long id;

	@Column("username")
	private String username;

	@Column("password")
	private String password;

	@Column("fullname")
	private String fullname;

	@Column("password_change_required")
	private boolean passwordChangeRequired;

	@Column("is_retired")
	private boolean isRetired;
}
