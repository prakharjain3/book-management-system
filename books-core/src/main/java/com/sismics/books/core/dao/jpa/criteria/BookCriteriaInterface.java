package com.sismics.books.core.dao.jpa.criteria;

import java.util.List;

public interface BookCriteriaInterface {
    public String getSearch();

    public void setSearch(String search);

    public Boolean getRead();

    public void setRead(Boolean read);

    public String getUserId();

    public void setUserId(String userId);

    public List<String> getTagIdList();

    public void setTagIdList(List<String> idList);


    public void setFavourite(int favourite);

    public int getFavourite();

    public void setMinRating(Integer rating);

    public Integer getMinRating();

    public void setAuthorNames(List<String> authorNames);

    public List<String> getAuthorNames();

    public void setGenreList(List<String> idList);

    public List<String> getGenreList();
}
