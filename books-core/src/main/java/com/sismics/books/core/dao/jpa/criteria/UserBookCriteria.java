package com.sismics.books.core.dao.jpa.criteria;

import java.util.List;


/**
 * User book criteria.
 *
 * @author bgamard 
 */
public class UserBookCriteria implements BookCriteriaInterface{
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Search query.
     */
    private String search;
    
    /**
     * Read state.
     */
    private Boolean read;

    /**
     * Favourite state.
     */
    private int favourite = 0;
    
    /**
     * Tag IDs.
     */
    private List<String> tagIdList;
    
    /**
     * Getter of userId.
     *
     * @return userId
     */
    @Override
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of search.
     *
     * @return the search
     */
    @Override
    public String getSearch() {
        return search;
    }

    /**
     * Setter of search.
     *
     * @param search search
     */
    @Override
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Getter of tagIdList.
     *
     * @return the tagIdList
     */
    @Override
    public List<String> getTagIdList() {
        return tagIdList;
    }

    /**
     * Setter of tagIdList.
     *
     * @param tagIdList tagIdList
     */
    @Override
    public void setTagIdList(List<String> tagIdList) {
        this.tagIdList = tagIdList;
    }

    /**
     * Getter of read.
     * @return read
     */
    @Override
    public Boolean getRead() {
        return read;
    }

    /**
     * Setter of read.
     * @param read read
     */
    @Override
    public void setRead(Boolean read) {
        this.read = read;
    }

    /**
     * Getter of favourite.
     * @return favourite
     */
    @Override
    public int getFavourite() {
        return favourite;
    }

    /**
     * Setter of favourite.
     * @param favourite favourite
     */
    @Override
    public void setFavourite(int favourite) {
        this.favourite = favourite;
    }

    @Override
    public void setMinRating(Integer rating) {
        throw new UnsupportedOperationException("Unimplemented method 'setMinRating'");
    }

    @Override
    public Integer getMinRating() {
        throw new UnsupportedOperationException("Unimplemented method 'getMinRating'");
    }

    @Override
    public void setAuthorNames(List<String> authorNames) {
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorNames'");
    }

    @Override
    public List<String> getAuthorNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorNames'");
    }

    @Override
    public void setGenreList(List<String> idList) {
        throw new UnsupportedOperationException("Unimplemented method 'setGenreList'");
    }

    @Override
    public List<String> getGenreList() {
        throw new UnsupportedOperationException("Unimplemented method 'getGenreList'");
    }

}
