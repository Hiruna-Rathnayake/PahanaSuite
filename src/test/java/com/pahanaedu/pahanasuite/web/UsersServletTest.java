package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UsersServletTest {

    private UsersServlet servlet;
    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        // Instantiate servlet and inject a mocked UserService via reflection
        servlet = new UsersServlet();
        userService = mock(UserService.class);
        Field f = UsersServlet.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(servlet, userService);
    }

    @Test
    void testRedirectsWhenNotLoggedIn() throws Exception {
        // Arrange: no session returned -> treated as not logged in
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getSession(false)).thenReturn(null);
        when(req.getContextPath()).thenReturn("");

        // Act
        servlet.doPost(req, resp);

        // Assert
        verify(resp).sendRedirect("/login");
        verifyNoInteractions(userService);
    }

    @Test
    void testRedirectsWhenUnauthorizedRole() throws Exception {
        // Arrange: logged in but role is not admin/manager
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("userRole")).thenReturn("cashier");
        when(req.getContextPath()).thenReturn("");

        // Act
        servlet.doPost(req, resp);

        // Assert
        verify(resp).sendRedirect("/dashboard/users?err=forbidden");
        verifyNoInteractions(userService);
    }

    @Test
    void testHandleCreateSuccess() throws Exception {
        // Arrange: logged in as admin and action=create
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        // The servlet checks getSession(false) for auth...
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("userRole")).thenReturn("admin");
        when(req.getContextPath()).thenReturn("");

        // ...but handleCreate uses getSession() to set flash
        when(req.getSession()).thenReturn(session);

        when(req.getParameter("action")).thenReturn("create");
        when(req.getParameter("username")).thenReturn("bob");
        when(req.getParameter("password")).thenReturn("pw");
        when(req.getParameter("role")).thenReturn("manager");

        when(userService.create("bob", "pw", "manager"))
                .thenReturn(new User(1, "bob", null, "manager"));

        // Act
        servlet.doPost(req, resp);

        // Assert: flash set and redirected to users page
        verify(session).setAttribute(eq("flash"), contains("User created"));
        verify(resp).sendRedirect("/dashboard/users");
        verify(userService).create("bob", "pw", "manager");
    }
}
