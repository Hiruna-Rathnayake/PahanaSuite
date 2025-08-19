package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.CustomerDAO;
import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {

    private static final String SELECT_BY_ID =
            "SELECT id, account_number, name, address, telephone, units_consumed FROM customers WHERE id = ?";
    private static final String SELECT_BY_ACC =
            "SELECT id, account_number, name, address, telephone, units_consumed FROM customers WHERE account_number = ?";
    private static final String SELECT_ALL =
            "SELECT id, account_number, name, address, telephone, units_consumed FROM customers ORDER BY id";
    private static final String INSERT_CUSTOMER =
            "INSERT INTO customers (account_number, name, address, telephone, units_consumed) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_CUSTOMER =
            "UPDATE customers SET account_number = ?, name = ?, address = ?, telephone = ?, units_consumed = ? WHERE id = ?";
    private static final String DELETE_CUSTOMER =
            "DELETE FROM customers WHERE id = ?";
    private static final String COUNT_ALL =
            "SELECT COUNT(*) FROM customers";

    @Override
    public Customer findById(int id) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Customer findByAccountNumber(String accountNumber) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ACC)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_CUSTOMER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getAccountNumber());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getAddress());
            ps.setString(4, customer.getTelephone());
            ps.setInt(5, customer.getUnitsConsumed());

            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) customer.setId(keys.getInt(1));
                }
                return scrub(customer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_CUSTOMER)) {

            ps.setString(1, customer.getAccountNumber());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getAddress());
            ps.setString(4, customer.getTelephone());
            ps.setInt(5, customer.getUnitsConsumed());
            ps.setInt(6, customer.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCustomer(int id) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_CUSTOMER)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countAll() {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // -- helpers --

    private static Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setAccountNumber(rs.getString("account_number"));
        c.setName(rs.getString("name"));
        c.setAddress(rs.getString("address"));
        c.setTelephone(rs.getString("telephone"));
        c.setUnitsConsumed(rs.getInt("units_consumed"));
        return scrub(c);
    }

    // TO DO
    private static Customer scrub(Customer c) {
        return c; // nothing to scrub right now
    }
}
