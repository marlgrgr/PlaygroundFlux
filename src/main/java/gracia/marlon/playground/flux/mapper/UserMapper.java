package gracia.marlon.playground.flux.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import gracia.marlon.playground.flux.dtos.UserDTO;
import gracia.marlon.playground.flux.model.Users;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	UserDTO toDto(Users entity);

}
