// AuthServiceTest.java
package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.impl.UserDAOMemoryImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    public void testLoginSuccess() {
        AuthService authService = new AuthService(new UserDAOMemoryImpl());
        assertTrue(authService.login("testuser", "testpass"));
    }

    @Test
    public void testLoginFailWrongPassword() {
        AuthService authService = new AuthService(new UserDAOMemoryImpl());
        assertFalse(authService.login("testuser", "wrongpass"));
    }

    @Test
    public void testLoginFailUserNotFound() {
        AuthService authService = new AuthService(new UserDAOMemoryImpl());
        assertFalse(authService.login("unknown", "testpass"));
    }
}
