package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.dao.impl.UserDAOImpl;
import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        UserDAO userDAO = new UserDAOImpl(); // DB-backed DAO (swap to memory impl for testing if needed)
        userService = new UserService(userDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = userService.login(username, password); // returns sanitized user (no password) on success

        if (user != null) {
            // Kill any existing session to prevent fixation, then create a fresh one
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute("isLoggedIn", true);
            session.setAttribute("user", user);
            session.setAttribute("userRole", user.getRole());  // <-- critical for UsersServlet
            session.setAttribute("lastActivity", System.currentTimeMillis());

            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            req.setAttribute("error", "Invalid username or password");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
