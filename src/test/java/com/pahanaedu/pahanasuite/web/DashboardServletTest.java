package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.services.CustomerService;
import com.pahanaedu.pahanasuite.services.ItemService;
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
    private CustomerService customerService;
    private ItemService itemService;
    private BillDAO billDAO;

    @BeforeEach
    void setup() throws Exception {
        servlet = new DashboardServlet();
        userService = mock(UserService.class);
        customerService = mock(CustomerService.class);
        itemService = mock(ItemService.class);
        billDAO = mock(BillDAO.class);

        Field f;
        f = DashboardServlet.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(servlet, userService);
        f = DashboardServlet.class.getDeclaredField("customerService");
        f.setAccessible(true);
        f.set(servlet, customerService);
        f = DashboardServlet.class.getDeclaredField("itemService");
        f.setAccessible(true);
        f.set(servlet, itemService);
        f = DashboardServlet.class.getDeclaredField("billDAO");
        f.setAccessible(true);
        f.set(servlet, billDAO);
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

    @Test
    void testOverviewKpisAreSetForAdmin() throws Exception {
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
        when(req.getPathInfo()).thenReturn("/overview");

        when(billDAO.countIssuedBetween(any(), any())).thenReturn(5, 20);
        when(customerService.countAll()).thenReturn(100);
        when(itemService.countLowStock(anyInt())).thenReturn(4);

        servlet.doGet(req, resp);

        verify(req).setAttribute("kpiDailySales", 5);
        verify(req).setAttribute("kpiMonthlySales", 20);
        verify(req).setAttribute("kpiCustomers", 100);
        verify(req).setAttribute("kpiLowStockItems", 4);
        verify(itemService).countLowStock(5);
        verify(billDAO, times(2)).countIssuedBetween(any(), any());
    }
}
