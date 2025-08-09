package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.CustomerDAO;
import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDAOImpl implements CustomerDAO {

    private static final String SELECT_CUSTOMER_SQL = "SELECT id, user_id, account_number, name, address, telephone, units_consumed FROM customers WHERE user_id = ?";
    private static final String INSERT_CUSTOMER_SQL = "INSERT INTO customers (user_id, account_number, name, address, telephone, units_consumed) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_CUSTOMER_SQL = "UPDATE customers SET account_number = ?, name = ?, address = ?, telephone = ?, units_consumed = ? WHERE user_id = ?";

    @Override
    public Customer findByUserId(int userId) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMER_SQL)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setId(rs.getInt("id"));
                    customer.setUserId(rs.getInt("user_id"));
                    customer.setAccountNumber(rs.getString("account_number"));
                    customer.setName(rs.getString("name"));
                    customer.setAddress(rs.getString("address"));
                    customer.setTelephone(rs.getString("telephone"));
                    customer.setUnitsConsumed(rs.getInt("units_consumed"));
                    return customer;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean createCustomer(Customer customer) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CUSTOMER_SQL)) {

            stmt.setInt(1, customer.getUserId());
            stmt.setString(2, customer.getAccountNumber());
            stmt.setString(3, customer.getName());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getTelephone());
            stmt.setInt(6, customer.getUnitsConsumed());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CUSTOMER_SQL)) {

            stmt.setString(1, customer.getAccountNumber());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getTelephone());
            stmt.setInt(5, customer.getUnitsConsumed());
            stmt.setInt(6, customer.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
