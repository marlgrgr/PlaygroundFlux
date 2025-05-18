package gracia.marlon.playground.flux.services;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DBSequenceServiceImpl implements DBSequenceService {

	private final DatabaseClient databaseClient;

	private static final String SEQUENCE_NAME_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$";

	@Override
	public Mono<Long> getNext(String sequenceName) {
		if(!isValidSequenceName(sequenceName)) {
			return Mono.empty();
		}
		
		String sql = "SELECT nextval('" + sequenceName + "') AS seq";
		return databaseClient.sql(sql).map(row -> row.get("seq", Long.class)).one();
	}

	private static boolean isValidSequenceName(String name) {
		return name != null && name.matches(SEQUENCE_NAME_REGEX);
	}

}
