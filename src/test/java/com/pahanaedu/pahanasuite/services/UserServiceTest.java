// File: src/test/java/com/pahanaedu/pahanasuite/services/UserServiceTest.java
package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserDAO userDAOMock;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAOMock = mock(UserDAO.class);
        userService = new UserService(userDAOMock);
    }

    // --- login() ---

    @Test
    void login_success() {
        // DAO returns auth row including password
        User authRow = new User();
        authRow.setId(1);
        authRow.setUsername("testuser");
        authRow.setPassword("pass123");
        authRow.setRole("manager");

        when(userDAOMock.findAuthByUsername("testuser")).thenReturn(authRow);

        User result = userService.login("testuser", "pass123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("manager", result.getRole());
        assertNull(result.getPassword(), "Service must scrub password on return");
    }

    @Test
    void login_fail_wrongPassword() {
        User authRow = new User();
        authRow.setId(1);
        authRow.setUsername("testuser");
        authRow.setPassword("pass123");
        authRow.setRole("manager");

        when(userDAOMock.findAuthByUsername("testuser")).thenReturn(authRow);

        User result = userService.login("testuser", "wrongpass");
        assertNull(result);
    }

    @Test
    void login_fail_userNotFound() {
        when(userDAOMock.findAuthByUsername("unknown")).thenReturn(null);

        User result = userService.login("unknown", "any");
        assertNull(result);
    }

    // --- create() ---

    @Test
    void create_success() {
        // Service will pass a User with username/password/role into DAO
        User created = new User();
        created.setId(42);
        created.setUsername("newuser");
        created.setRole("cashier");
        created.setPassword(null); // DAO typically scrubs on return

        when(userDAOMock.createUser(any(User.class))).thenReturn(created);

        User result = userService.create("newuser", "pass", "cashier");

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals("cashier", result.getRole());
        assertNull(result.getPassword(), "Service must scrub password on return");
        verify(userDAOMock).createUser(any(User.class));
    }

    @Test
    void create_fail_invalidInputs() {
        // blank username → null
        assertNull(userService.create("   ", "pass", "cashier"));
        // blank password → null
        assertNull(userService.create("user", "   ", "cashier"));
        // invalid role → null (service whitelists roles)
        assertNull(userService.create("user", "pass", "godmode"));
        verify(userDAOMock, never()).createUser(any());
    }

    @Test
    void create_fail_daoReturnsNull() {
        when(userDAOMock.createUser(any(User.class))).thenReturn(null);
        assertNull(userService.create("u1", "p1", "cashier"));
    }

    // --- listAll() scrubs passwords ---

    @Test
    void listAll_scrubsPasswords() {
        User u1 = new User(1, "a", "secret", "admin");
        User u2 = new User(2, "b", "secret2", "cashier");
        when(userDAOMock.findAll()).thenReturn(List.of(u1, u2));

        var result = userService.listAll();

        assertEquals(2, result.size());
        assertNull(result.get(0).getPassword());
        assertNull(result.get(1).getPassword());
    }
}
