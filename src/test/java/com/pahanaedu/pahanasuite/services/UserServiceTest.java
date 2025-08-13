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

    private UserDAO userDAO;
    private UserService svc;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        svc = new UserService(userDAO);
    }

    // -------- login --------

    @Test
    void login_returnsNull_onBlankInputs() {
        assertNull(svc.login(null, "pw"));
        assertNull(svc.login("  ", "pw"));
        assertNull(svc.login("bob", null));
        assertNull(svc.login("bob", "  "));
    }

    @Test
    void login_returnsNull_whenUserMissingOrNoPasswordInRow() {
        when(userDAO.findAuthByUsername("bob")).thenReturn(null);
        assertNull(svc.login("bob", "pw"));

        when(userDAO.findAuthByUsername("bob")).thenReturn(new User(1, "bob", null, "manager"));
        assertNull(svc.login("bob", "pw"));
    }

    @Test
    void login_returnsSanitizedUser_onPasswordMatch() {
        User authRow = new User(1, "bob", "pw", "manager");
        when(userDAO.findAuthByUsername("bob")).thenReturn(authRow);

        User result = svc.login("  bob  ", "pw");
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("bob", result.getUsername());
        assertEquals("manager", result.getRole());
        assertNull(result.getPassword()); // password scrubbed
    }

    @Test
    void login_returnsNull_onPasswordMismatch() {
        User authRow = new User(1, "bob", "pw", "manager");
        when(userDAO.findAuthByUsername("bob")).thenReturn(authRow);

        assertNull(svc.login("bob", "wrong"));
    }

    // -------- create --------

    @Test
    void create_returnsNull_onBlankInputsOrInvalidRole() {
        assertNull(svc.create(null, "pw", "admin"));
        assertNull(svc.create("bob", null, "admin"));
        assertNull(svc.create("bob", "pw", null));
        assertNull(svc.create("  ", "pw", "admin"));
        assertNull(svc.create("bob", "  ", "admin"));

        // invalid role rejected by normalization
        assertNull(svc.create("bob", "pw", "superuser"));
    }

    @Test
    void create_returnsSanitizedUser_onSuccess() {
        User created = new User(10, "bob", null, "manager");
        when(userDAO.createUser(any(User.class))).thenReturn(created);

        User result = svc.create(" bob ", " pw ", " MANAGER ");
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("bob", result.getUsername());
        assertEquals("manager", result.getRole());
        assertNull(result.getPassword()); // scrubbed

        // DAO received normalized role and rawPassword pre-hash (per TODO)
        verify(userDAO).createUser(argThat(u ->
                "bob".equals(u.getUsername()) &&
                        "pw".equals(u.getPassword()) &&
                        "manager".equals(u.getRole())
        ));
    }

    @Test
    void create_returnsNull_whenDaoFails() {
        when(userDAO.createUser(any())).thenReturn(null);
        assertNull(svc.create("bob", "pw", "manager"));
    }

    // -------- listAll --------

    @Test
    void listAll_returnsEmpty_onNullFromDao() {
        when(userDAO.findAll()).thenReturn(null);
        assertTrue(svc.listAll().isEmpty());
    }

//    @Test
//    void listAll_scrubsPasswords() {
//        List<User> rows = List.of(
//                new User(1, "a", "x", "admin"),
//                new User(2, "b", "y", "manager"),
//                null
//        );
//        when(userDAO.findAll()).thenReturn(rows);
//
//        List<User> result = svc.listAll();
//        assertEquals(3, result.size());
//        assertNull(result.get(0).getPassword());
//        assertNull(result.get(1).getPassword());
//        assertNull(result.get(2)); // preserves nulls as-is
//    }

    // -------- getById --------

    @Test
    void getById_validatesIdAndScrubsPassword() {
        assertNull(svc.getById(0));
        assertNull(svc.getById(-5));

        when(userDAO.findById(7)).thenReturn(new User(7, "u", "secret", "cashier"));
        User u = svc.getById(7);
        assertNotNull(u);
        assertEquals(7, u.getId());
        assertNull(u.getPassword());
    }

    // -------- update --------

    @Test
    void update_returnsNull_whenInvalidInputs() {
        assertNull(svc.update(0, "bob", "admin"));
        assertNull(svc.update(5, "  ", "admin"));
        assertNull(svc.update(5, "bob", "unknown")); // role not allowed
    }

    @Test
    void update_returnsNull_whenUserMissing() {
        when(userDAO.findById(9)).thenReturn(null);
        assertNull(svc.update(9, "new", "admin"));
    }

    @Test
    void update_returnsNull_whenDaoUpdateFails() {
        when(userDAO.findById(9)).thenReturn(new User(9, "old", null, "cashier"));
        when(userDAO.updateUser(any())).thenReturn(false);
        assertNull(svc.update(9, "new", "manager"));
    }

    @Test
    void update_returnsSanitizedUser_onSuccess() {
        User existing = new User(9, "old", "secret", "cashier");
        when(userDAO.findById(9)).thenReturn(existing);
        when(userDAO.updateUser(any())).thenReturn(true);

        User updated = svc.update(9, " newName ", " ADMIN ");
        assertNotNull(updated);
        assertEquals(9, updated.getId());
        assertEquals("newName", updated.getUsername());
        assertEquals("admin", updated.getRole());
        assertNull(updated.getPassword()); // scrubbed

        verify(userDAO).updateUser(argThat(u ->
                u.getId() == 9 &&
                        "newName".equals(u.getUsername()) &&
                        "admin".equals(u.getRole())
        ));
    }

    // -------- delete --------

    @Test
    void delete_validatesId_andPassesThroughDao() {
        assertFalse(svc.delete(0));
        assertFalse(svc.delete(-1));

        when(userDAO.deleteUser(3)).thenReturn(true);
        assertTrue(svc.delete(3));
        verify(userDAO).deleteUser(3);
    }

    // -------- resetPassword --------

    @Test
    void resetPassword_validatesInputs() {
        assertFalse(svc.resetPassword(0, "x"));
        assertFalse(svc.resetPassword(1, null));
        assertFalse(svc.resetPassword(1, "   "));
    }

    @Test
    void resetPassword_callsDao_onValidInputs() {
        when(userDAO.resetPassword(2, "newpw")).thenReturn(true);
        assertTrue(svc.resetPassword(2, "  newpw  "));
        verify(userDAO).resetPassword(2, "newpw");
    }
}
