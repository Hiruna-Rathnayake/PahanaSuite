package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.Customer;
import java.util.List;

public interface CustomerDAO {
    Customer findById(int id);
    Customer findByAccountNumber(String accountNumber);
    List<Customer> findAll();

    Customer createCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    boolean deleteCustomer(int id);

    /** Counts all customers. */
    int countAll();
}
