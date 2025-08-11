// File: src/main/java/com/pahanaedu/pahanasuite/web/UsersServlet.java
package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.UserDAOImpl;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.security.SecureRandom;

@WebServlet(urlPatterns = "/dashboard/users/actions")
public class UsersServlet extends HttpServlet {

    private UserService userService;
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public void init() {
        userService = new UserService(new UserDAOImpl());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = String.valueOf(session.getAttribute("userRole"));
        boolean canWrite = "admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role);
        if (!canWrite) {
            resp.sendRedirect(req.getContextPath() + "/dashboard/users?err=forbidden");
            return;
        }

        String action = trim(req.getParameter("action"));
        try {
            switch (action == null ? "" : action) {
                case "create" -> handleCreate(req);
                case "update" -> handleUpdate(req);
                case "delete" -> handleDelete(req);
                case "resetPassword" -> handleResetPassword(req);
                default -> { /* ignore unknown */ }
            }
        } catch (Exception e) {
            session.setAttribute("flash", "Operation failed. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/dashboard/users?err=failed");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/dashboard/users");
    }

    private void handleCreate(HttpServletRequest req) {
        String username = trim(req.getParameter("username"));
        String password = trim(req.getParameter("password"));
        String role = trim(req.getParameter("role"));
        if (!isBlank(username) && !isBlank(password) && !isBlank(role)) {
            var created = userService.create(username, password, role);
            if (created != null) req.getSession().setAttribute("flash", "User created: " + created.getUsername());
            else req.getSession().setAttribute("flash", "Could not create user (duplicate or invalid).");
        }
    }

    private void handleUpdate(HttpServletRequest req) {
        int id = parseId(req.getParameter("id"));
        String username = trim(req.getParameter("username"));
        String role = trim(req.getParameter("role"));
        if (id > 0 && !isBlank(username) && !isBlank(role)) {
            var updated = userService.update(id, username, role);
            req.getSession().setAttribute("flash", updated != null ? "User updated." : "Update failed.");
        }
    }

    private void handleDelete(HttpServletRequest req) {
        int id = parseId(req.getParameter("id"));
        if (id > 0) {
            boolean ok = userService.delete(id);
            req.getSession().setAttribute("flash", ok ? "User deleted." : "Delete failed.");
        }
    }

    private void handleResetPassword(HttpServletRequest req) {
        int id = parseId(req.getParameter("id"));
        String newPw = trim(req.getParameter("password")); // optional from edit panel
        if (id <= 0) return;

        if (isBlank(newPw)) {
            newPw = generateTempPassword(); // <-- make it do something by default
        }

        boolean ok = userService.resetPassword(id, newPw);
        if (ok) {
            req.getSession().setAttribute("flash", "Password reset. Temporary password: " + newPw);
        } else {
            req.getSession().setAttribute("flash", "Password reset failed.");
        }
    }

    // simple temp password: 8 chars [A-Za-z0-9]
    private static String generateTempPassword() {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(alphabet.charAt(RNG.nextInt(alphabet.length())));
        return sb.toString();
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static int parseId(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return -1; } }
}
