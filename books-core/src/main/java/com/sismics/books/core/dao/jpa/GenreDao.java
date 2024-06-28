package com.sismics.books.core.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.sismics.books.core.model.jpa.Genre;
import com.sismics.util.context.ThreadLocalContext;

public class GenreDao implements BaseDao<Genre> {
    /*
     * Return the entity manager.
     * 
     * @return EntityManager
     */
    private EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

     /**
     * Creates a new genre.
     * 
     * @param genre Genre
     * @return New ID
     * @throws Exception
     */
    public String create(Genre genre) {
        // Create the book
        EntityManager em = getEntityManager();
        em.persist(genre);
        return genre.getId();
    }

    /**
     * Gets a genre by its ID.
     * 
     * @param id Genre ID
     * @return Genre
     */
    public Genre getById(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Genre.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Gets a genre by its name.
     * 
     * @param name Genre name
     * @return Genre
     */
    public Genre getByName(String name) {
        EntityManager em = getEntityManager();
        try {
            return (Genre) em.createQuery("SELECT g FROM Genre g WHERE g.name = :name")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
