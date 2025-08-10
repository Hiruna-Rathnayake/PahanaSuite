package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/dashboard/*")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Check if user is logged in
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Get user from session
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Set user attributes for JSP
        req.setAttribute("userRole", user.getRole());
        req.setAttribute("username", user.getUsername());
        req.setAttribute("user", user);

        // Get section from URL path or parameter
        String section = getSectionFromRequest(req, user.getRole());

        // Validate section access based on role
        section = validateSectionAccess(section, user.getRole());

        req.setAttribute("currentSection", section);

        // Route to appropriate JSP
        String jspPath = "/WEB-INF/views/dashboard/" + section + ".jsp";

        try {
            req.getRequestDispatcher(jspPath).forward(req, resp);
        } catch (ServletException e) {
            // Fallback to overview if section JSP doesn't exist
            req.getRequestDispatcher("/WEB-INF/views/dashboard/overview.jsp").forward(req, resp);
        }
    }

    private String getSectionFromRequest(HttpServletRequest req, String userRole) {
        // First try to get from URL path
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            return pathInfo.substring(1); // Remove leading slash
        }

        // Then try from parameter (for backward compatibility)
        String section = req.getParameter("section");
        if (section != null) {
            return section;
        }

        // Default based on role
        return "cashier".equals(userRole) ? "sales" : "overview";
    }

    private String validateSectionAccess(String section, String userRole) {
        if ("admin".equals(userRole)) {
            // Admin can access any section
            return section;
        }

        if ("cashier".equals(userRole)) {
            // Cashiers can only access sales and customers
            if ("sales".equals(section) || "customers".equals(section)) {
                return section;
            }
            return "sales"; // Default for cashiers
        }

        // Unknown role, default to sales
        return "sales";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}