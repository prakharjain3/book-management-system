package com.sismics.books.core.service;

import org.codehaus.jackson.JsonNode;

public interface ServiceStrategy {

    public JsonNode callAPI(String name, boolean isAudiobook) throws Exception;
    
}
