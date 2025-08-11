package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UserDAOMemoryImpl implements UserDAO {

    private final Map<Integer, User> byId = new HashMap<>();
    private final Map<String, Integer> idByUsername = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    public UserDAOMemoryImpl() {
        // seed a couple users
        createRaw(new User(0, "admin",   "admin",   "admin"));
        createRaw(new User(0, "manager", "manager", "manager"));
        createRaw(new User(0, "cashier", "cashier", "cashier"));
    }

    // ---------- DAO methods ----------

    // Sanitized read (no password) for general use
    @Override
    public User findByUsername(String username) {
        Integer id = idByUsername.get(username);
        if (id == null) return null;
        return sanitize(byId.get(id));
    }

    // Auth-only read (INCLUDES password) for login
    @Override
    public User findAuthByUsername(String username) {
        Integer id = idByUsername.get(username);
        if (id == null) return null;
        User u = cloneUser(byId.get(id));
        // keep password here
        return u;
    }

    @Override
    public User findById(int id) {
        return sanitize(byId.get(id));
    }

    @Override
    public List<User> findAll() {
        List<User> out = new ArrayList<>(byId.size());
        for (User u : byId.values()) out.add(sanitize(u));
        // keep stable order by id
        out.sort(Comparator.comparingInt(User::getId));
        return out;
    }

    // Create and return created user with id set (password scrubbed)
    @Override
    public User createUser(User user) {
        if (user == null || isBlank(user.getUsername()) || isBlank(user.getPassword()) || isBlank(user.getRole()))
            return null;
        String uname = user.getUsername().trim();
        if (idByUsername.containsKey(uname)) return null; // duplicate
        User stored = createRaw(user);
        return sanitize(stored);
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) return false;
        User existing = byId.get(user.getId());
        if (existing == null) return false;

        String newUname = user.getUsername() == null ? null : user.getUsername().trim();
        if (isBlank(newUname)) return false;

        // enforce unique username
        Integer currentIdWithUname = idByUsername.get(newUname);
        if (currentIdWithUname != null && currentIdWithUname != user.getId()) return false;

        // update username index if changed
        if (!existing.getUsername().equals(newUname)) {
            idByUsername.remove(existing.getUsername());
            idByUsername.put(newUname, user.getId());
        }

        existing.setUsername(newUname);
        existing.setRole(normRole(user.getRole()));
        return true;
    }

    @Override
    public boolean deleteUser(int id) {
        User removed = byId.remove(id);
        if (removed == null) return false;
        idByUsername.remove(removed.getUsername());
        return true;
    }

    @Override
    public boolean resetPassword(int id, String newPassword) {
        User u = byId.get(id);
        if (u == null || isBlank(newPassword)) return false;
        u.setPassword(newPassword);
        return true;
    }

    // ---------- helpers ----------

    private User createRaw(User input) {
        User stored = new User();
        stored.setId(seq.incrementAndGet());
        stored.setUsername(input.getUsername().trim());
        stored.setPassword(input.getPassword()); // plain for now
        stored.setRole(normRole(input.getRole()));
        byId.put(stored.getId(), stored);
        idByUsername.put(stored.getUsername(), stored.getId());
        return stored;
    }

    private static User sanitize(User u) {
        if (u == null) return null;
        User copy = cloneUser(u);
        copy.setPassword(null);
        return copy;
    }

    private static User cloneUser(User u) {
        if (u == null) return null;
        User c = new User();
        c.setId(u.getId());
        c.setUsername(u.getUsername());
        c.setPassword(u.getPassword());
        c.setRole(u.getRole());
        return c;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String normRole(String role) {
        if (role == null) return "cashier";
        String r = role.trim().toLowerCase();
        return switch (r) {
            case "admin", "manager", "cashier" -> r;
            default -> "cashier";
        };
    }
}
