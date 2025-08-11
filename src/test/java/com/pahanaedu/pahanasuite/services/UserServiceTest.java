package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.UserDAO;
import com.pahanaedu.pahanasuite.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserDAO userDAO;
    private UserService service;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        service = new UserService(userDAO);
    }

    @Test
    void loginReturnsSanitizedUserOnSuccess() {
        User authUser = new User(1, "alice", "secret", "admin");
        when(userDAO.findAuthByUsername("alice")).thenReturn(authUser);

        User result = service.login(" alice ", "secret");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("alice", result.getUsername());
        assertEquals("admin", result.getRole());
        assertNull(result.getPassword(), "password should be scrubbed");
    }

    @Test
    void loginFailsWithWrongPassword() {
        User authUser = new User(1, "alice", "secret", "admin");
        when(userDAO.findAuthByUsername("alice")).thenReturn(authUser);

        assertNull(service.login("alice", "bad"));
    }

    @Test
    void loginRejectsBlankCredentials() {
        assertNull(service.login(" ", "pw"));
        assertNull(service.login("alice", " "));
        assertNull(service.login(null, "pw"));
        assertNull(service.login("alice", null));
        verifyNoInteractions(userDAO);
    }

    @Test
    void createSanitizesAndValidatesInput() {
        when(userDAO.createUser(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99);
            u.setPassword(null); // DAO is expected to clear password
            return u;
        });

        User result = service.create(" Bob ", "pw", "ADMIN ");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).createUser(captor.capture());
        User passed = captor.getValue();
        assertEquals("Bob", passed.getUsername());
        assertEquals("pw", passed.getPassword());
        assertEquals("admin", passed.getRole());

        assertNotNull(result);
        assertEquals(99, result.getId());
        assertNull(result.getPassword(), "password should be scrubbed");
    }

    @Test
    void createRejectsInvalidRole() {
        assertNull(service.create("bob", "pw", "hacker"));
        verify(userDAO, never()).createUser(any());
    }

    @Test
    void listAllScrubsPasswordsAndHandlesNull() {
        User u = new User(1, "alice", "secret", "admin");
        when(userDAO.findAll()).thenReturn(List.of(u));
        List<User> result = service.listAll();
        assertEquals(1, result.size());
        assertNull(result.get(0).getPassword(), "password should be scrubbed");

        when(userDAO.findAll()).thenReturn(null);
        assertTrue(service.listAll().isEmpty());
    }

    @Test
    void getByIdReturnsSanitizedUser() {
        User u = new User(1, "alice", "secret", "admin");
        when(userDAO.findById(1)).thenReturn(u);

        User result = service.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("alice", result.getUsername());
        assertNull(result.getPassword(), "password should be scrubbed");
        verify(userDAO).findById(1);
        verifyNoMoreInteractions(userDAO);
    }

    @Test
    void getByIdValidatesId() {
        assertNull(service.getById(0));
        verifyNoInteractions(userDAO);
    }

    @Test
    void updateModifiesExistingUserAndSanitizes() {
        User existing = new User(1, "old", "pw", "cashier");
        when(userDAO.findById(1)).thenReturn(existing);
        when(userDAO.updateUser(existing)).thenReturn(true);

        User result = service.update(1, " New ", "MANAGER ");

        assertNotNull(result);
        assertEquals("New", existing.getUsername());
        assertEquals("manager", existing.getRole());
        assertNull(result.getPassword(), "password should be scrubbed");
    }

    @Test
    void deleteValidatesId() {
        when(userDAO.deleteUser(1)).thenReturn(true);
        assertTrue(service.delete(1));
        assertFalse(service.delete(0));
        verify(userDAO, times(1)).deleteUser(1);
    }

    @Test
    void resetPasswordValidatesInput() {
        when(userDAO.resetPassword(1, "new")).thenReturn(true);
        assertTrue(service.resetPassword(1, " new "));
        assertFalse(service.resetPassword(0, "x"));
        assertFalse(service.resetPassword(1, " "));
        verify(userDAO, times(1)).resetPassword(1, "new");
    }
}

