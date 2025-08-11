package com.pahanaedu.pahanasuite.web;

import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class LogoutServletTest {

    private final LogoutServlet servlet = new LogoutServlet();

    @Test
    void testDoGetInvalidatesSession() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(req.getContextPath()).thenReturn("");

        servlet.doGet(req, resp);

        verify(session).invalidate();
        verify(resp).sendRedirect("/login");
    }
}
