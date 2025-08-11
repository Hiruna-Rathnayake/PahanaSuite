package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private static final String SELECT_BY_USERNAME =
            "SELECT id, username, password, role FROM users WHERE username = ?";
    private static final String SELECT_BY_ID =
            "SELECT id, username, password, role FROM users WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, username, password, role FROM users ORDER BY id";
    private static final String INSERT_USER =
            "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE users SET username = ?, role = ? WHERE id = ?";
    private static final String DELETE_USER =
            "DELETE FROM users WHERE id = ?";
    private static final String UPDATE_PASSWORD =
            "UPDATE users SET password = ? WHERE id = ?";

    @Override
    public User findByUsername(String username) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User findById(int id) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public User createUser(User user) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword()); // TODO: hash in real world
            ps.setString(3, user.getRole());
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
                // never return password to callers
                user.setPassword(null);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateUser(User user) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getRole());
            ps.setInt(3, user.getId());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteUser(int id) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_USER)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean resetPassword(int id, String newPassword) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_PASSWORD)) {

            ps.setString(1, newPassword); // TODO: hash
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User findAuthByUsername(String username) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapWithPassword(rs); // note: includes password
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // sanitized mapper (no password)
    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(null); // scrub
        u.setRole(rs.getString("role"));
        return u;
    }

    // auth-only mapper (keeps password for login check)
    private static User mapWithPassword(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password")); // used only inside service.login
        u.setRole(rs.getString("role"));
        return u;
    }


}
