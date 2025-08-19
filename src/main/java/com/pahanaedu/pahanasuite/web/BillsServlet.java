package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.dao.impl.BillDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.PaymentDAOImpl;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillStatus;
import com.pahanaedu.pahanasuite.services.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/dashboard/bills")
public class BillsServlet extends HttpServlet {

    private BillDAO billDAO;
    private PaymentService paymentService;

    @Override
    public void init() {
        billDAO = new BillDAOImpl();
        paymentService = new PaymentService(new PaymentDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Load all bill headers for listing
        List<Bill> bills = billDAO.findAll();
        Map<Integer, BigDecimal> outstanding = new HashMap<>();
        for (Bill b : bills) {
            outstanding.put(b.getId(), paymentService.remainingBalance(b.getId(), b.getTotal()));
        }
        req.setAttribute("bills", bills);
        req.setAttribute("outstanding", outstanding);

        // If ?id= provided, load bill details
        int id = parseInt(req.getParameter("id"));
        if (id > 0) {
            Bill b = billDAO.findById(id);
            if (b != null) req.setAttribute("selectedBill", b);
        }

        // dashboard wrapper expectations
        req.setAttribute("currentSection", "bills");
        req.setAttribute("hasSidebar", Boolean.TRUE);
        Object role = session.getAttribute("userRole");
        Object user = session.getAttribute("username");
        if (role != null) req.setAttribute("userRole", role);
        if (user != null) req.setAttribute("username", user);

        Object flash = session.getAttribute("flash");
        if (flash != null) {
            req.setAttribute("flash", String.valueOf(flash));
            session.removeAttribute("flash");
        }

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
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
            resp.sendRedirect(req.getContextPath() + "/dashboard/bills?err=forbidden");
            return;
        }

        String action = trim(req.getParameter("action"));
        int id = parseInt(req.getParameter("id"));

        switch (action == null ? "" : action) {
            case "delete" -> {
                if (id > 0 && billDAO.deleteBill(id)) {
                    session.setAttribute("flash", "Bill deleted.");
                } else {
                    session.setAttribute("flash", "Delete failed.");
                }
            }
            case "markPaid" -> {
                if (id > 0) {
                    Bill bill = billDAO.findById(id);
                    if (bill != null) {
                        bill.setStatus(BillStatus.PAID);
                        boolean ok = billDAO.updateBill(bill);
                        session.setAttribute("flash", ok ? "Bill marked as paid." : "Operation failed.");
                    } else {
                        session.setAttribute("flash", "Bill not found.");
                    }
                }
            }
            default -> {
                // ignore unknown
            }
        }

        resp.sendRedirect(req.getContextPath() + "/dashboard/bills");
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return -1; } }
}
