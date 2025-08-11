package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class LoginServletTest {

    private LoginServlet servlet;
    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new LoginServlet();
        userService = mock(UserService.class);
        Field f = LoginServlet.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(servlet, userService);
    }

    @Test
    void testDoGetLoggedInRedirects() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(req.getContextPath()).thenReturn("");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/dashboard");
    }

    @Test
    void testDoGetNotLoggedInForwards() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login.jsp")).thenReturn(rd);

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
    }

    @Test
    void testDoPostSuccessfulLogin() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        when(req.getParameter("username")).thenReturn("alice");
        when(req.getParameter("password")).thenReturn("pw");
        when(userService.login("alice", "pw")).thenReturn(new User(1, "alice", null, "admin"));
        when(req.getSession(false)).thenReturn(oldSession);
        when(req.getSession(true)).thenReturn(newSession);
        when(req.getContextPath()).thenReturn("");

        servlet.doPost(req, resp);

        verify(oldSession).invalidate();
        verify(newSession).setAttribute("isLoggedIn", true);
        verify(resp).sendRedirect("/dashboard");
    }

    @Test
    void testDoPostInvalidLogin() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getParameter("username")).thenReturn("alice");
        when(req.getParameter("password")).thenReturn("bad");
        when(userService.login("alice", "bad")).thenReturn(null);
        when(req.getRequestDispatcher("/login.jsp")).thenReturn(rd);

        servlet.doPost(req, resp);

        verify(req).setAttribute(eq("error"), anyString());
        verify(rd).forward(req, resp);
    }
}
