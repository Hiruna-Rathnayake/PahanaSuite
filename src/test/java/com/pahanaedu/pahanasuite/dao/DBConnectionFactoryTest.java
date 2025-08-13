// File: src/test/java/com/pahanaedu/pahanasuite/dao/DBConnectionFactoryTest.java
package com.pahanaedu.pahanasuite.dao;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DBConnectionFactory without hitting a real database.
 * Uses Mockito static mocking for Class.forName and DriverManager.
 *
 * Requires mockito-inline on the test classpath, e.g.:
 * testImplementation "org.mockito:mockito-inline:5.x.y"
 */
class DBConnectionFactoryTest {

    private static final String URL = "jdbc:mysql://localhost:3306/pahanaedu";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @Test
    void getConnection_returnsMock_onHappyPath() throws Exception {
        Connection mockConn = mock(Connection.class);

        try (MockedStatic<Class> classMock = mockStatic(Class.class);
             MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {

            // Simulate driver class present
            classMock.when(() -> Class.forName("com.mysql.cj.jdbc.Driver"))
                    .thenReturn(Object.class);

            // Return a mocked connection for the expected args
            dmMock.when(() -> DriverManager.getConnection(URL, USER, PASSWORD))
                    .thenReturn(mockConn);

            Connection c = DBConnectionFactory.getConnection();

            assertNotNull(c);
            assertSame(mockConn, c);

            // Verify both static calls happened as expected
            classMock.verify(() -> Class.forName("com.mysql.cj.jdbc.Driver"));
            dmMock.verify(() -> DriverManager.getConnection(URL, USER, PASSWORD));
        }
    }

    @Test
    void getConnection_throws_whenDriverClassMissing() {
        try (MockedStatic<Class> classMock = mockStatic(Class.class);
             MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {

            classMock.when(() -> Class.forName("com.mysql.cj.jdbc.Driver"))
                    .thenThrow(new ClassNotFoundException("no driver"));

            Exception ex = assertThrows(Exception.class, DBConnectionFactory::getConnection);
            assertTrue(ex instanceof ClassNotFoundException
                    || ex.getCause() instanceof ClassNotFoundException);

            // If driver didn’t load, we shouldn’t try to hit DriverManager
            dmMock.verifyNoInteractions();
        }
    }

    @Test
    void getConnection_throws_whenDriverManagerFails() throws Exception {
        try (MockedStatic<Class> classMock = mockStatic(Class.class);
             MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {

            classMock.when(() -> Class.forName("com.mysql.cj.jdbc.Driver"))
                    .thenReturn(Object.class);

            dmMock.when(() -> DriverManager.getConnection(URL, USER, PASSWORD))
                    .thenThrow(new SQLException("boom"));

            Exception ex = assertThrows(Exception.class, DBConnectionFactory::getConnection);
            assertTrue(ex instanceof SQLException || ex.getCause() instanceof SQLException);
        }
    }
}
