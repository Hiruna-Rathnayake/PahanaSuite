package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserService {
    private final UserDAO userDAO;

    // Allowed roles to prevent junk input or escalation typos
    private static final Set<String> ALLOWED_ROLES = new HashSet<>(Set.of("admin", "manager", "cashier"));

    public UserService(UserDAO userDAO) { this.userDAO = userDAO; }

    /**
     * Authenticates a user by username and password.
     * Uses auth-only DAO method that returns the stored password.
     * Returns a sanitized user (no password) on success, null otherwise.
     */
    public User login(String username, String password) {
        username = trim(username);
        password = trim(password);
        if (isBlank(username) || isBlank(password)) return null;

        // Must exist in DAO: findAuthByUsername returns row with password populated
        User authRow = userDAO.findAuthByUsername(username);
        if (authRow == null || authRow.getPassword() == null) return null;

        // Plain compare for now. Replace with hashing + constant-time compare later.
        if (!password.equals(authRow.getPassword())) return null;

        User safe = new User();
        safe.setId(authRow.getId());
        safe.setUsername(authRow.getUsername());
        safe.setRole(authRow.getRole());
        safe.setPassword(null);
        return safe;
    }

    /**
     * Creates a new user. Returns the created user (password stripped) or null on failure.
     */
    public User create(String username, String rawPassword, String role) {
        username = trim(username);
        rawPassword = trim(rawPassword);
        role = normalizeRole(role);
        if (isBlank(username) || isBlank(rawPassword) || role == null) return null;

        User u = new User();
        u.setUsername(username);
        u.setPassword(rawPassword); // TODO: hash later
        u.setRole(role);

        // DAO returns created user with id set and password cleared (per your impl)
        User created = userDAO.createUser(u);
        if (created == null) return null;

        created.setPassword(null);
        return created;
    }

    /**
     * Lists all users with passwords scrubbed.
     */
    public List<User> listAll() {
        List<User> list = userDAO.findAll();
        if (list == null) return new ArrayList<>();
        for (User u : list) {
            if (u != null) u.setPassword(null);
        }
        return list;
    }

    /**
     * Gets a single user with password scrubbed.
     */
    public User getById(int id) {
        if (id <= 0) return null;
        User u = userDAO.findById(id);
        if (u != null) u.setPassword(null);
        return u;
    }

    /**
     * Updates username and role. Returns the updated user (sanitized) or null.
     */
    public User update(int id, String username, String role) {
        if (id <= 0) return null;
        username = trim(username);
        role = normalizeRole(role);
        if (isBlank(username) || role == null) return null;

        User existing = userDAO.findById(id);
        if (existing == null) return null;

        existing.setUsername(username);
        existing.setRole(role);

        boolean ok = userDAO.updateUser(existing);
        if (!ok) return null;

        existing.setPassword(null);
        return existing;
    }

    /**
     * Deletes a user by id.
     */
    public boolean delete(int id) {
        if (id <= 0) return false;
        return userDAO.deleteUser(id);
    }

    /**
     * Resets a user's password to the provided value.
     */
    public boolean resetPassword(int id, String newPassword) {
        if (id <= 0) return false;
        newPassword = trim(newPassword);
        if (isBlank(newPassword)) return false;
        return userDAO.resetPassword(id, newPassword); // TODO: hash later
    }

    // --- helpers ---

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String normalizeRole(String role) {
        if (role == null) return null;
        String r = role.trim().toLowerCase();
        return ALLOWED_ROLES.contains(r) ? r : null;
    }
}
