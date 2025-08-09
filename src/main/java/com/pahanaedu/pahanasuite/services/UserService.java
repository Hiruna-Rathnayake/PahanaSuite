package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Attempts to authenticate a user by username and password.
     * @param username user's username
     * @param password user's password
     * @return the authenticated User object if credentials are valid, otherwise null
     */
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Registers a new user in the system.
     * @param user the user to register
     * @return true if registration succeeded, false otherwise
     */
    public boolean register(User user) {
        // Could add more validation here (e.g., check username uniqueness)
        return userDAO.createUser(user);
    }
}
