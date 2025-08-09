package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Checks if the username and password match a user in the system.
     * @param username input username
     * @param password input password
     * @return true if authenticated, false otherwise
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        User user = userDAO.findByUsername(username);
        if (user == null) return false;
        return password.equals(user.getPassword());
    }
}
