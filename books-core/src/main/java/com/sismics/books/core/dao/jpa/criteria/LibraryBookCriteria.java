package com.sismics.books.core.dao.jpa.criteria;

import java.util.List;

public class LibraryBookCriteria implements BookCriteriaInterface {
    private List<String> genreList;
    private String search;
    private List<String> authorNames;
    private int minRating;

    @Override
    public List<String> getGenreList() {
        return genreList;
    }
    @Override
    public void setGenreList(List<String> genreList) {
        this.genreList = genreList;
    }
    @Override
    public String getSearch() {
        return search;
    }
    @Override
    public void setSearch(String search) {
        this.search = search;
    }
    @Override
    public Integer getMinRating() {
        return minRating;
    }
    @Override
    public void setMinRating(Integer rating) {
        this.minRating = rating;
    }
    @Override
    public List<String> getAuthorNames() {
        return authorNames;
    }
    @Override
    public void setAuthorNames(List<String> authorNames) {
        this.authorNames = authorNames;
    }
    @Override
    public Boolean getRead() {
        throw new UnsupportedOperationException("Unimplemented method 'getRead'");
    }
    @Override
    public void setRead(Boolean read) {
        throw new UnsupportedOperationException("Unimplemented method 'setRead'");
    }
    @Override
    public String getUserId() {
        throw new UnsupportedOperationException("Unimplemented method 'getUserId'");
    }
    @Override
    public void setUserId(String userId) {
        throw new UnsupportedOperationException("Unimplemented method 'setUserId'");
    }
    @Override
    public void setFavourite(int favourite) {
        throw new UnsupportedOperationException("Unimplemented method 'setFavourite'");
    }
    @Override
    public int getFavourite() {
        throw new UnsupportedOperationException("Unimplemented method 'getFavourite'");
    }
    @Override
    public List<String> getTagIdList() {
        throw new UnsupportedOperationException("Unimplemented method 'getTagIdList'");
    }
    @Override
    public void setTagIdList(List<String> idList) {
        throw new UnsupportedOperationException("Unimplemented method 'setTagIdList'");
    }
}
