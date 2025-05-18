package gracia.marlon.playground.flux.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import gracia.marlon.playground.flux.dtos.UserRoleRow;
import gracia.marlon.playground.flux.model.UserRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

	@Query("""
			    SELECT ur.id AS userRoleId, u.id AS userId, u.username AS userUsername,
			    u.fullname AS userFullname, u.password_change_required AS userPasswordChangeRequired,
			    r.id AS roleId, r.role AS role
			    FROM user_role ur
			    JOIN users u ON ur.user_id = u.id
			    JOIN role r ON ur.role_id = r.id
			    WHERE u.is_retired = false
			    ORDER BY ur.id DESC LIMIT :limit OFFSET :offset
			""")
	Flux<UserRoleRow> findAllActiveUserRolesWithDetails(@Param("limit") int limit, @Param("offset") int offset);

	@Query("SELECT count(1) FROM user_role ur JOIN users u ON ur.user_id = u.id JOIN role r ON ur.role_id = r.id WHERE u.is_retired = false")
	Mono<Long> countAllActiveUserRoles();

	@Query("""
			    SELECT ur.id AS userRoleId, u.id AS userId, u.username AS userUsername,
			    u.fullname AS userFullname, u.password_change_required AS userPasswordChangeRequired,
			    r.id AS roleId, r.role AS role
			    FROM user_role ur
			    JOIN users u ON ur.user_id = u.id
			    JOIN role r ON ur.role_id = r.id
			    WHERE u.is_retired = false AND u.id = :userId
			    ORDER BY ur.id DESC LIMIT :limit OFFSET :offset
			""")
	Flux<UserRoleRow> findByUserId(@Param("userId") Long userId, @Param("limit") int limit,
			@Param("offset") int offset);

	@Query("SELECT count(1) FROM user_role ur JOIN users u ON ur.user_id = u.id JOIN role r ON ur.role_id = r.id WHERE u.is_retired = false AND u.id = :userId")
	Mono<Long> countByUserId(@Param("userId") Long userId);

	@Query("""
			    SELECT ur.id AS userRoleId, u.id AS userId, u.username AS userUsername,
			    u.fullname AS userFullname, u.password_change_required AS userPasswordChangeRequired,
			    r.id AS roleId, r.role AS role
			    FROM user_role ur
			    JOIN users u ON ur.user_id = u.id
			    JOIN role r ON ur.role_id = r.id
			    WHERE u.is_retired = false AND r.id = :roleId
			    ORDER BY ur.id DESC LIMIT :limit OFFSET :offset
			""")
	Flux<UserRoleRow> findByRoleId(@Param("roleId") Long roleId, @Param("limit") int limit,
			@Param("offset") int offset);

	@Query("SELECT count(1) FROM user_role ur JOIN users u ON ur.user_id = u.id JOIN role r ON ur.role_id = r.id WHERE u.is_retired = false AND r.id = :roleId")
	Mono<Long> countByRoleId(@Param("roleId") Long roleId);

	@Query("""
			    SELECT ur.id AS userRoleId, u.id AS userId, u.username AS userUsername,
			    u.fullname AS userFullname, u.password_change_required AS userPasswordChangeRequired,
			    r.id AS roleId, r.role AS role
			    FROM user_role ur
			    JOIN users u ON ur.user_id = u.id
			    JOIN role r ON ur.role_id = r.id
			    WHERE u.is_retired = false AND u.id = :userId
			""")
	Flux<UserRoleRow> findAllByUserId(@Param("userId") Long userId);
	
	@Query("SELECT count(1) FROM user_role ur JOIN users u ON ur.user_id = u.id JOIN role r ON ur.role_id = r.id WHERE u.is_retired = false AND u.id = :userId AND r.id = :roleId")
	Mono<Long> countByUserAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
