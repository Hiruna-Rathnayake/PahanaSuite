package com.pahanaedu.pahanasuite.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class BillsServletTest {
    @Test
    void refund_invalidId_setsFlashMessage() throws Exception {
        BillsServlet servlet = new BillsServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("userRole")).thenReturn("admin");
        when(req.getParameter("action")).thenReturn("refund");
        when(req.getParameter("id")).thenReturn("0");
        when(req.getParameter("refundAmount")).thenReturn("1.00");
        when(req.getContextPath()).thenReturn("");

        servlet.doPost(req, resp);

        verify(session).setAttribute("flash", "Refund failed.");
    }
}
