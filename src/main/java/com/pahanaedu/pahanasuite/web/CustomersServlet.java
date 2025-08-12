// File: src/main/java/com/pahanaedu/pahanasuite/web/CustomersServlet.java
package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.CustomerDAOImpl;
import com.pahanaedu.pahanasuite.services.CustomerService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(urlPatterns = "/dashboard/customers/actions")
public class CustomersServlet extends HttpServlet {

    private CustomerService customerService;

    @Override
    public void init() {
        // Initialize service with JDBC-backed DAO
        customerService = new CustomerService(new CustomerDAOImpl());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Guard: must be logged in
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Authorization: only admin/manager can write
        String role = String.valueOf(session.getAttribute("userRole"));
        boolean canWrite = "admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role);
        if (!canWrite) {
            resp.sendRedirect(req.getContextPath() + "/dashboard/customers?err=forbidden");
            return;
        }

        String action = trim(req.getParameter("action"));

        try {
            switch (action == null ? "" : action) {
                case "create" -> handleCreate(req);
                case "update" -> handleUpdate(req);
                case "delete" -> handleDelete(req);
                default -> { /* unknown action, ignore */ }
            }
        } catch (Exception e) {
            // Set a generic flash message to avoid leaking internal errors
            session.setAttribute("flash", "Operation failed. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/dashboard/customers?err=failed");
            return;
        }

        // Redirect back to listing page after action
        resp.sendRedirect(req.getContextPath() + "/dashboard/customers");
    }

    // Creates a customer after basic validation handled by service
    private void handleCreate(HttpServletRequest req) {
        String accountNumber = trim(req.getParameter("accountNumber"));
        String name          = trim(req.getParameter("name"));
        String address       = trim(req.getParameter("address"));
        String telephone     = trim(req.getParameter("telephone"));
        int unitsConsumed    = parseInt(req.getParameter("unitsConsumed"));

        var created = customerService.create(accountNumber, name, address, telephone, unitsConsumed);
        if (created != null) {
            req.getSession().setAttribute("flash", "Customer created: " + created.getAccountNumber());
        } else {
            req.getSession().setAttribute("flash", "Could not create customer (duplicate or invalid).");
        }
    }

    // Updates an existing customer (by id) with new details
    private void handleUpdate(HttpServletRequest req) {
        int id               = parseInt(req.getParameter("id"));
        String accountNumber = trim(req.getParameter("accountNumber"));
        String name          = trim(req.getParameter("name"));
        String address       = trim(req.getParameter("address"));
        String telephone     = trim(req.getParameter("telephone"));
        int unitsConsumed    = parseInt(req.getParameter("unitsConsumed"));

        if (id <= 0) {
            req.getSession().setAttribute("flash", "Invalid customer id.");
            return;
        }

        var updated = customerService.update(id, accountNumber, name, address, telephone, unitsConsumed);
        req.getSession().setAttribute("flash", updated != null ? "Customer updated." : "Update failed.");
    }

    // Deletes a customer by id
    private void handleDelete(HttpServletRequest req) {
        int id = parseInt(req.getParameter("id"));
        if (id <= 0) {
            req.getSession().setAttribute("flash", "Invalid customer id.");
            return;
        }
        boolean ok = customerService.delete(id);
        req.getSession().setAttribute("flash", ok ? "Customer deleted." : "Delete failed.");
    }

    // --- helpers ---

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
