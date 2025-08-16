package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.BillDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.CustomerDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.PaymentDAOImpl;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.Customer;
import com.pahanaedu.pahanasuite.services.CustomerService;
import com.pahanaedu.pahanasuite.services.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/billing/receipt")
public class ReceiptServlet extends HttpServlet {

    private final BillDAOImpl billDAO = new BillDAOImpl();
    private CustomerService customerService;
    private PaymentService paymentService;

    @Override
    public void init() {
        customerService = new CustomerService(new CustomerDAOImpl());
        paymentService  = new PaymentService(new PaymentDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // same login guard as the rest of dashboard
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int id = parseInt(req.getParameter("id"), 0);
        if (id <= 0) {
            session.setAttribute("flash", "Missing receipt id.");
            resp.sendRedirect(req.getContextPath() + "/dashboard/sales");
            return;
        }

        Bill bill = billDAO.findById(id);
        if (bill == null) {
            session.setAttribute("flash", "Bill not found.");
            resp.sendRedirect(req.getContextPath() + "/dashboard/sales");
            return;
        }

        // load customer (min dependency: use listAll and pick by id)
        Customer customer = null;
        for (Customer c : customerService.listAll()) {
            if (c.getId() == bill.getCustomerId()) { customer = c; break; }
        }

        BigDecimal paid = paymentService.totalPaid(bill.getId());
        BigDecimal balance = safe(bill.getTotal()).subtract(safe(paid));

        req.setAttribute("bill", bill);
        req.setAttribute("customer", customer);
        req.setAttribute("paid", paid);
        req.setAttribute("balance", balance);

        // Forward to a clean, print-ready JSP (no dashboard chrome)
        req.getRequestDispatcher("/WEB-INF/views/billing/receipt.jsp").forward(req, resp);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignore) { return def; }
    }
    private static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO.setScale(2) : v.setScale(2);
    }
}
