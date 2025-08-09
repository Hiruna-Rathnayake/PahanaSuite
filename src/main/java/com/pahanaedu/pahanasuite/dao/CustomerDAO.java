package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.Customer;

public interface CustomerDAO {
    Customer findByUserId(int userId);
    boolean createCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    // Add other customer-specific methods as needed
}
