package com.sismics.books.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.books.core.dao.jpa.dto.LibraryBookDto;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.LibraryBook;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.PaginatedQuery;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.books.core.dao.jpa.criteria.LibraryBookCriteria;

public class LibraryBookDao implements BaseDao<LibraryBook> {
    // Encapsulating the EntityManager
    private EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

    /**
     * Creates a new library book.
     * 
     * @param libraryBook LibraryBook
     * @return New ID
     * @throws Exception
     */
    public String create(LibraryBook libraryBook) {
        libraryBook.setId(UUID.randomUUID().toString());

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(libraryBook);
        return libraryBook.getId();
    }

    /**
     * Gets a library book by its ID.
     * 
     * @param id LibraryBook ID
     * @return LibraryBook
     */
    public LibraryBook getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(LibraryBook.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public void addRatingForBook(String bookId, int rating) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            tx.commit();
        }
        tx.begin();
        LibraryBook lb = getByBookId(bookId);

        lb.updateAvgRating(rating);
        em.flush();
        em.refresh(lb);
        tx.commit();
    }

    /**
     * @author : Prakhar Jain
     */
    public LibraryBook getByBookId(String bookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select lb from LibraryBook lb where lb.bookId = :bookId");
        q.setParameter("bookId", bookId);
        try {
            return (LibraryBook) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get all the books in the table
     * 
     * @param paginatedList PaginatedList<LibraryBookDto>
     * @return void
     */
    public void getAllBooks(PaginatedList<LibraryBookDto> paginatedList) {
        StringBuilder sb = new StringBuilder(
                "select lb.LBK_ID_C c0, b.BOK_ID_C c1, b.BOK_TITLE_C c2, b.BOK_SUBTITLE_C c3, b.BOK_AUTHOR_C c4, b.BOK_LANGUAGE_C c5, b.BOK_PUBLISHDATE_D c6, lb.LBK_NUMRATINGS_I c7, LBK_AVGRATING_F c8");
        sb.append(" from T_BOOK b ");
        sb.append(" join T_LIBRARY_BOOK lb on lb.LBK_IDBOOK_C = b.BOK_ID_C and lb.LBK_DELETEDATE_D is null");

        Map<String, Object> parameterMap = new HashMap<>();
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        List<Object[]> l = PaginatedQuery.executePaginatedQuery(paginatedList, queryParam, null);

        List<LibraryBookDto> libraryBookDtoList = new ArrayList<LibraryBookDto>();
        for (Object[] o : l) {
            int i = 0;
            LibraryBookDto libraryBookDto = new LibraryBookDto();
            libraryBookDto.setId((String) o[i++]);
            libraryBookDto.setBookId((String) o[i++]);
            libraryBookDto.setTitle((String) o[i++]);
            libraryBookDto.setSubtitle((String) o[i++]);
            libraryBookDto.setAuthor((String) o[i++]);
            libraryBookDto.setLanguage((String) o[i++]);
            Timestamp publishTimestamp = (Timestamp) o[i++];
            if (publishTimestamp != null) {
                libraryBookDto.setPublishTimestamp(publishTimestamp.getTime());
            }
            libraryBookDto.setNumRatings((int) o[i++]);
            libraryBookDto.setAvgRating((double) o[i++]);

            BookDao bookDao = new BookDao();
            Book book = bookDao.getById(libraryBookDto.getBookId());

            // set genres
            libraryBookDto.setGenres(book.getGenres());

            libraryBookDtoList.add(libraryBookDto);
        }

        paginatedList.setResultList(libraryBookDtoList);
    }

    /**
     * Returns the top 10 books based on highest average rating or number of
     * ratings.
     * If tied, breaks ties arbitrarily.
     * 
     * @param desc      True if descending order, false if ascending order
     * @param avgRating True if sorting by average rating, false if sorting by
     *                  number of ratings
     * @return List of top 10 books as LibraryBookDto
     */
    public List<LibraryBookDto> getTop10Books(boolean desc, boolean avgRating) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String order = desc ? "DESC" : "ASC";
        String rankingField = avgRating ? "lb.avgRating" : "lb.numRatings";
        // Query q = em.createQuery("SELECT lb, b FROM LibraryBook lb JOIN Book b ON
        // lb.bookId = b.id ORDER BY " + rankingField + " " + order)
        // .setMaxResults(10);
        Query q = em
                .createQuery("SELECT lb, b FROM LibraryBook lb, Book b WHERE lb.bookId = b.id ORDER BY " + rankingField
                        + " " + order)
                .setMaxResults(10);
        List<Object[]> resultList = q.getResultList();

        List<LibraryBookDto> libraryBookDtoList = new ArrayList<>();
        for (Object[] result : resultList) {
            LibraryBook libraryBook = (LibraryBook) result[0];
            Book book = (Book) result[1];

            LibraryBookDto libraryBookDto = new LibraryBookDto();
            libraryBookDto.setId(libraryBook.getId());
            libraryBookDto.setBookId(libraryBook.getBookId());
            libraryBookDto.setTitle(book.getTitle());
            libraryBookDto.setSubtitle(book.getSubtitle());
            libraryBookDto.setAuthor(book.getAuthor());
            libraryBookDto.setLanguage(book.getLanguage());
            Timestamp publishTimestamp = (Timestamp) book.getPublishDate();
            if (publishTimestamp != null) {
                libraryBookDto.setPublishTimestamp(publishTimestamp.getTime());
            }
            libraryBookDto.setNumRatings(libraryBook.getNumRatings());
            libraryBookDto.setAvgRating(libraryBook.getAvgRating());
            libraryBookDto.setGenres(book.getGenres());

            libraryBookDtoList.add(libraryBookDto);
        }

        return libraryBookDtoList;
    }

    public List<String> getAuthors() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "SELECT DISTINCT b.BOK_AUTHOR_C FROM T_BOOK b JOIN T_LIBRARY_BOOK lb ON b.BOK_ID_C = lb.LBK_IDBOOK_C");
        return q.getResultList();
    }

    public void findByCriteria(PaginatedList<LibraryBookDto> paginatedList, LibraryBookCriteria criteria,
            SortCriteria sortCriteria) {
        // Prepare query parameters based on criteria
        QueryParam queryParam = getQueryByCriteria(criteria);

        // Execute the paginated query
        List<Object[]> rawResults = PaginatedQuery.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        // Assemble results into DTOs
        List<LibraryBookDto> dtoList = assembleResults(rawResults);

        // Set the DTO list as the result of the paginated list
        paginatedList.setResultList(dtoList);
    }


    private QueryParam getQueryByCriteria(LibraryBookCriteria criteria) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT lb.LBK_ID_C AS c0, b.BOK_ID_C AS c1, b.BOK_TITLE_C AS c2, b.BOK_SUBTITLE_C AS c3, b.BOK_AUTHOR_C AS c4, ");
        sb.append(
                "b.BOK_LANGUAGE_C AS c5, b.BOK_PUBLISHDATE_D AS c6, lb.LBK_NUMRATINGS_I AS c7, lb.LBK_AVGRATING_F AS c8 ");
        sb.append("FROM T_BOOK b ");
        sb.append("JOIN T_LIBRARY_BOOK lb ON lb.LBK_IDBOOK_C = b.BOK_ID_C AND lb.LBK_DELETEDATE_D IS NULL ");

        Map<String, Object> parameterMap = new HashMap<>();

        if (criteria.getSearch() != null && !criteria.getSearch().isEmpty()) {
            sb.append(" AND (b.BOK_TITLE_C LIKE :search OR b.BOK_AUTHOR_C LIKE :search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        
        if (criteria.getGenreList() != null && !criteria.getGenreList().isEmpty()) {
            int index = 0;
            sb.append(" AND (");
            for (String genreName : criteria.getGenreList()) {
                if (index > 0) {
                    sb.append(" OR");
                }
                sb.append(" EXISTS (SELECT 1 FROM T_BOOK_GENRE bg, T_GENRE g WHERE bg.BOK_ID_C = b.BOK_ID_C AND bg.GNR_ID_C = g.GNR_ID_C AND g.GNR_NAME_C = :genreName" + index + ")");
                parameterMap.put("genreName" + index, genreName);
                index++;
            }
            sb.append(")");
        }

        if (criteria.getAuthorNames() != null && !criteria.getAuthorNames().isEmpty()) {
            int index = 0;
            sb.append(" AND (");
            for (String authorName : criteria.getAuthorNames()) {
                if (index > 0) {
                    sb.append(" OR");
                }
                sb.append(" b.BOK_AUTHOR_C = :author" + index);
                parameterMap.put("author" + index, authorName);
                index++;
            }
            sb.append(")");
        }

        if (criteria.getMinRating() > 0) { // Assuming 0 is a valid rating, adjust as needed
            sb.append(" AND lb.LBK_AVGRATING_F > :minRating");
            parameterMap.put("minRating", criteria.getMinRating());
        }


        return new QueryParam(sb.toString(), parameterMap);
    }


    private List<LibraryBookDto> assembleResults(List<Object[]> rawResults) {
        List<LibraryBookDto> results = new ArrayList<>();

        for (Object[] row : rawResults) {
            LibraryBookDto libraryBookDto = new LibraryBookDto();
            libraryBookDto.setId((String) row[0]);
            libraryBookDto.setBookId((String) row[1]);
            libraryBookDto.setTitle((String) row[2]);
            libraryBookDto.setSubtitle((String) row[3]);
            libraryBookDto.setAuthor((String) row[4]);
            libraryBookDto.setLanguage((String) row[5]);
            Timestamp publishTimestamp = (Timestamp) row[6];
            if (publishTimestamp != null) {
                libraryBookDto.setPublishTimestamp(publishTimestamp.getTime());
            }
            libraryBookDto.setNumRatings((Integer) row[7]);
            libraryBookDto.setAvgRating((Double) row[8]);
    

            BookDao bookDao = new BookDao();
            Book book = bookDao.getById(libraryBookDto.getBookId());

            // set genres
            libraryBookDto.setGenres(book.getGenres());

            results.add(libraryBookDto);
        }

        return results;
    }

}
