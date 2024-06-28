package com.sismics.books.core.service;

import com.sismics.books.core.model.jpa.Audiobook;

public interface CreatorFactory {
    public Audiobook buildContent(); 
}