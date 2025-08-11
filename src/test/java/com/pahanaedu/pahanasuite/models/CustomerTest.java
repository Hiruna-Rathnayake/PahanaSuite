package com.pahanaedu.pahanasuite.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        Customer c = new Customer(1, "ACC123", "Alice", "Addr", "12345", 10);
        assertEquals(1, c.getId());
        assertEquals("ACC123", c.getAccountNumber());
        assertEquals("Alice", c.getName());
        assertEquals("Addr", c.getAddress());
        assertEquals("12345", c.getTelephone());
        assertEquals(10, c.getUnitsConsumed());
    }

    @Test
    void testSetters() {
        Customer c = new Customer();
        c.setId(2);
        c.setAccountNumber("ACC999");
        c.setName("Bob");
        c.setAddress("Addr2");
        c.setTelephone("67890");
        c.setUnitsConsumed(20);
        assertEquals(2, c.getId());
        assertEquals("ACC999", c.getAccountNumber());
        assertEquals("Bob", c.getName());
        assertEquals("Addr2", c.getAddress());
        assertEquals("67890", c.getTelephone());
        assertEquals(20, c.getUnitsConsumed());
    }
}
