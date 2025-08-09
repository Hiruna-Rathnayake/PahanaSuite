// AuthService.java
package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

public class AuthService {

    private UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }
}
