package com.pahanaedu.pahanasuite.dao;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DBConnectionFactoryTest {

    @Test
    void testGetConnectionUsesDriverManager() throws Exception {
        Connection conn = mock(Connection.class);
        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(conn);
            Connection result = DBConnectionFactory.getConnection();
            assertEquals(conn, result);
        }
    }
}
