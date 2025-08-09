// File: src/main/java/com/pahanaedu/pahanasuite/dao/impl/UserDAOImpl.java
package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAOImpl implements UserDAO {

    private static final String SELECT_USER_SQL = "SELECT id, username, password, role, telephone, address FROM users WHERE username = ?";
    private static final String INSERT_USER_SQL = "INSERT INTO users (username, password, role, telephone, address) VALUES (?, ?, ?, ?, ?)";

    @Override
    public User findByUsername(String username) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_SQL)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    user.setTelephone(rs.getString("telephone"));
                    user.setAddress(rs.getString("address"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean createUser(User user) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER_SQL)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getTelephone());
            stmt.setString(5, user.getAddress());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
