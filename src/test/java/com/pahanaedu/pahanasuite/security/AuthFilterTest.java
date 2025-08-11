package com.pahanaedu.pahanasuite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.*;

public class AuthFilterTest {

    private AuthFilter filter = new AuthFilter();

    @Test
    void testAllowsPublicUrl() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getRequestURI()).thenReturn("/login");
        when(req.getContextPath()).thenReturn("");

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    void testRedirectsUnauthenticated() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getRequestURI()).thenReturn("/dashboard");
        when(req.getContextPath()).thenReturn("");
        when(req.getSession(false)).thenReturn(null);

        filter.doFilter(req, resp, chain);

        verify(resp).sendRedirect("/login");
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void testAllowsAuthenticatedAndUpdatesActivity() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getRequestURI()).thenReturn("/dashboard");
        when(req.getContextPath()).thenReturn("");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("lastActivity")).thenReturn(System.currentTimeMillis());

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
        verify(session).setAttribute(eq("lastActivity"), ArgumentMatchers.anyLong());
    }

    @Test
    void testInvalidatesExpiredSession() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getRequestURI()).thenReturn("/dashboard");
        when(req.getContextPath()).thenReturn("");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        long past = System.currentTimeMillis() - (9 * 60 * 60 * 1000L); // 9 hours ago
        when(session.getAttribute("lastActivity")).thenReturn(past);

        filter.doFilter(req, resp, chain);

        verify(session).invalidate();
        verify(resp).sendRedirect("/login");
        verify(chain, never()).doFilter(any(), any());
    }
}
