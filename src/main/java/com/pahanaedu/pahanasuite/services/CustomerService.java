package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.CustomerDAO;
import com.pahanaedu.pahanasuite.models.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates validation + business rules for Customer.
 * Keeps servlets thin: HTTP in/out stays outside this class.
 */
public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) { this.customerDAO = customerDAO; }

    /**
     * Creates a new customer after validating inputs and ensuring accountNumber uniqueness.
     * Returns the created customer or null on failure.
     */
    public Customer create(String accountNumber, String name, String address, String telephone, int unitsConsumed) {
        accountNumber = trim(accountNumber);
        name = trim(name);
        address = trim(address);
        telephone = trim(telephone);

        // Basic validation
        if (isBlank(accountNumber) || isBlank(name)) return null;
        if (unitsConsumed < 0) return null;
        if (!isValidAccountNumber(accountNumber)) return null;

        // Enforce unique business key
        if (customerDAO.findByAccountNumber(accountNumber) != null) return null;

        Customer c = new Customer(accountNumber, name, address, telephone, unitsConsumed);
        return customerDAO.createCustomer(c);
    }

    /**
     * Updates an existing customer by id. accountNumber can also be updated if still unique.
     * Returns the updated customer or null on failure.
     */
    public Customer update(int id, String accountNumber, String name, String address, String telephone, int unitsConsumed) {
        if (id <= 0) return null;

        accountNumber = trim(accountNumber);
        name = trim(name);
        address = trim(address);
        telephone = trim(telephone);

        if (isBlank(accountNumber) || isBlank(name)) return null;
        if (unitsConsumed < 0) return null;
        if (!isValidAccountNumber(accountNumber)) return null;

        Customer existing = customerDAO.findById(id);
        if (existing == null) return null;

        // If accountNumber changes, confirm uniqueness
        if (!accountNumber.equals(existing.getAccountNumber())) {
            Customer byAcc = customerDAO.findByAccountNumber(accountNumber);
            if (byAcc != null) return null;
        }

        existing.setAccountNumber(accountNumber);
        existing.setName(name);
        existing.setAddress(address);
        existing.setTelephone(telephone);
        existing.setUnitsConsumed(unitsConsumed);

        boolean ok = customerDAO.updateCustomer(existing);
        return ok ? existing : null;
    }

    /**
     * Retrieves a customer by id.
     */
    public Customer getById(int id) {
        if (id <= 0) return null;
        return customerDAO.findById(id);
    }

    /**
     * Retrieves a customer by accountNumber.
     */
    public Customer getByAccountNumber(String accountNumber) {
        accountNumber = trim(accountNumber);
        if (isBlank(accountNumber)) return null;
        return customerDAO.findByAccountNumber(accountNumber);
    }

    /**
     * Lists all customers.
     */
    public List<Customer> listAll() {
        List<Customer> list = customerDAO.findAll();
        return list != null ? list : new ArrayList<>();
    }

    /**
     * Deletes a customer by id.
     */
    public boolean delete(int id) {
        if (id <= 0) return false;
        return customerDAO.deleteCustomer(id);
    }

    // --- helpers ---

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    // Simple rule: 3–20 chars, letters/digits/hyphen only. Adjust as needed.
    private static boolean isValidAccountNumber(String s) {
        if (s == null) return false;
        int len = s.length();
        if (len < 3 || len > 20) return false;
        return s.matches("[A-Za-z0-9-]+");
    }
}
