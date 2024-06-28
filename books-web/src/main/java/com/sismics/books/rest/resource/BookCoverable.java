package com.sismics.books.rest.resource;

import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.core.Response;

public interface BookCoverable {
    Response add(String id) throws  JSONException;
    Response updateCover(String id, String url) throws JSONException;
    Response get(String id) throws JSONException;
}
