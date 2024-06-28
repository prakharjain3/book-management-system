package com.sismics.books.core.util.jpa;

/**
 * Utilities for paginated lists.
 * 
 * @author jtremeaux
 */
public class PaginatedLists {
    /**
     * Default size of a page.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum size of a page.
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Constructs a paginated list.
     * 
     * @param pageSize Size of the page
     * @param offset Offset of the page
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create(Integer pageSize, Integer offset) {
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (offset == null) {
            offset = 0;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        return new PaginatedList<E>(pageSize, offset);
    }
    
    /**
     * Constructs a paginated list with default parameters.
     * 
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create() {
        return create(null, null);
    }
}
