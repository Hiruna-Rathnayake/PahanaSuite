package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CustomerDAOImplTest {

    @Test
    void testFindByIdReturnsCustomer() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("account_number")).thenReturn("ACC");
        when(rs.getString("name")).thenReturn("Alice");
        when(rs.getString("address")).thenReturn("Addr");
        when(rs.getString("telephone")).thenReturn("123");
        when(rs.getInt("units_consumed")).thenReturn(5);

        try (MockedStatic<DBConnectionFactory> mocked = mockStatic(DBConnectionFactory.class)) {
            mocked.when(DBConnectionFactory::getConnection).thenReturn(conn);

            CustomerDAOImpl dao = new CustomerDAOImpl();
            Customer c = dao.findById(1);
            assertNotNull(c);
            assertEquals("ACC", c.getAccountNumber());
            assertEquals("Alice", c.getName());
            assertEquals("Addr", c.getAddress());
            assertEquals("123", c.getTelephone());
            assertEquals(5, c.getUnitsConsumed());
        }
    }

    @Test
    void testCreateCustomerReturnsTrue() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnectionFactory> mocked = mockStatic(DBConnectionFactory.class)) {
            mocked.when(DBConnectionFactory::getConnection).thenReturn(conn);

            CustomerDAOImpl dao = new CustomerDAOImpl();
            Customer c = new Customer("ACC", "Alice", "Addr", "123", 5);
            assertTrue(dao.createCustomer(c));
        }
    }
}
