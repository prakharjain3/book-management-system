package com.sismics.books.core.model.jpa;

import java.util.Date;

public interface AssociatedBook {
    String getId();
    void setId(String id);

    String getBookId();
    void setBookId(String bookId);

    Date getCreateDate();
    void setCreateDate(Date createDate);

    Date getDeleteDate();
    void setDeleteDate(Date deleteDate);
}
