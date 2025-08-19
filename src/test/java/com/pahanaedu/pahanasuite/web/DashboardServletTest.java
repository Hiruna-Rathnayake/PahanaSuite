package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.models.Item;
import com.pahanaedu.pahanasuite.models.User;
import com.pahanaedu.pahanasuite.services.CustomerService;
import com.pahanaedu.pahanasuite.services.ItemService;
import com.pahanaedu.pahanasuite.services.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardServletTest {
    @Test
    void overviewSetsLowStockItemsAttribute() throws Exception {
        DashboardServlet servlet = new DashboardServlet();

        ItemService itemService = Mockito.mock(ItemService.class);
        CustomerService customerService = Mockito.mock(CustomerService.class);
        UserService userService = Mockito.mock(UserService.class);
        BillDAO billDAO = Mockito.mock(BillDAO.class);

        List<Item> lowItems = List.of(new Item());
        when(itemService.findLowStock(anyInt(), anyInt())).thenReturn(lowItems);
        when(itemService.countLowStock(anyInt())).thenReturn(lowItems.size());
        when(customerService.countAll()).thenReturn(0);
        when(billDAO.countIssuedBetween(any(), any())).thenReturn(0);
        when(billDAO.findRecent(anyInt())).thenReturn(Collections.emptyList());

        java.lang.reflect.Field f;
        f = DashboardServlet.class.getDeclaredField("itemService");
        f.setAccessible(true);
        f.set(servlet, itemService);
        f = DashboardServlet.class.getDeclaredField("customerService");
        f.setAccessible(true);
        f.set(servlet, customerService);
        f = DashboardServlet.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(servlet, userService);
        f = DashboardServlet.class.getDeclaredField("billDAO");
        f.setAccessible(true);
        f.set(servlet, billDAO);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("isLoggedIn")).thenReturn(true);
        User user = new User();
        user.setUsername("u");
        user.setRole("admin");
        when(session.getAttribute("user")).thenReturn(user);
        when(req.getPathInfo()).thenReturn("/overview");

        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher("/dashboard.jsp")).thenReturn(rd);

        servlet.doGet(req, resp);

        verify(req).setAttribute("lowStockItems", lowItems);
        verify(req).setAttribute("lowStockCount", lowItems.size());
    }
}
