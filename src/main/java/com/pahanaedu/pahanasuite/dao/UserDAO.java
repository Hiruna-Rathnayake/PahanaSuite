package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.User;
import java.util.List;

public interface UserDAO {
    User findByUsername(String username);      // sanitized
    User findAuthByUsername(String username);  // includes password
    User findById(int id);
    List<User> findAll();

    User createUser(User user);                // returns created row
    boolean updateUser(User user);
    boolean deleteUser(int id);
    boolean resetPassword(int id, String newPassword);
}

