package com.sismics.books.rest.service;

import java.util.Date;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;

import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.constant.UserConstants;
import com.sismics.books.core.model.jpa.AuthenticationToken;
import com.sismics.books.core.model.jpa.User;
import com.sismics.books.rest.constant.BaseFunction;
import com.sismics.books.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.UserPrincipal;



public class UserService {

    private UserDao userDao;

    public UserService() {
        userDao = new UserDao();
    }
    
    public void register(String username, String password, String email) throws JSONException {
        // Validate the input data
        username = ValidationUtil.validateLength(username, UserConstants.USERNAME, 3, 50);
        ValidationUtil.validateAlphanumeric(username, UserConstants.USERNAME);
        password = ValidationUtil.validateLength(password, UserConstants.PASSWORD, 8, 50);
        email = ValidationUtil.validateLength(email, UserConstants.EMAIL, 3, 50);
        ValidationUtil.validateEmail(email, UserConstants.EMAIL);

        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setCreateDate(new Date());
        user.setLocaleId(Constants.DEFAULT_LOCALE_ID);
        
        // Create the user
        this.userDao = new UserDao();
        try {
            this.userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }
    }

    public void update(String username, String password, String email, String localeId, String themeId)  throws JSONException {

        // Validate the input data
        password = ValidationUtil.validateLength(password, UserConstants.PASSWORD, 8, 50, true);
        email = ValidationUtil.validateLength(email, UserConstants.EMAIL, null, 100, true);
        localeId = ValidationUtil.validateLocale(localeId, UserConstants.LOCALE, true);
        themeId = ValidationUtil.validateTheme(themeId, UserConstants.THEME, true);
        
        // Check if the user exists
        // UserDao userDao = new UserDao();
        User user = this.userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException(UserConstants.USERNOTFOUND, UserConstants.USER_DOESNT_EXIST);
        }

        // Update the user
        if (email != null) {
            user.setEmail(email);
        }
        if (themeId != null) {
            user.setTheme(themeId);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        
        user = this.userDao.update(user);
        
        if (StringUtils.isNotBlank(password)) {
            // Change the password
            user.setPassword(password);
            this.userDao.updatePassword(user);
        }
        
    }

    public String login (String username, String password, boolean longLasted) throws JSONException {
        // Validate the input data
        username = StringUtils.strip(username);
        password = StringUtils.strip(password);

        // Get the user
        // UserDao userDao = new UserDao();
        String userId = this.userDao.authenticate(username, password);
        if (userId == null) {
            throw new ForbiddenClientException();
        }
            
        // Create a new session token
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setUserId(userId);
        authenticationToken.setLongLasted(longLasted);
        String token = authenticationTokenDao.create(authenticationToken);
        
        // Cleanup old session tokens
        authenticationTokenDao.deleteOldSessionToken(userId);
        return token;
    }
}
