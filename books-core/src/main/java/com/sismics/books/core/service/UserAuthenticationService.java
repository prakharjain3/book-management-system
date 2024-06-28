package com.sismics.books.core.service;

// import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

// import org.mindrot.jbcrypt.BCrypt;

import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.model.jpa.User;

/**
 * Service for user authentication.
 * 
 */

public class UserAuthenticationService {
    private UserDao userDao;
    private PasswordHashService passwordHashService;

    public UserAuthenticationService(UserDao userDao) {
        this.userDao = userDao;
        this.passwordHashService = new PasswordHashService();
    }

    /**
     * Authenticates an user.
     * 
     * @param username User login
     * @param password User password
     * @return ID of the authenticated user or null
     */
    public String authenticate(String username, String password, Query q) {
        try {
            User user = userDao.getActiveByUsername(username);
            if (!this.passwordHashService.verifyPassword(password, user.getPassword())) {
                return null;
            }
            return user.getId();
        } catch (NoResultException e) {
            return null;
        }
    }


}