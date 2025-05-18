package gracia.marlon.playground.flux.configuration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import gracia.marlon.playground.flux.exception.ApiError;
import gracia.marlon.playground.flux.exception.RestException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(RestException.class)
	public Mono<ResponseEntity<ApiError>> handleApiException(RestException ex) {
		log.error("An exception ocurred: " + ex.getError());
		final ApiError error = ex.getError();
		return Mono.just(ResponseEntity.status(error.getHttpCode()).body(error));
	}
}
