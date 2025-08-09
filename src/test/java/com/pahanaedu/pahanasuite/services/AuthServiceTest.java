// File: src/test/java/com/pahanaedu/pahanasuite/services/AuthServiceTest.java
package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.dao.impl.UserDAOMemoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    public void setup() {
        UserDAO userDAO = new UserDAOMemoryImpl();
        authService = new AuthService(userDAO);
    }

    @Test
    public void testLoginSuccess() {
        boolean result = authService.login("testuser", "testpass");
        Assertions.assertTrue(result, "Login should succeed with correct credentials");
    }

    @Test
    public void testLoginFailWrongPassword() {
        boolean result = authService.login("testuser", "wrongpass");
        Assertions.assertFalse(result, "Login should fail with wrong password");
    }

    @Test
    public void testLoginFailNoUser() {
        boolean result = authService.login("nouser", "testpass");
        Assertions.assertFalse(result, "Login should fail for non-existing user");
    }

    @Test
    public void testLoginFailNullInputs() {
        Assertions.assertFalse(authService.login(null, "testpass"));
        Assertions.assertFalse(authService.login("testuser", null));
        Assertions.assertFalse(authService.login(null, null));
    }
}
