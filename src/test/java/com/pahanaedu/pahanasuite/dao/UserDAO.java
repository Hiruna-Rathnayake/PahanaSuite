package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.User;

public interface UserDAO {
    User findByUsername(String username);
}
