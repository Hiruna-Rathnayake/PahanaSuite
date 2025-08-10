// File: src/main/java/com/pahanaedu/pahanasuite/dao/impl/UserDAOMemoryImpl.java
package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemoryImpl implements UserDAO {

    private final Map<String, User> users = new HashMap<>();

    public UserDAOMemoryImpl() {
        // Preload with a test user (id = 1, role = "customer", telephone and address empty for example)
        users.put("testuser", new User(1, "testuser", "testpass", "customer"));
    }

    @Override
    public User findByUsername(String username) {
        return users.get(username);
    }

    @Override
    public boolean createUser(User user) {
        if (user == null || user.getUsername() == null) return false;
        if (users.containsKey(user.getUsername())) return false; // User already exists
        users.put(user.getUsername(), user);
        return true;
    }
}
