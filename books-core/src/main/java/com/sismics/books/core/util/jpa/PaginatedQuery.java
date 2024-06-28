package com.sismics.books.core.util.jpa;

import java.util.List;

import javax.persistence.Query;

public class PaginatedQuery {
	/**
	 * Executes a native count(*) request to count the number of results.
	 * 
	 * @param paginatedList Paginated list object containing parameters, and into
	 *                      which results are added by side effects
	 * @param queryParam    Query parameters
	 */
	private static <E> void executeCountQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
		String sb = "select count(*) as result_count from (" + queryParam.getQueryString() + ") as t1";

		QueryParam countQueryParam = new QueryParam(sb, queryParam.getParameterMap());

		Query q = QueryUtil.getNativeQuery(countQueryParam);

		Number resultCount = (Number) q.getSingleResult();
		paginatedList.setResultCount(resultCount.intValue());
	}

	/**
	 * Executes a query and returns the data of the currunt page.
	 * 
	 * @param em            EntityManager
	 * @param paginatedList Paginated list object containing parameters, and into
	 *                      which results are added by side effects
	 * @param queryParam    Query parameters
	 * @return List of results
	 */
	@SuppressWarnings("unchecked")
	private static <E> List<Object[]> executeResultQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
		Query q = QueryUtil.getNativeQuery(queryParam);

		q.setFirstResult(paginatedList.getOffset());
		q.setMaxResults(paginatedList.getLimit());
		return q.getResultList();
	}

	/**
	 * Executes a paginated request with 2 native queries (one to count the number
	 * of results, and one to return the page).
	 * 
	 * @param paginatedList Paginated list object containing parameters, and into
	 *                      which results are added by side effects
	 * @param queryParam    Query parameters
	 * @return List of results
	 */
	public static <E> List<Object[]> executePaginatedQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
		return executePaginatedQuery(paginatedList, queryParam, null);
	}

	/**
	 * Executes a paginated request with 2 native queries (one to count the number
	 * of results, and one to return the page).
	 * 
	 * @param paginatedList Paginated list object containing parameters, and into
	 *                      which results are added by side effects
	 * @param queryParam    Query parameters
	 * @param sortCriteria  Sort criteria
	 * @return List of results
	 */
	public static <E> List<Object[]> executePaginatedQuery(PaginatedList<E> paginatedList, QueryParam queryParam, SortCriteria sortCriteria) {
		QueryParam sortedQueryParam = queryParam;
		if (sortCriteria != null) {
			StringBuilder sb = new StringBuilder(queryParam.getQueryString());
			sb.append(" order by c");
			sb.append(sortCriteria.getColumn());
			sb.append(sortCriteria.isAsc() ? " asc" : " desc");
			sortedQueryParam = new QueryParam(sb.toString(), queryParam.getParameterMap());
		}

		executeCountQuery(paginatedList, sortedQueryParam);
		return executeResultQuery(paginatedList, sortedQueryParam);
	}
}
