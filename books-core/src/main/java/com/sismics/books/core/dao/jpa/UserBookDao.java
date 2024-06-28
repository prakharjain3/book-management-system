package com.sismics.books.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.PaginatedQuery;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;


/**
 * User book DAO.
 * 
 * @author bgamard
 */
public class UserBookDao {

    private static final String USER_ID = "userId";

    /*
     * Return the entity manager.
     * 
     * @return EntityManager
     */
    private EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

    /**
     * Creates a new user book.
     * 
     * @param userBook UserBook
     * @return New ID
     * @throws Exception
     */
    public String create(UserBook userBook) {
        // Create the UUID
        userBook.setId(UUID.randomUUID().toString());
        
        // Create the user book
        EntityManager em = getEntityManager();
        em.persist(userBook);
        
        return userBook.getId();
    }

    /**
     * Deletes a user book.
     * 
     * @param id User book ID
     */
    public void delete(String id) {
        EntityManager em = getEntityManager();
            
        // Get the user book
        Query q = em.createQuery("select ub from UserBook ub where ub.id = :id and ub.deleteDate is null");
        q.setParameter("id", id);
        UserBook userBookDb = (UserBook) q.getSingleResult();
        
        // Delete the user book
        Date dateNow = new Date();
        userBookDb.setDeleteDate(dateNow);
    }
    
    /**
     * Return a user book.
     * 
     * @param userBookId User book ID
     * @param userId User ID
     * @return User book
     */
    public UserBook getUserBook(String userBookId, String userId) {
        EntityManager em = getEntityManager();
        String userIdQuery = "";
        if(userId != null){
            userIdQuery = "and ub.userId = :userId ";
        }
        Query q = em.createQuery("select ub from UserBook ub where ub.id = :userBookId " + userIdQuery + "and ub.deleteDate is null");
        q.setParameter("userBookId", userBookId);
        if(userId != null)
            q.setParameter(USER_ID, userId);
        try {
            return (UserBook) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Return a user book
     * @param userBookId User book ID
     * @return User book
     */
    public UserBook getUserBook(String userBookId) {
        return this.getUserBook(userBookId, null);
    }
    
    /**
     * Return a user book by book ID and user ID.
     * 
     * @param bookId Book ID
     * @param userId User ID
     * @return User book
     */
    public UserBook getByBook(String bookId, String userId) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select ub from UserBook ub where ub.bookId = :bookId and ub.userId = :userId and ub.deleteDate is null");
        q.setParameter("bookId", bookId);
        q.setParameter(USER_ID, userId);
        try {
            return (UserBook) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


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
        sb.append(" join T_USER_BOOK ub on ub.UBK_IDBOOK_C = b.BOK_ID_C and ub.UBK_IDUSER_C = :userId and ub.UBK_DELETEDATE_D is null ");
        
        // Adds search criteria
        if (!Strings.isNullOrEmpty(criteria.getSearch())) {
            criteriaList.add(" (b.BOK_TITLE_C like :search or b.BOK_SUBTITLE_C like :search or b.BOK_AUTHOR_C like :search) ");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        if (criteria.getTagIdList() != null && !criteria.getTagIdList().isEmpty()) {
            int index = 0;
            for (String tagId : criteria.getTagIdList()) {
                sb.append(" left join T_USER_BOOK_TAG ubk" + index + " on ubk" + index + ".BOT_IDUSERBOOK_C = ub.UBK_ID_C and ubk" + index + ".BOT_IDTAG_C = :tagId" + index + " ");
                criteriaList.add("ubk" + index + ".BOT_ID_C is not null");
                parameterMap.put("tagId" + index, tagId);
                index++;
            }
        }
        if (criteria.getRead() != null) {
            criteriaList.add(" ub.UBK_READDATE_D is " + (Boolean.TRUE.equals(criteria.getRead()) ? "not" : "") + " null ");
        }

        if (criteria.getFavourite() != 0) {
            criteriaList.add(" ub.UBK_FAV_C = :favourite ");
            parameterMap.put("favourite", criteria.getFavourite());
        }
        parameterMap.put(USER_ID, criteria.getUserId());
        
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
    public void findByCriteria(PaginatedList<UserBookDto> paginatedList, UserBookCriteria criteria, SortCriteria sortCriteria) {
        // Execute query
        QueryParam queryParam = getQueryByCriteria(criteria);
        List<Object[]> l = PaginatedQuery.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        List<UserBookDto> userBookDtoList = assembleResults(l);

        paginatedList.setResultList(userBookDtoList);
    }
}
