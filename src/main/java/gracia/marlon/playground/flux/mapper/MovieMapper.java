package gracia.marlon.playground.flux.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import gracia.marlon.playground.flux.dtos.MovieDTO;
import gracia.marlon.playground.flux.model.Movie;

@Mapper(componentModel = "spring")
public interface MovieMapper {

	MovieMapper INSTANCE = Mappers.getMapper(MovieMapper.class);

	MovieDTO toDto(Movie entity);

}
