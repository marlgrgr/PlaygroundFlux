package gracia.marlon.playground.flux.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import gracia.marlon.playground.flux.dtos.RoleDTO;
import gracia.marlon.playground.flux.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

	RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

	RoleDTO toDto(Role entity);
}
