package com.pahanaedu.pahanasuite.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        User u = new User(1, "alice", "secret", "admin");
        assertEquals(1, u.getId());
        assertEquals("alice", u.getUsername());
        assertEquals("secret", u.getPassword());
        assertEquals("admin", u.getRole());
    }

    @Test
    void testSetters() {
        User u = new User();
        u.setId(2);
        u.setUsername("bob");
        u.setPassword("pw");
        u.setRole("manager");
        assertEquals(2, u.getId());
        assertEquals("bob", u.getUsername());
        assertEquals("pw", u.getPassword());
        assertEquals("manager", u.getRole());
    }
}
