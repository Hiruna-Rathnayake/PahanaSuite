package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.dao.PaymentDAO;
import com.pahanaedu.pahanasuite.models.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    private static final String INSERT = """
        INSERT INTO payments (bill_id, amount, method, reference, paid_at)
        VALUES (?,?,?,?,?)
    """;

    private static final String SELECT_BY_BILL = """
        SELECT id, bill_id, amount, method, reference, paid_at
        FROM payments WHERE bill_id = ? ORDER BY id
    """;

    private static final String SUM_BY_BILL = "SELECT COALESCE(SUM(amount),0) AS paid FROM payments WHERE bill_id = ?";

    @Override
    public Payment create(Payment p) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getBillId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setString(3, p.getMethod());
            ps.setString(4, p.getReference());
            ps.setTimestamp(5, Timestamp.valueOf(p.getPaidAt() == null ? LocalDateTime.now() : p.getPaidAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Payment> findByBillId(int billId) {
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_BILL)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public BigDecimal sumByBillId(int billId) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SUM_BY_BILL)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal v = rs.getBigDecimal("paid");
                    return v == null ? BigDecimal.ZERO.setScale(2) : v.setScale(2);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return BigDecimal.ZERO.setScale(2);
    }

    private static Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setBillId(rs.getInt("bill_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setMethod(rs.getString("method"));
        p.setReference(rs.getString("reference"));
        Timestamp t = rs.getTimestamp("paid_at");
        p.setPaidAt(t == null ? null : t.toLocalDateTime());
        return p;
    }
}
