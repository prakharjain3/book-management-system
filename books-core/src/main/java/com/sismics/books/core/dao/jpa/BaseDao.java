package com.sismics.books.core.dao.jpa;

public interface BaseDao<T> {
    String create(T entity) throws Exception;
    Object getById(String id);
}
