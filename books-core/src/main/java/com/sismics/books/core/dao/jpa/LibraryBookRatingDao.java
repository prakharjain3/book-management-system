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
import com.sismics.books.core.model.jpa.LibraryBookRating;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedQuery;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

public class LibraryBookRatingDao implements BaseDao<LibraryBookRating>{
    
    private EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

    /**
     * Creates a new library book rating
     * 
     * @param libraryBookRating LibraryBookRating
     * @return New ID
     * @throws Exception
     */
    public String create(LibraryBookRating libraryBookRating) {
        libraryBookRating.setId(UUID.randomUUID().toString());

        EntityManager em = getEntityManager();
        em.persist(libraryBookRating);
        return libraryBookRating.getId();
    }

    /**
     * Deletes a library book rating.
     * 
     * @param id User book ID
     */
    public void delete(String id) {
        EntityManager em = getEntityManager();
            
        // Get the user book
        Query q = em.createQuery("select lbr from LibraryBookRating lbr where lbr.id = :id and lbr.deleteDate is null");
        q.setParameter("id", id);
        LibraryBookRating libraryBookRatingDb = (LibraryBookRating) q.getSingleResult();
        
        // Delete the user book
        Date dateNow = new Date();
        libraryBookRatingDb.setDeleteDate(dateNow);
    }

    /**
     * Get by ID
     * 
     * @param id LibraryBookRating ID
     * @return LibraryBookRating
     */
    public LibraryBookRating getById(String id) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select lbr from LibraryBookRating lbr where lbr.id = :id and lbr.deleteDate is null");
        q.setParameter("id", id);
        try {
            return (LibraryBookRating) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get by userID and bookId
     * @param userId User ID
     * @param bookId Book ID
     * @return LibraryBookRating
     */
    public LibraryBookRating getByUserIdAndBookId(String userId, String bookId) {
        EntityManager em = getEntityManager();
        if (userId != null) {
            Query q = em.createQuery("select lbr from LibraryBookRating lbr where lbr.userId = :userId and lbr.bookId = :bookId and lbr.deleteDate is null");
            q.setParameter("userId", userId);
            q.setParameter("bookId", bookId);
            try {
                return (LibraryBookRating) q.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    // /*
    //  * Return the query to search user books by criteria.
    //  * @params criteria Search criteria
    //  * @return Query
    //  */
    // public QueryParam getQueryByCriteria(UserBookCriteria criteria) {
    //     Map<String, Object> parameterMap = new HashMap<>();
    //     List<String> criteriaList = new ArrayList<>();
        
    //     StringBuilder sb = new StringBuilder("select ub.UBK_ID_C c0, b.BOK_TITLE_C c1, b.BOK_SUBTITLE_C c2, b.BOK_AUTHOR_C c3, b.BOK_LANGUAGE_C c4, b.BOK_PUBLISHDATE_D c5, ub.UBK_CREATEDATE_D c6, ub.UBK_READDATE_D c7");
    //     sb.append(" from T_BOOK b ");
    //     sb.append(" join T_USER_BOOK ub on ub.UBK_IDBOOK_C = b.BOK_ID_C and ub.UBK_IDUSER_C = :userId and ub.UBK_DELETEDATE_D is null ");
        
    //     // Adds search criteria
    //     if (!Strings.isNullOrEmpty(criteria.getSearch())) {
    //         criteriaList.add(" (b.BOK_TITLE_C like :search or b.BOK_SUBTITLE_C like :search or b.BOK_AUTHOR_C like :search) ");
    //         parameterMap.put("search", "%" + criteria.getSearch() + "%");
    //     }
    //     if (criteria.getTagIdList() != null && !criteria.getTagIdList().isEmpty()) {
    //         int index = 0;
    //         for (String tagId : criteria.getTagIdList()) {
    //             sb.append(" left join T_USER_BOOK_TAG ubk" + index + " on ubk" + index + ".BOT_IDUSERBOOK_C = ub.UBK_ID_C and ubk" + index + ".BOT_IDTAG_C = :tagId" + index + " ");
    //             criteriaList.add("ubk" + index + ".BOT_ID_C is not null");
    //             parameterMap.put("tagId" + index, tagId);
    //             index++;
    //         }
    //     }
    //     if (criteria.getRead() != null) {
    //         criteriaList.add(" ub.UBK_READDATE_D is " + (Boolean.TRUE.equals(criteria.getRead()) ? "not" : "") + " null ");
    //     }
    //     parameterMap.put("userId", criteria.getUserId());
        
    //     if (!criteriaList.isEmpty()) {
    //         sb.append(" where ");
    //         sb.append(Joiner.on(" and ").join(criteriaList));
    //     }

    //     return new QueryParam(sb.toString(), parameterMap);
    // }

    // /*
    //  * Assembles the results of a query.
    //  * 
    //  * @param l List of results
    //  * @return List of user books
    //  */
    // public List<UserBookDto> assembleResults(List<Object[]> l){
    //     List<UserBookDto> userBookDtoList = new ArrayList<>();
    //     for (Object[] o : l) {
    //         int i = 0;
    //         UserBookDto userBookDto = new UserBookDto();
    //         userBookDto.setId((String) o[i++]);
    //         userBookDto.setTitle((String) o[i++]);
    //         userBookDto.setSubtitle((String) o[i++]);
    //         userBookDto.setAuthor((String) o[i++]);
    //         userBookDto.setLanguage((String) o[i++]);
    //         Timestamp publishTimestamp = (Timestamp) o[i++];
    //         if (publishTimestamp != null) {
    //             userBookDto.setPublishTimestamp(publishTimestamp.getTime());
    //         }
    //         userBookDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
    //         Timestamp readTimestamp = (Timestamp) o[i++];
    //         if (readTimestamp != null) {
    //             userBookDto.setReadTimestamp(readTimestamp.getTime());
    //         }
    //         userBookDtoList.add(userBookDto);
    //     }

    //     return userBookDtoList;
    // }
    
    // /**
    //  * Searches user books by criteria.
    //  * 
    //  * @param paginatedList List of user books (updated by side effects)
    //  * @param criteria Search criteria
    //  * @return List of user books
    //  */
    // public void findByCriteria(PaginatedList<UserBookDto> paginatedList, UserBookCriteria criteria, SortCriteria sortCriteria) {
    //     // Execute query
    //     QueryParam queryParam = getQueryByCriteria(criteria);
    //     List<Object[]> l = PaginatedQuery.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
    //     List<UserBookDto> userBookDtoList = assembleResults(l);

    //     paginatedList.setResultList(userBookDtoList);
    // }
}
