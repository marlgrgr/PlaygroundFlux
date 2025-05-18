package gracia.marlon.playground.flux.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {

	private String message;

	private String code;

	private int httpCode;

}
