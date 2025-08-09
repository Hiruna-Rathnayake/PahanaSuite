// File: src/main/java/com/pahanaedu/pahanasuite/dao/UserDAO.java
package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.User;

public interface UserDAO {
    User findByUsername(String username);
    boolean createUser(User user);
    // Add other user-related methods here
}
