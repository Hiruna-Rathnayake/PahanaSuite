package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/dashboard")
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

        // TODO: In the future, you can add dashboard data here
        // For example:
        // req.setAttribute("todaysSales", getSalesForToday());
        // req.setAttribute("lowStockItems", getLowStockItems());
        // req.setAttribute("recentSales", getRecentSales());
        // req.setAttribute("topBooks", getTopSellingBooks());

        // Forward to dashboard JSP
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect POST requests to GET
        doGet(req, resp);
    }

    // TODO: Add these methods when you have your DAO layer ready
    /*
    private double getSalesForToday() {
        // Implementation to get today's sales from database
        return 0.0;
    }

    private int getLowStockItems() {
        // Implementation to count low stock items
        return 0;
    }

    private List<Sale> getRecentSales() {
        // Implementation to get recent sales
        return new ArrayList<>();
    }

    private List<Book> getTopSellingBooks() {
        // Implementation to get top selling books
        return new ArrayList<>();
    }
    */
}