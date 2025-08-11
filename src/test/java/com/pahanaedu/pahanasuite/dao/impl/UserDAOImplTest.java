package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserDAOImplTest {

    @Test
    void testFindByIdReturnsUser() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("alice");
        when(rs.getString("role")).thenReturn("admin");

        try (MockedStatic<DBConnectionFactory> mocked = mockStatic(DBConnectionFactory.class)) {
            mocked.when(DBConnectionFactory::getConnection).thenReturn(conn);

            UserDAOImpl dao = new UserDAOImpl();
            User user = dao.findById(1);
            assertNotNull(user);
            assertEquals("alice", user.getUsername());
            assertNull(user.getPassword());
            assertEquals("admin", user.getRole());
        }
    }

    @Test
    void testCreateUserScrubsPassword() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getInt(1)).thenReturn(10);

        try (MockedStatic<DBConnectionFactory> mocked = mockStatic(DBConnectionFactory.class)) {
            mocked.when(DBConnectionFactory::getConnection).thenReturn(conn);

            UserDAOImpl dao = new UserDAOImpl();
            User created = dao.createUser(new User("bob", "pw"));
            assertNotNull(created);
            assertEquals(10, created.getId());
            assertNull(created.getPassword());
        }
    }
}
