package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class UsersServletTest {

    private UsersServlet servlet;
    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        servlet = new UsersServlet();
        userService = mock(UserService.class);
        Field f = UsersServlet.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(servlet, userService);
    }

    @Test
    void testRedirectsWhenNotLoggedIn() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getSession(false)).thenReturn(null);
        when(req.getContextPath()).thenReturn("");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/login");
    }

    @Test
    void testRedirectsWhenUnauthorizedRole() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("userRole")).thenReturn("cashier");
        when(req.getContextPath()).thenReturn("");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/dashboard/users?err=forbidden");
    }

    @Test
    void testHandleCreateSuccess() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("userRole")).thenReturn("admin");
        when(req.getContextPath()).thenReturn("");

        when(req.getParameter("action")).thenReturn("create");
        when(req.getParameter("username")).thenReturn("bob");
        when(req.getParameter("password")).thenReturn("pw");
        when(req.getParameter("role")).thenReturn("manager");

        when(userService.create("bob", "pw", "manager")).thenReturn(new User(1, "bob", null, "manager"));

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), contains("User created"));
        verify(resp).sendRedirect("/dashboard/users");
    }
}
