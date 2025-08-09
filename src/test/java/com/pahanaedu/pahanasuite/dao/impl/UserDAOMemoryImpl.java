package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemoryImpl implements UserDAO {

    private Map<String, User> users = new HashMap<>();

    public UserDAOMemoryImpl() {
        users.put("testuser", new User("testuser", "testpass"));
    }

    @Override
    public User findByUsername(String username) {
        return users.get(username);
    }
}