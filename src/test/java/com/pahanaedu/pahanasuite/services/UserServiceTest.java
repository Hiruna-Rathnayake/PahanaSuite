// File: src/test/java/com/pahanaedu/pahanasuite/services/UserServiceTest.java
package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserDAO userDAOMock;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAOMock = mock(UserDAO.class);
        userService = new UserService(userDAOMock);
    }

    @Test
    void login_success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass123");

        when(userDAOMock.findByUsername("testuser")).thenReturn(user);

        User result = userService.login("testuser", "pass123");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void login_fail_wrongPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass123");

        when(userDAOMock.findByUsername("testuser")).thenReturn(user);

        User result = userService.login("testuser", "wrongpass");
        assertNull(result);
    }

    @Test
    void login_fail_userNotFound() {
        when(userDAOMock.findByUsername("unknown")).thenReturn(null);

        User result = userService.login("unknown", "any");
        assertNull(result);
    }

    @Test
    void register_success() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("pass");

        when(userDAOMock.createUser(user)).thenReturn(true);

        boolean result = userService.register(user);
        assertTrue(result);
    }

    @Test
    void register_fail() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("pass");

        when(userDAOMock.createUser(user)).thenReturn(false);

        boolean result = userService.register(user);
        assertFalse(result);
    }
}
