package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.UserDAOImpl;
// For quick testing without DB swap the DAO:
// import com.pahanaedu.pahanasuite.dao.impl.UserDAOMemoryImpl;
import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/dashboard/*")
public class DashboardServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        // Make sure this matches what UsersServlet uses (both JDBC or both Memory)
        userService = new UserService(new UserDAOImpl());
        // userService = new UserService(new UserDAOMemoryImpl()); // <-- uncomment for memory testing
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        // Session / auth
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Logged-in user
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Common attributes for JSP
        String role = user.getRole();  // "admin", "manager", "cashier"
        req.setAttribute("userRole", role);
        req.setAttribute("username", user.getUsername());
        req.setAttribute("user", user);

        // Determine section
        String section = getSectionFromRequest(req, role);
        section = validateSectionAccess(section, role);

        // Load data needed for the section
        if ("users".equalsIgnoreCase(section)) {
            var list = userService.listAll();
            req.setAttribute("users", list);
        }

        // Layout flag for sidebar
        boolean hasSidebar = !"cashier".equalsIgnoreCase(role);
        req.setAttribute("currentSection", section);
        req.setAttribute("hasSidebar", hasSidebar);

        Object flash = session.getAttribute("flash");
        if (flash != null) {
            req.setAttribute("flash", String.valueOf(flash));
            session.removeAttribute("flash");
        }

        // Forward to the wrapper under WEB-INF
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);

    }

    private String getSectionFromRequest(HttpServletRequest req, String userRole) {
        String pathInfo = req.getPathInfo(); // e.g. "/users"
        if (pathInfo != null && pathInfo.length() > 1) {
            return pathInfo.substring(1); // drop leading "/"
        }
        String section = req.getParameter("section");
        if (section != null && !section.isBlank()) {
            return section;
        }
        // Default section per role
        return "cashier".equalsIgnoreCase(userRole) ? "sales" : "overview";
    }

    private String validateSectionAccess(String section, String userRole) {
        if ("admin".equalsIgnoreCase(userRole) || "manager".equalsIgnoreCase(userRole)) {
            return section;
        }
        if ("cashier".equalsIgnoreCase(userRole)) {
            if ("sales".equalsIgnoreCase(section) || "customers".equalsIgnoreCase(section)) {
                return section;
            }
            return "sales";
        }
        return "sales";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
