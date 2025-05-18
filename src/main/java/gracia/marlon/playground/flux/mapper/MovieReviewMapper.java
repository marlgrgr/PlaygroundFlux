package gracia.marlon.playground.flux.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import gracia.marlon.playground.flux.dtos.MovieReviewDTO;
import gracia.marlon.playground.flux.model.MovieReview;

@Mapper(componentModel = "spring")
public interface MovieReviewMapper {

	MovieReviewMapper INSTANCE = Mappers.getMapper(MovieReviewMapper.class);

	MovieReviewDTO toDto(MovieReview entity);

	MovieReview toEntity(MovieReviewDTO dto);
}
