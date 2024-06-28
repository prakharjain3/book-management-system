package com.sismics.books.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.dao.jpa.dto.UserDto;
import com.sismics.books.core.model.jpa.User;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.PaginatedQuery;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.books.core.service.UserAuthenticationService;
import com.sismics.books.core.service.PasswordHashService;

// import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

/**
 * User DAO.
 * 
 * @author jtremeaux
 */
public class UserDao implements BaseDao<User>{

    private final PasswordHashService passwordHashService;

    public UserDao() {
        this.passwordHashService = new PasswordHashService();
    }

    // Encapsulating the EntityManager
    private EntityManager getEntityManager() {
        return ThreadLocalContext.get().getEntityManager();
    }

    /**
     * Gets a user by its username.
     * 
     * @param username User's username
     * @return User
     */
    private Query getQueryForUserFromDb(EntityManager em, String parameterName, String parameterValue) {
        Query q = em.createQuery("select u from User u where u." + parameterName + " = :" + parameterName + " and u.deleteDate is null");
        q.setParameter(parameterName, parameterValue);
        return q;
    }

    /**
     * Gets a single result from the database.
     * 
     * @param q Query
     * @return User
     */
    private User getSingleResultFromDb(Query q) {
        return (User) q.getSingleResult();
    }

    /**
     * Gets a list of results from the database.
     * 
     * @param q Query
     * @return List of users
     */
    private List<?> getResultListFromDb(Query q) {
        return q.getResultList();
    }

    /**
     * Deletes linked data.
     * 
     * @param em Entity manager
     * @param userFromDb User from the database
     */
    private void deleteLinkedData(EntityManager em, User userFromDb) {
        Query q = em.createQuery("delete from AuthenticationToken at where at.userId = :userId");
        q.setParameter("userId", userFromDb.getId());
        q.executeUpdate();
    }

    /**
     * Authenticates an user.
     * 
     * @param username User login
     * @param password User password
     * @return ID of the authenticated user or null
     */
    public String authenticate(String username, String password) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
        // Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        // q.setParameter("username", username);
        // try {
        //     User user = (User) q.getSingleResult();
        //     if (!BCrypt.checkpw(password, user.getPassword())) {
        //         return null;
        //     }
        //     return user.getId();
        // } catch (NoResultException e) {
        //     return null;
        // }
        Query q = getQueryForUserFromDb(em, "username", username);
        UserAuthenticationService userAuthenticationService = new UserAuthenticationService(this);
        return userAuthenticationService.authenticate(username, password, q);
    }
    
    /**
     * Creates a new user.
     * 
     * @param user User to create
     * @return User ID
     * @throws Exception
     */
    public String create(User user) throws Exception {

        try {
            
            // Create the user UUID
            user.setId(UUID.randomUUID().toString());
            
            // Checks for user unicity
            // EntityManager em = ThreadLocalContext.get().getEntityManager();
            EntityManager em = getEntityManager();
            // Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
            // q.setParameter("username", user.getUsername());
            Query sameUsernameQuery = getQueryForUserFromDb(em, "username", user.getUsername());
            Query sameEmailQuery = getQueryForUserFromDb(em, "email", user.getEmail());
            // List<?> l = q.getResultList();
            List<?> sameUsernameResults = getResultListFromDb(sameUsernameQuery);
            List<?> sameEmailResults = getResultListFromDb(sameEmailQuery);

            if (sameUsernameResults.size() > 0) {
                throw new Exception("\"User with username \" + user.getUsername() + \" already exists.\"");
                // throw new UserAlreadyExistsException("User with username " + user.getUsername() + " already exists.");
            } else if (!sameEmailResults.isEmpty()) {
                throw new Exception("AlreadyExistingEmail");
            }
            
            user.setCreateDate(new Date());
            user.setPassword(this.passwordHashService.hashPassword(user.getPassword()));
            user.setTheme(Constants.DEFAULT_THEME_ID);
            em.persist(user);
            
            return user.getId();
        } catch (PersistenceException e) {
            throw new Exception("Failed to create user.", e);
        }
    }
    
    /**
     * Updates a user.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User update(User user) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
        
        // Get the user
        // Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        // q.setParameter("id", user.getId());
        Query q = getQueryForUserFromDb(em, "id", user.getId());
        // User userFromDb = (User) q.getSingleResult();
        User userFromDb = getSingleResultFromDb(q);

        // Update the user
        userFromDb.setLocaleId(user.getLocaleId());
        userFromDb.setEmail(user.getEmail());
        userFromDb.setTheme(user.getTheme());
        userFromDb.setFirstConnection(user.isFirstConnection());
        
        return user;
    }
    
    /**
     * Update the user password.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User updatePassword(User user) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
        
        // Get the user
        // Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        // q.setParameter("id", user.getId());
        Query q = getQueryForUserFromDb(em, "id", user.getId());
        // User userFromDb = (User) q.getSingleResult();
        User userFromDb = getSingleResultFromDb(q);

        // Update the user
        userFromDb.setPassword(this.passwordHashService.hashPassword(user.getPassword()));
        
        return user;
    }

    /**
     * Gets a user by its ID.
     * 
     * @param id User ID
     * @return User
     */
    public User getById(String id) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active user by its username.
     * 
     * @param username User's username
     * @return User
     */
    public User getActiveByUsername(String username) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
        try {
            // Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
            // q.setParameter("username", username);
            Query q = getQueryForUserFromDb(em, "username", username);
            // return (User) q.getSingleResult();
            return getSingleResultFromDb(q);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active user by its password recovery token.
     * 
     * @param passwordResetKey Password recovery token
     * @return User
     */
    public User getActiveByPasswordResetKey(String passwordResetKey) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
        try {
            // Query q = em.createQuery("select u from User u where u.passwordResetKey = :passwordResetKey and u.deleteDate is null");
            // q.setParameter("passwordResetKey", passwordResetKey);
            Query q = getQueryForUserFromDb(em, passwordResetKey, passwordResetKey);
            // return (User) q.getSingleResult();
            return getSingleResultFromDb(q);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a user.
     * 
     * @param username User's username
     */
    public void delete(String username) {
        // EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityManager em = getEntityManager();
            
        // Get the user
        // Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        // q.setParameter("username", username);
        Query q = getQueryForUserFromDb(em, "username", username);
        // User userFromDb = (User) q.getSingleResult();
        User userFromDb = getSingleResultFromDb(q);
        
        // Delete the user
        Date dateNow = new Date();
        userFromDb.setDeleteDate(dateNow);

        // Delete linked data
        // q = em.createQuery("delete from AuthenticationToken at where at.userId = :userId");
        // q.setParameter("userId", userFromDb.getId());
        // q.executeUpdate();
        deleteLinkedData(em, userFromDb);
    }

    /**
     * Hash the user's password.
     * 
     * @param password Clear password
     * @return Hashed password
     */
    // protected String hashPassword(String password) {
    //     return BCrypt.hashpw(password, BCrypt.gensalt());
    // }
    
    /**
     * Returns the list of all users.
     * 
     * @param paginatedList List of users (updated by side effects)
     * @param sortCriteria Sort criteria
     */
    public void findAll(PaginatedList<UserDto> paginatedList, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder sb = new StringBuilder("select u.USE_ID_C as c0, u.USE_USERNAME_C as c1, u.USE_EMAIL_C as c2, u.USE_CREATEDATE_D as c3, u.USE_IDLOCALE_C as c4");
        sb.append(" from T_USER u ");
        
        // Add search criterias
        List<String> criteriaList = new ArrayList<String>();
        criteriaList.add("u.USE_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        // Perform the search
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        List<Object[]> l = PaginatedQuery.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        // Assemble results
        List<UserDto> userDtoList = new ArrayList<UserDto>();
        for (Object[] o : l) {
            int i = 0;
            UserDto userDto = new UserDto();
            userDto.setId((String) o[i++]);
            userDto.setUsername((String) o[i++]);
            userDto.setEmail((String) o[i++]);
            userDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            userDto.setLocaleId((String) o[i++]);
            userDtoList.add(userDto);
        }
        paginatedList.setResultList(userDtoList);
    }
}
