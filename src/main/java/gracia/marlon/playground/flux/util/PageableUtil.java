package gracia.marlon.playground.flux.util;

import java.util.List;

import gracia.marlon.playground.flux.dtos.PageDTO;
import gracia.marlon.playground.flux.dtos.PagedResponse;

public class PageableUtil {

	private final static int PAGE_BASE = 1;

	private final static int PAGE_MIN_SIZE = 1;

	private final static int PAGE_DEFAULT_SIZE = 10;

	public static PageDTO getPageable(Integer page, Integer pageSize) {
		int pageInt = page == null || page < PAGE_BASE ? PAGE_BASE : page;
		int pageSizeInt = pageSize == null || pageSize < PAGE_MIN_SIZE ? PAGE_DEFAULT_SIZE : pageSize;
		int offset = (pageInt - PAGE_BASE) * pageSizeInt;

		PageDTO pageDTO = new PageDTO();
		pageDTO.setPage(pageInt - PAGE_BASE);
		pageDTO.setPageSize(pageSizeInt);
		pageDTO.setOffset(offset);

		return pageDTO;
	}

	public static <A, B> PagedResponse<B> getPagedResponse(PageDTO page, Long total, List<B> response) {
		PagedResponse<B> pagedResponse = new PagedResponse<B>();
		pagedResponse.setPage(page.getPage() + PAGE_BASE);
		pagedResponse.setTotalPages((int) Math.ceil((double) total / page.getPageSize()));
		pagedResponse.setTotalResults(total);
		pagedResponse.setResults(response);

		return pagedResponse;
	}

}
