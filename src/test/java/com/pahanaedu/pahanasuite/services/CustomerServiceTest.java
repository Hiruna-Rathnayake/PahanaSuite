package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.CustomerDAO;
import com.pahanaedu.pahanasuite.models.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    private CustomerDAO dao;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        dao = mock(CustomerDAO.class);
        service = new CustomerService(dao);
    }

    // -------- create() --------

    @Test
    void create_success() {
        when(dao.findByAccountNumber("ACC-001")).thenReturn(null);
        Customer created = new Customer("ACC-001", "Alice", "Addr", "0771234567", 10);
        created.setId(42);
        when(dao.createCustomer(any(Customer.class))).thenReturn(created);

        Customer out = service.create(" ACC-001 ", " Alice ", " Addr ", " 0771234567 ", 10);

        assertNotNull(out);
        assertEquals(42, out.getId());
        assertEquals("ACC-001", out.getAccountNumber());
        assertEquals("Alice", out.getName());
        verify(dao).findByAccountNumber("ACC-001");
        verify(dao).createCustomer(any(Customer.class));
    }

    @Test
    void create_fail_invalidInputs() {
        // blank account number
        assertNull(service.create("   ", "Alice", null, null, 0));
        // blank name
        assertNull(service.create("ACC-001", "   ", null, null, 0));
        // negative units
        assertNull(service.create("ACC-001", "Alice", null, null, -1));
        // invalid account number pattern
        assertNull(service.create("ACC 001", "Alice", null, null, 0));
        verifyNoInteractions(dao);
    }

    @Test
    void create_fail_duplicateAccountNumber() {
        when(dao.findByAccountNumber("ACC-001")).thenReturn(new Customer());
        assertNull(service.create("ACC-001", "Alice", null, null, 0));
        verify(dao).findByAccountNumber("ACC-001");
        verify(dao, never()).createCustomer(any());
    }

    // -------- update() --------

    @Test
    void update_success_sameAccountNumber() {
        Customer existing = new Customer("ACC-001", "Old", "A", "T", 5);
        existing.setId(7);
        when(dao.findById(7)).thenReturn(existing);
        when(dao.updateCustomer(any(Customer.class))).thenReturn(true);

        Customer out = service.update(7, "ACC-001", "Alice", "Addr", "077", 11);

        assertNotNull(out);
        assertEquals(7, out.getId());
        assertEquals("ACC-001", out.getAccountNumber());
        assertEquals("Alice", out.getName());
        assertEquals(11, out.getUnitsConsumed());
        verify(dao, never()).findByAccountNumber("ACC-001"); // no uniqueness lookup if unchanged
        verify(dao).updateCustomer(existing);
    }

    @Test
    void update_success_changeAccountNumber_unique() {
        Customer existing = new Customer("ACC-001", "Old", "A", "T", 5);
        existing.setId(7);
        when(dao.findById(7)).thenReturn(existing);
        when(dao.findByAccountNumber("ACC-002")).thenReturn(null);
        when(dao.updateCustomer(any(Customer.class))).thenReturn(true);

        Customer out = service.update(7, "ACC-002", "Alice", "Addr", "077", 11);

        assertNotNull(out);
        assertEquals("ACC-002", out.getAccountNumber());
        verify(dao).findByAccountNumber("ACC-002");
        verify(dao).updateCustomer(existing);
    }

    @Test
    void update_fail_duplicateNewAccountNumber() {
        Customer existing = new Customer("ACC-001", "Old", null, null, 0);
        existing.setId(7);
        when(dao.findById(7)).thenReturn(existing);
        when(dao.findByAccountNumber("ACC-002")).thenReturn(new Customer());

        assertNull(service.update(7, "ACC-002", "Alice", null, null, 1));
        verify(dao).findByAccountNumber("ACC-002");
        verify(dao, never()).updateCustomer(any());
    }

    @Test
    void update_fail_notFoundOrInvalid() {
        // not found
        when(dao.findById(99)).thenReturn(null);
        assertNull(service.update(99, "ACC-001", "Alice", null, null, 0));

        // invalid id
        assertNull(service.update(0, "ACC-001", "Alice", null, null, 0));
        assertNull(service.update(-1, "ACC-001", "Alice", null, null, 0));

        // invalid fields
        Customer existing = new Customer("ACC-001", "Old", null, null, 0);
        existing.setId(1);
        when(dao.findById(1)).thenReturn(existing);
        assertNull(service.update(1, "   ", "Alice", null, null, 0));   // blank acc
        assertNull(service.update(1, "ACC 001", "Alice", null, null, 0)); // bad pattern
        assertNull(service.update(1, "ACC-001", "   ", null, null, 0)); // blank name
        assertNull(service.update(1, "ACC-001", "Alice", null, null, -5)); // negative units
        verify(dao, atLeastOnce()).findById(anyInt());
    }

    // -------- getters/list/delete --------

    @Test
    void getById_invalidOrMissing() {
        assertNull(service.getById(0));
        assertNull(service.getById(-1));
        when(dao.findById(5)).thenReturn(new Customer());
        assertNotNull(service.getById(5));
    }

    @Test
    void getByAccountNumber_trimsAndRejectsBlank() {
        assertNull(service.getByAccountNumber("   "));
        when(dao.findByAccountNumber("ACC-001")).thenReturn(new Customer());
        assertNotNull(service.getByAccountNumber(" ACC-001 "));
        verify(dao).findByAccountNumber("ACC-001");
    }

    @Test
    void listAll_handlesNullFromDao() {
        when(dao.findAll()).thenReturn(null);
        List<Customer> out = service.listAll();
        assertNotNull(out);
        assertTrue(out.isEmpty());

        when(dao.findAll()).thenReturn(List.of(new Customer(), new Customer()));
        assertEquals(2, service.listAll().size());
    }

    @Test
    void delete_checksIdAndDelegates() {
        assertFalse(service.delete(0));
        when(dao.deleteCustomer(10)).thenReturn(true);
        assertTrue(service.delete(10));
        verify(dao, times(1)).deleteCustomer(10);
    }
}
