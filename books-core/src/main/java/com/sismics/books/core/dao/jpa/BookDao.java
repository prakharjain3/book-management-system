package com.sismics.books.core.dao.jpa;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.books.core.model.jpa.Audiobook;
import com.sismics.books.core.model.jpa.AudiobookAdapter;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Podcast;
import com.sismics.books.core.model.jpa.PodcastAdapter;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.books.core.util.jpa.QueryUtil;
import com.sismics.books.core.model.jpa.Genre;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Book DAO.
 * 
 * @author bgamard
 */
public class BookDao implements BaseDao<Book>{

    /*
     * Return the entity manager.
     * 
     * @return EntityManager
     */
    private EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

    /**
     * Creates a new book.
     * 
     * @param book Book
     * @return New ID
     * @throws Exception
     */
    public String create(Book book) {
        // Create the book
        EntityManager em = getEntityManager();
        em.persist(book);

        return book.getId();
    }
    
    /**
     * Gets a book by its ID.
     * 
     * @param id Book ID
     * @return Book
     */
    public Book getById(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Book.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public AudiobookAdapter getAudiobookAdapterById(String id) {
        try {
            String qq = "select b.BOK_ID_C, b.BOK_TITLE_C, b.BOK_SUBTITLE_C, b.BOK_AUTHOR_C, b.BOK_LANGUAGE_C, b.BOK_PUBLISHDATE_D, b.BOK_DESCRIPTION_C from T_BOOK b where b.BOK_ID_C = :id";
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            Query q = QueryUtil.getNativeQuery(new QueryParam(qq, params));

            q.setFirstResult(0);
            q.setMaxResults(1);
            
            List<Object[]> results = q.getResultList();

            Object[] result = results.get(0);
            AudiobookAdapter audio = new AudiobookAdapter();
            audio.setId((String) result[0]);
            audio.setTitle((String) result[1]);
            audio.setSubtitle((String) result[2]);
            audio.setAuthor((String) result[3]);
            audio.setLanguage((String) result[4]);
            audio.setPublishDate((java.util.Date) result[5]);
            audio.setDescription((String) result[6]);

            return audio;
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public PodcastAdapter getPodcastAdapterById(String id) {
        try {
            String qq = "select b.BOK_ID_C, b.BOK_TITLE_C, b.BOK_SUBTITLE_C, b.BOK_AUTHOR_C, b.BOK_LANGUAGE_C, b.BOK_PUBLISHDATE_D, b.BOK_DESCRIPTION_C from T_BOOK b where b.BOK_ID_C = :id";
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            Query q = QueryUtil.getNativeQuery(new QueryParam(qq, params));

            q.setFirstResult(0);
            q.setMaxResults(1);
            
            List<Object[]> results = q.getResultList();

            Object[] result = results.get(0);
            PodcastAdapter podcast = new PodcastAdapter();
            podcast.setId((String) result[0]);
            podcast.setTitle((String) result[1]);
            podcast.setSubtitle((String) result[2]);
            podcast.setAuthor((String) result[3]);
            podcast.setLanguage((String) result[4]);
            podcast.setPublishDate((java.util.Date) result[5]);
            podcast.setDescription((String) result[6]);

            return podcast;
        } catch (NoResultException e) {
            return null;
        }
    }

    public Podcast getPodcastById(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Podcast.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns a book by its ISBN number (10 or 13)
     * 
     * @param isbn ISBN Number (10 or 13)
     * @return Book
     */
    public Book getByIsbn(String isbn) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select b from Book b where b.isbn10 = :isbn or b.isbn13 = :isbn");
        q.setParameter("isbn", isbn);
        try {
            return (Book) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Set the genres for a book with given ID
     * 
     * @param bookId Book ID
     * @param genreSet Set of Genre
     */
    public void setGenres(String bookId, Set<Genre> genreSet) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        if (tx.isActive()) {
            tx.commit();
        }
        tx.begin();

        Book book = em.find(Book.class, bookId);
        book.setGenres(genreSet);
        em.flush();
        em.refresh(book);
        tx.commit();
    }
}
