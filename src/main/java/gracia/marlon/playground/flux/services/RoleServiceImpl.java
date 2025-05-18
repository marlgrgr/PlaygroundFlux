package gracia.marlon.playground.flux.services;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gracia.marlon.playground.flux.cache.ReactiveCacheable;
import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.mapper.RoleMapper;
import gracia.marlon.playground.flux.model.Role;
import gracia.marlon.playground.flux.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;

	private final RoleMapper roleMapper;

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	@Override
	@ReactiveCacheable(cacheName = "Flux_RoleService_getRoles", key = "'getRoles'")
	public Flux<RoleDTO> getRoles() {

		Flux<Role> roleList = this.roleRepository.findAll();

		Flux<RoleDTO> roleDTOList = roleList.map(this.roleMapper::toDto);

		return roleDTOList;
	}

	@Override
	@ReactiveCacheable(cacheName = "Flux_RoleService_findByRole", key = "#role")
	public Mono<RoleDTO> findByRole(String role) {
		Mono<Role> monoRole = this.roleRepository.findByRole(role).next();

		return monoRole.map(this.roleMapper::toDto);
	}

	@Override
	@Transactional
	public Mono<RoleDTO> saveRole(Role role) {
		Mono<Long> roleSeq = this.dbSequenceService.getNext("role_seq");

		return roleSeq.flatMap(seq -> {
			role.setId(seq);
			return databaseClient.sql("INSERT INTO role (id, role) VALUES (:id, :role)").bind("id", role.getId())
					.bind("role", role.getRole()).fetch().rowsUpdated().thenReturn(role);
		}).map(this.roleMapper::toDto);
	}

}
