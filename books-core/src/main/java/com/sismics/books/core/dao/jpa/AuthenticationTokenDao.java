package com.sismics.books.core.dao.jpa;

import com.sismics.books.core.model.jpa.AuthenticationToken;
import com.sismics.util.context.ThreadLocalContext;
import org.joda.time.DateTime;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Authentication token DAO.
 * 
 * @author jtremeaux
 */
public class AuthenticationTokenDao {
    /**
     * Gets an authentication token.
     * 
     * @param id Authentication token ID
     * @return Authentication token
     */

    private static EntityManager getEntityManager(){
        return ThreadLocalContext.get().getEntityManager();
    }
    public AuthenticationToken get(String id) {
        EntityManager em = getEntityManager();
        return em.find(AuthenticationToken.class, id);
    }

    /**
     * Creates a new authentication token.
     * 
     * @param authenticationToken Authentication token
     * @return Authentication token ID
     */
    public String create(AuthenticationToken authenticationToken) {
        EntityManager em = getEntityManager();
            
        authenticationToken.setId(UUID.randomUUID().toString());
        authenticationToken.setCreationDate(new Date());
        em.persist(authenticationToken);
        
        return authenticationToken.getId();
    }

    /**
     * Deletes the authentication token.
     * 
     * @param authenticationTokenId Authentication token ID
     * @throws Exception
     */
    public void delete(String authenticationTokenId) throws Exception {
        EntityManager em = getEntityManager();
        AuthenticationToken authenticationToken = em.find(AuthenticationToken.class, authenticationTokenId);
        if (authenticationToken != null) {
            em.remove(authenticationToken);
        } else {
            throw new Exception("Token not found: " + authenticationTokenId);
        }
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param userId User ID
     * @throws Exception
     */
    public void deleteOldSessionToken(String userId) {
        String sb = "delete from T_AUTHENTICATION_TOKEN AS ato " + " where ato.AUT_IDUSER_C = :userId and ato.AUT_LONGLASTED_B = :longLasted" +
                " and ato.AUT_LASTCONNECTIONDATE_D < :minDate ";

        EntityManager em = getEntityManager();
        Query q = em.createNativeQuery(sb);
        q.setParameter("userId", userId);
        q.setParameter("longLasted", false);
        q.setParameter("minDate", DateTime.now().minusDays(1).toDate());
        q.executeUpdate();
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param id Token id
     * @throws Exception
     */
    public void updateLastConnectionDate(String id) {
        String sb = "update T_AUTHENTICATION_TOKEN ato " + " set ato.AUT_LASTCONNECTIONDATE_D = :currentDate " +
                " where ato.AUT_ID_C = :id";

        EntityManager em = getEntityManager();
        Query q = em.createNativeQuery(sb);
        q.setParameter("currentDate", new Date());
        q.setParameter("id", id);
        q.executeUpdate();
    }
    
    /**
     * Returns all authentication tokens of an user.
     * 
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AuthenticationToken> getByUserId(String userId) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("select a from AuthenticationToken a where a.userId = :userId");
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    /**
     * Deletes all authentication tokens of an user.
     * 
     * @param userId
     */
    public void deleteByUserId(String userId, String id) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery("delete AuthenticationToken a where a.userId = :userId and a.id != :id");
        q.setParameter("userId", userId);
        q.setParameter("id", id);
        q.executeUpdate();
    }
}
