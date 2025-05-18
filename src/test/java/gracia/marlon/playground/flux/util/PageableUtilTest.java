package gracia.marlon.playground.flux.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import gracia.marlon.playground.flux.dtos.PageDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;

public class PageableUtilTest {

	@Test
	public void pageableUtilInstance() {
		PageableUtil pageableUtil = new PageableUtil();
		assertTrue(pageableUtil instanceof PageableUtil);
	}

	@Test
	public void getPageableSuccessful() {
		PageDTO pageDTO = PageableUtil.getPageable(0, 0);

		assertEquals(0, pageDTO.getPage());
		assertEquals(10, pageDTO.getPageSize());

		pageDTO = PageableUtil.getPageable(null, null);

		assertEquals(0, pageDTO.getPage());
		assertEquals(10, pageDTO.getPageSize());

		pageDTO = PageableUtil.getPageable(20, 30);

		assertEquals(19, pageDTO.getPage());
		assertEquals(30, pageDTO.getPageSize());

	}

	@Test
	public void getPagedResponseSuccessful() {
		List<String> content = Arrays.asList("first", "second", "third");
		PageDTO page = PageableUtil.getPageable(0, 10);

		PagedResponse<String> pagedResponse = PageableUtil.getPagedResponse(page, 3L, content);

		assertEquals(1, pagedResponse.getPage());
		assertEquals(content, pagedResponse.getResults());
		assertEquals(1, pagedResponse.getTotalPages());
		assertEquals(3, pagedResponse.getTotalResults());
	}

}
