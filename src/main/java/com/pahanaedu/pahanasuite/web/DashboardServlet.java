// src/main/java/com/pahanaedu/pahanasuite/web/DashboardServlet.java
package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.dao.impl.BillDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.CustomerDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.ItemDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.UserDAOImpl;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.CustomerService;
import com.pahanaedu.pahanasuite.services.ItemService;
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
    private CustomerService customerService;
    private ItemService itemService;

    // NEW: bills listing uses DAO directly (keeps your BillingService unchanged)
    private BillDAO billDAO;

    @Override
    public void init() {
        // Match your existing style
        userService = new UserService(new UserDAOImpl());
        customerService = new CustomerService(new CustomerDAOImpl());
        itemService = new ItemService(new ItemDAOImpl());

        billDAO = new BillDAOImpl(); // JDBC impl; uses DBConnectionFactory internally
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
            req.setAttribute("users", userService.listAll());
        } else if ("customers".equalsIgnoreCase(section)) {
            req.setAttribute("customers", customerService.listAll());
        } else if ("items".equalsIgnoreCase(section)) {
            String q = req.getParameter("q");
            String cat = req.getParameter("category");
            req.setAttribute("items", itemService.search(q, cat, 100, 0));
        } else if ("bills".equalsIgnoreCase(section)) {
            // NEW: list bill headers so they’re visible on the dashboard
            req.setAttribute("bills", billDAO.findAll());
        } else if ("sales".equalsIgnoreCase(section)) {
            // Optional: show current working bill started by BillingServlet
            Bill working = (Bill) session.getAttribute("bill");
            req.setAttribute("bill", working);
            // You can also surface recent bills alongside the sales screen
            req.setAttribute("recentBills", billDAO.findAll());
        }

        // Sidebar layout flag
        boolean hasSidebar = !"cashier".equalsIgnoreCase(role);
        req.setAttribute("currentSection", section);
        req.setAttribute("hasSidebar", hasSidebar);

        // Flash message (from POST-redirect)
        Object flash = session.getAttribute("flash");
        if (flash != null) {
            req.setAttribute("flash", String.valueOf(flash));
            session.removeAttribute("flash");
        }

        // Forward to the wrapper at webapp root
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
            // Allow cashiers to see bills + customers + sales
            if ("sales".equalsIgnoreCase(section) ||
                    "customers".equalsIgnoreCase(section) ||
                    "bills".equalsIgnoreCase(section)) {
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
