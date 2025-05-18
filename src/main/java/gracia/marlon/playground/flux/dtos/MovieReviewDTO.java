package gracia.marlon.playground.flux.dtos;

import java.util.Date;

import lombok.Data;

@Data
public class MovieReviewDTO {

	private String id;

	private String review;

	private Double score;

	private Long movieId;

	private Date createOn;

}
