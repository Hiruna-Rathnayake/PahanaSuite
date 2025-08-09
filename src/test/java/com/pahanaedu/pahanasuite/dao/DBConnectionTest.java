package com.pahanaedu.pahanasuite.dao;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class DBConnectionTest {

    @Test
    public void testConnectionNotNull() {
        try (Connection conn = DBConnectionFactory.getConnection()) {
            assertNotNull(conn, "DB connection should not be null");
            assertFalse(conn.isClosed(), "DB connection should be open");
        } catch (Exception e) {
            fail("Exception while getting DB connection: " + e.getMessage());
        }
    }
}
