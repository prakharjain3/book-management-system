package com.sismics.books.core.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Service for password hashing.
 * 
 * @see BCrypt
 */
public class PasswordHashService {

    /**
     * Hashes a password.
     * 
     * @param password Password
     * @return Hashed password
     */

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifies a password.
     * 
     * @param password Password
     * @param hashedPassword Hashed password
     * @return True if the password is correct
     */
    
    public boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
    
}
