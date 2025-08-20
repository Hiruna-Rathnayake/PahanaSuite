package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.dao.impl.BillDAOImpl;
import com.pahanaedu.pahanasuite.models.Bill;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/dashboard/reports")
public class ReportsServlet extends HttpServlet {

    private BillDAO billDAO;

    @Override
    public void init() {
        billDAO = new BillDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object role = session.getAttribute("userRole");
        Object user = session.getAttribute("username");
        if (role != null) req.setAttribute("userRole", role);
        if (user != null) req.setAttribute("username", user);

        String period = req.getParameter("period");
        if (!"monthly".equalsIgnoreCase(period)) period = "daily";

        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDateTime from;
        LocalDateTime to;

        if ("monthly".equalsIgnoreCase(period)) {
            String m = req.getParameter("month");
            if (m != null && !m.isBlank()) {
                try {
                    fromDate = LocalDate.parse(m + "-01");
                } catch (DateTimeParseException e) {
                    fromDate = today.withDayOfMonth(1);
                }
            } else {
                fromDate = today.withDayOfMonth(1);
            }
            from = fromDate.atStartOfDay();
            to = from.plusMonths(1);
            req.setAttribute("month", fromDate);
        } else {
            String d = req.getParameter("date");
            if (d != null && !d.isBlank()) {
                try {
                    fromDate = LocalDate.parse(d);
                } catch (DateTimeParseException e) {
                    fromDate = today;
                }
            } else {
                fromDate = today;
            }
            from = fromDate.atStartOfDay();
            to = from.plusDays(1);
            req.setAttribute("date", fromDate);
        }

        List<Bill> bills = billDAO.findIssuedBetween(from, to);
        int count = billDAO.countIssuedBetween(from, to);
        BigDecimal total = billDAO.sumTotalIssuedBetween(from, to);

        req.setAttribute("bills", bills);
        req.setAttribute("count", count);
        req.setAttribute("total", total);
        req.setAttribute("period", period);
        req.setAttribute("currentSection", "reports");
        req.setAttribute("hasSidebar", Boolean.TRUE);

        Object flash = session.getAttribute("flash");
        if (flash != null) {
            req.setAttribute("flash", String.valueOf(flash));
            session.removeAttribute("flash");
        }

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}

