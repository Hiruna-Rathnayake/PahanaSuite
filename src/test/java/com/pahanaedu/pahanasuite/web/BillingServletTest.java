package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.BillDAOImpl;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.services.CustomerService;
import com.pahanaedu.pahanasuite.services.ItemService;
import com.pahanaedu.pahanasuite.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BillingServletTest {
    @Test
    void save_adjustsStockAndUnits() throws Exception {
        BillingServlet servlet = new BillingServlet();

        BillDAOImpl billDAO = mock(BillDAOImpl.class);
        CustomerService customerService = mock(CustomerService.class);
        ItemService itemService = mock(ItemService.class);
        PaymentService paymentService = mock(PaymentService.class);

        java.lang.reflect.Field f;
        f = BillingServlet.class.getDeclaredField("billDAO");
        f.setAccessible(true);
        f.set(servlet, billDAO);
        f = BillingServlet.class.getDeclaredField("customerService");
        f.setAccessible(true);
        f.set(servlet, customerService);
        f = BillingServlet.class.getDeclaredField("itemService");
        f.setAccessible(true);
        f.set(servlet, itemService);
        f = BillingServlet.class.getDeclaredField("paymentService");
        f.setAccessible(true);
        f.set(servlet, paymentService);

        Bill bill = new Bill();
        bill.setCustomerId(5);
        BillLine line = new BillLine();
        line.setItemId(10);
        line.setQuantity(2);
        line.setUnitPrice(new BigDecimal("1.00"));
        bill.addLine(line);
        bill.setId(1);
        when(billDAO.createBill(any(Bill.class))).thenReturn(bill);
        when(paymentService.remainingBalance(anyInt(), any())).thenReturn(BigDecimal.ONE);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("bill")).thenReturn(bill);
        when(req.getParameter("action")).thenReturn("save");
        when(req.getParameter("paymentAmount")).thenReturn("0");
        when(req.getContextPath()).thenReturn("");

        servlet.doPost(req, resp);

        verify(itemService).adjustStock(10, -2);
        verify(customerService).addUnitsConsumed(5, 2);
    }
}
