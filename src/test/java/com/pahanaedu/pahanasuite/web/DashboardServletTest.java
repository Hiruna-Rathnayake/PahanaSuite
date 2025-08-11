package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class DashboardServletTest {

    private DashboardServlet servlet;
    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        servlet = new DashboardServlet();
        userService = mock(UserService.class);
        Field f = DashboardServlet.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(servlet, userService);
    }

    @Test
    void testRedirectsWhenNotLoggedIn() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getSession(false)).thenReturn(null);
        when(req.getContextPath()).thenReturn("");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/login");
    }

    @Test
    void testForwardsWhenLoggedIn() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        User user = new User(1, "alice", null, "admin");

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(user);
        when(req.getRequestDispatcher("/dashboard.jsp")).thenReturn(rd);
        when(req.getContextPath()).thenReturn("");

        servlet.doGet(req, resp);

        verify(req).setAttribute("userRole", "admin");
        verify(req).setAttribute("username", "alice");
        verify(req).setAttribute("user", user);
        verify(req).setAttribute("currentSection", "overview");
        verify(req).setAttribute("hasSidebar", true);
        verify(rd).forward(req, resp);
    }
}
