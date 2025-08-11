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

        // Check login/session
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Get user
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Common attributes for JSP
        String role = user.getRole();                   // "admin", "manager", "cashier"
        req.setAttribute("userRole", role);
        req.setAttribute("username", user.getUsername());
        req.setAttribute("user", user);

        // Determine target section
        String section = getSectionFromRequest(req, role);
        section = validateSectionAccess(section, role);

        // Role-aware layout flag (used by JSP + CSS)
        boolean hasSidebar = !"cashier".equalsIgnoreCase(role);

        req.setAttribute("currentSection", section);
        req.setAttribute("hasSidebar", hasSidebar);

        // Forward to wrapper (your choice to keep it here)
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }

    private String getSectionFromRequest(HttpServletRequest req, String userRole) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            return pathInfo.substring(1); // drop leading "/"
        }
        String section = req.getParameter("section");
        if (section != null && !section.isBlank()) {
            return section;
        }
        return "cashier".equalsIgnoreCase(userRole) ? "sales" : "overview";
    }

    private String validateSectionAccess(String section, String userRole) {
        if ("admin".equalsIgnoreCase(userRole) || "manager".equalsIgnoreCase(userRole)) {
            return section;
        }

        if ("cashier".equalsIgnoreCase(userRole)) {
            // Keep both if you want: sales + customers
            // If you want SALES ONLY, change to: if ("sales".equals(section)) return "sales";
            if ("sales".equals(section) || "customers".equals(section)) {
                return section;
            }
            return "sales";
        }

        // Unknown role → safest default
        return "sales";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
