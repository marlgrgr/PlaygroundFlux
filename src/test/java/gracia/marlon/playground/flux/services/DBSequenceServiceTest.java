package gracia.marlon.playground.flux.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;

import io.r2dbc.spi.Row;
import reactor.core.publisher.Mono;

public class DBSequenceServiceTest {

	private final DatabaseClient databaseClient;

	private final DBSequenceService dbSequenceService;

	public DBSequenceServiceTest() {
		this.databaseClient = Mockito.mock(DatabaseClient.class);
		this.dbSequenceService = new DBSequenceServiceImpl(this.databaseClient);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getNextSuccessful() {
		GenericExecuteSpec genericExecuteSpec = Mockito.mock(GenericExecuteSpec.class);

		RowsFetchSpec<Long> rowsFetchSpec = Mockito.mock(RowsFetchSpec.class);

		Row mockRow = Mockito.mock(Row.class);

		Mockito.when(mockRow.get("seq", Long.class)).thenReturn(1L);
		Mockito.when(databaseClient.sql(Mockito.anyString())).thenReturn(genericExecuteSpec);
		Mockito.when(genericExecuteSpec.map(Mockito.any(Function.class))).thenAnswer(invocation -> {
			Function<Row, Long> mappingFunction = invocation.getArgument(0);
			mappingFunction.apply(mockRow);
			return rowsFetchSpec;
		});
		Mockito.when(rowsFetchSpec.one()).thenReturn(Mono.just(1L));

		assertEquals(1L, this.dbSequenceService.getNext("seq").block());

	}

	@Test
	public void getNextFailValidation() {
		assertEquals(null, this.dbSequenceService.getNext(null).block());
		assertEquals(null, this.dbSequenceService.getNext("seq*$%").block());
	}
}
