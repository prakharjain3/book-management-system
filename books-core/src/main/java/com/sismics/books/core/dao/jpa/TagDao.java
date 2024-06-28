package com.sismics.books.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;


import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.model.jpa.UserBookTag;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedQuery;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Tag DAO.
 * 
 * @author bgamard
 */
public class TagDao implements BaseDao<Tag>{
    private static EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

    private Set<String> fetchCurrentTagIds(String userBookId) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select bt.tagId from UserBookTag bt where bt.userBookId = :userBookId");
        query.setParameter("userBookId", userBookId);
        List<String> currentTagIds = (List<String>) query.getResultList();
        return new HashSet<>(currentTagIds);
    }

    // Determines which tags to add and which to remove
    private Map<String, Set<String>> determineTagsToUpdate(Set<String> currentTagIds, Set<String> newTagIds) {
        Set<String> tagsToRemove = new HashSet<>(currentTagIds);
        tagsToRemove.removeAll(newTagIds);

        Set<String> tagsToAdd = new HashSet<>(newTagIds);
        tagsToAdd.removeAll(currentTagIds);

        Map<String, Set<String>> updateMap = new HashMap<>();
        updateMap.put("remove", tagsToRemove);
        updateMap.put("add", tagsToAdd);

        return updateMap;
    }

    // Removes tags from a user book
    private void removeTags(String userBookId, Set<String> tagsToRemove) {
        if (!tagsToRemove.isEmpty()) {
            EntityManager em = getEntityManager();
            Query deleteQuery = em.createQuery("delete from UserBookTag bt where bt.userBookId = :userBookId and bt.tagId in :tagsToRemove");
            deleteQuery.setParameter("userBookId", userBookId);
            deleteQuery.setParameter("tagsToRemove", tagsToRemove);
            deleteQuery.executeUpdate();
        }
    }

    // Adds tags to a user book
    private void addTags(String userBookId, Set<String> tagsToAdd) {
        EntityManager em = getEntityManager();
        for (String tagId : tagsToAdd) {
            UserBookTag userBookTag = new UserBookTag();
            userBookTag.setId(UUID.randomUUID().toString());
            userBookTag.setUserBookId(userBookId);
            userBookTag.setTagId(tagId);
            em.persist(userBookTag);
        }
    }

    private Tag getSingleResultOrNull(Query q) {
        try {
            return (Tag) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Gets a tag by its ID.
     * 
     * @param id Tag ID
     * @return Tag
     */
    public Tag getById(String id) {
        try {
            return getEntityManager().find(Tag.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns the list of all tags.
     * 
     * @return List of tags
     */
    @SuppressWarnings("unchecked")
    public List<Tag> getByUserId(String userId) {
        Query q = getEntityManager().createQuery("select t from Tag t where t.userId = :userId and t.deleteDate is null order by t.name");
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    /**
     * Update tags on a user book.
     * 
     * @param userBookId
     * @param tagIdSet
     */
//    public void updateTagList(String userBookId, Set<String> tagIdSet) {
//        // Delete old tag links
//        EntityManager em = getEntityManager();
//        Query q = em.createQuery("delete UserBookTag bt where bt.userBookId = :userBookId");
//        q.setParameter("userBookId", userBookId);
//        q.executeUpdate();
//
//        // Create new tag links
//        for (String tagId : tagIdSet) {
//            UserBookTag userBookTag = new UserBookTag();
//            userBookTag.setId(UUID.randomUUID().toString());
//            userBookTag.setUserBookId(userBookId);
//            userBookTag.setTagId(tagId);
//            em.persist(userBookTag);
//        }
//    }

    public void updateTagList(String userBookId, Set<String> newTagIds) {
        Set<String> currentTagIds = fetchCurrentTagIds(userBookId);
        Map<String, Set<String>> tagsToUpdate = determineTagsToUpdate(currentTagIds, newTagIds);

        removeTags(userBookId, tagsToUpdate.get("remove"));
        addTags(userBookId, tagsToUpdate.get("add"));
    }

    /**
     * Returns tag list on a user book.
     * @param userBookId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TagDto> getByUserBookId(String userBookId) {
        String sb = "select t.TAG_ID_C, t.TAG_NAME_C, t.TAG_COLOR_C, t.TAG_VISIBILITY_C from T_USER_BOOK_TAG bt join T_TAG t on t.TAG_ID_C = bt.BOT_IDTAG_C where bt.BOT_IDUSERBOOK_C = :userBookId and t.TAG_DELETEDATE_D is null order by t.TAG_NAME_C ";
        
        // Perform the query
        Query q = getEntityManager().createNativeQuery(sb);
        q.setParameter("userBookId", userBookId);
        List<Object[]> l = q.getResultList();
        
        // Assemble results
        List<TagDto> tagDtoList = new ArrayList<TagDto>();
        for (Object[] o : l) {
            int i = 0;
            TagDto tagDto = new TagDto();
            tagDto.setId((String) o[i++]);
            tagDto.setName((String) o[i++]);
            tagDto.setColor((String) o[i++]);
            tagDto.setVisibility((String) o[i++]);
            tagDtoList.add(tagDto);
        }
        return tagDtoList;
    }

    /**
     * make a tag public
     * @return void
     */
    public void makePublic(String tagId) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("update Tag t set t.visibility = 'public' where t.id = :tagId");
        q.setParameter("tagId", tagId);
        q.executeUpdate();
    }


    /**
     * Get the public tags.
     * 
     * @return List of public tags
     */
    @SuppressWarnings("unchecked")
    public List<Tag> getPublicTags() {
        Query q = getEntityManager().createQuery("select t from Tag t where t.visibility = 'public' and t.deleteDate is null order by t.name");
        return q.getResultList();
    }
    
    // /**
    //  * Get public userbooks
    //  * @param tagId
    //  * @return List of public books
    //  */
    // public List<UserBookDto> getPublicUserBooks(PaginatedList<UserBookDto> paginatedList, UserBookCriteria criteria, SortCriteria sortCriteria) {
    //     // tagId = 
    //     Query q = getEntityManager().createQuery("SELECT ub FROM UserBook ub LEFT JOIN T_USER_BOOK_TAG ubt ON ub.UBK_ID_C = ubt.BOT_IDUSERBOOK_C LEFT JOIN Tag t ON t.TAG_ID_C = ubt.BOT_IDTAG_C WHERE t.TAG_ID_C = :tagId AND t.visibility = 'public' AND t.deleteDate IS NULL");
    //     q.setParameter("tagId", tagId);
    //     return q.getResultList();
    // }

    /**
     * Creates a new tag.
     * 
     * @param tag Tag
     * @return New ID
     * @throws Exception
     */
    public String create(Tag tag) {
        // Create the UUID
        tag.setId(UUID.randomUUID().toString());
        
        // Create the tag
        EntityManager em = getEntityManager();
        tag.setCreateDate(new Date());
        em.persist(tag);
        
        return tag.getId();
    }

    /**
     * Returns a tag by name.
     * @param userId User ID
     * @param name Name
     * @return Tag
     */
    public Tag getByName(String userId, String name) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select t from Tag t where t.name = :name and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("name", name);

        return getSingleResultOrNull(q);
    }

    /**
     * Returns a tag only by name.
     * @param tagId
     * @return
     */
    public Tag getByNameOnly(String name) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select t from Tag t where t.name = :name and t.deleteDate is null");
        q.setParameter("name", name);

        return getSingleResultOrNull(q);
    }
    
    /**
     * get public Tags by name
     * @param tagId
     * @return
     */
    public Tag getPublicByID(String tagId) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select t from Tag t where t.id = :tagId and t.visibility = 'public' and t.deleteDate is null");
        q.setParameter("tagId", tagId);
        // q.setParameter("visibility", visibility);

        return getSingleResultOrNull(q);
    }

    /**
     * Returns a tag by ID.
     * @param userId User ID
     * @param tagId Tag ID
     * @return Tag
     */
    public Tag getByTagId(String userId, String tagId) {
        Query q = getEntityManager().createQuery("select t from Tag t where t.id = :tagId and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("tagId", tagId);
        return getSingleResultOrNull(q);
    }
    
    /**
     * Deletes a tag.
     * 
     * @param tagId Tag ID
     */
    public void delete(String tagId) {
        EntityManager em = getEntityManager();
            
        // Get the tag
        Query q = em.createQuery("select t from Tag t where t.id = :id and t.deleteDate is null");
        q.setParameter("id", tagId);
        Tag tagDb = getSingleResultOrNull(q);
        
        // Delete the tag
        Date dateNow = new Date();
        assert tagDb != null;
        tagDb.setDeleteDate(dateNow);

        // Delete linked data
        q = em.createQuery("delete UserBookTag bt where bt.tagId = :tagId");
        q.setParameter("tagId", tagId);
        q.executeUpdate();
    }

    /**
     * Search tags by name.
     * 
     * @param name Tag name
     * @return List of found tags
     */
//    @SuppressWarnings("unchecked")
//    public List<Tag> findByName(String userId, String name) {
//        EntityManager em = ThreadLocalContext.get().getEntityManager();
//        Query q = em.createQuery("select t from Tag t where t.name like :name and t.userId = :userId and t.deleteDate is null");
//        q.setParameter("userId", userId);
//        q.setParameter("name", "%" + name + "%");
//        return q.getResultList();
//    }

    /*
     * Return the query to search user books by criteria.
     * @params criteria Search criteria
     * @return Query
     */
    public QueryParam getQueryByCriteria(UserBookCriteria criteria) {


        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder("select ub.UBK_ID_C c0, b.BOK_TITLE_C c1, b.BOK_SUBTITLE_C c2, b.BOK_AUTHOR_C c3, b.BOK_LANGUAGE_C c4, b.BOK_PUBLISHDATE_D c5, ub.UBK_CREATEDATE_D c6, ub.UBK_READDATE_D c7, b.BOK_ID_C c8, ub.UBK_TYPE_C c9");
        sb.append(" from T_BOOK b ");
        sb.append(" join T_USER_BOOK ub on ub.UBK_IDBOOK_C = b.BOK_ID_C and ub.UBK_DELETEDATE_D is null ");
        
        // Adds search criteria
        // if (!Strings.isNullOrEmpty(criteria.getSearch())) {
        //     criteriaList.add(" (b.BOK_TITLE_C like :search or b.BOK_SUBTITLE_C like :search or b.BOK_AUTHOR_C like :search) ");
        //     parameterMap.put("search", "%" + criteria.getSearch() + "%");
        // }
        if (criteria.getTagIdList() != null && !criteria.getTagIdList().isEmpty()) {
            int index = 0;
            for (String tagId : criteria.getTagIdList()) {
                sb.append(" left join T_USER_BOOK_TAG ubk" + index + " on ubk" + index + ".BOT_IDUSERBOOK_C = ub.UBK_ID_C and ubk" + index + ".BOT_IDTAG_C = :tagId" + index + " ");
                sb.append(" left join T_TAG t on t.TAG_ID_C = ubk" + index + ".BOT_IDTAG_C and t.TAG_VISIBILITY_C = 'public' ");
                
                criteriaList.add("ubk" + index + ".BOT_ID_C is not null");
                parameterMap.put("tagId" + index, tagId);
                index++;
            }
        }
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        return new QueryParam(sb.toString(), parameterMap);
    }

    /*
     * Assembles the results of a query.
     * 
     * @param l List of results
     * @return List of user books
     */
    public List<UserBookDto> assembleResults(List<Object[]> l){
        List<UserBookDto> userBookDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            UserBookDto userBookDto = new UserBookDto();
            userBookDto.setId((String) o[i++]);
            userBookDto.setTitle((String) o[i++]);
            userBookDto.setSubtitle((String) o[i++]);
            userBookDto.setAuthor((String) o[i++]);
            userBookDto.setLanguage((String) o[i++]);
            Timestamp publishTimestamp = (Timestamp) o[i++];
            if (publishTimestamp != null) {
                userBookDto.setPublishTimestamp(publishTimestamp.getTime());
            }
            userBookDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            Timestamp readTimestamp = (Timestamp) o[i++];
            if (readTimestamp != null) {
                userBookDto.setReadTimestamp(readTimestamp.getTime());
            }
            userBookDto.setBookId((String) o[i++]);
            userBookDto.setType((String) o[i++]);

            userBookDtoList.add(userBookDto);
        }

        return userBookDtoList;
    }
    
    /**
     * Searches user books by criteria.
     * 
     * @param paginatedList List of user books (updated by side effects)
     * @param criteria Search criteria
     * @return List of user books
     */
    public void getPublicBooks(PaginatedList<UserBookDto> paginatedList, UserBookCriteria criteria, SortCriteria sortCriteria) {
        // Execute query
        if (criteria.getTagIdList() != null && !criteria.getTagIdList().isEmpty()) {

            QueryParam queryParam = getQueryByCriteria(criteria);
            List<Object[]> l = PaginatedQuery.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
            List<UserBookDto> userBookDtoList = assembleResults(l);
            paginatedList.setResultList(userBookDtoList);
        } else {
            paginatedList.setResultList(new ArrayList<UserBookDto>());
        }
    }
}
