package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.models.BillStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class BillDAOImpl implements BillDAO {

    private static final String INSERT_BILL = """
        INSERT INTO bills
          (bill_no, customer_id, issued_at, due_at, status, subtotal, discount_amount, tax_amount, total)
        VALUES (?,?,?,?,?,?,?,?,?)
        """;

    private static final String INSERT_LINE = """
        INSERT INTO bill_lines
          (bill_id, item_id, sku, name, quantity, unit_price, line_discount, line_total)
        VALUES (?,?,?,?,?,?,?,?)
        """;

    private static final String SELECT_BILL = """
        SELECT id, bill_no, customer_id, issued_at, due_at, status,
               subtotal, discount_amount, tax_amount, total
        FROM bills WHERE id = ?
        """;

    private static final String SELECT_LINES = """
        SELECT id, item_id, sku, name, quantity, unit_price, line_discount, line_total
        FROM bill_lines WHERE bill_id = ? ORDER BY id
        """;

    private static final String SELECT_ALL = """
        SELECT id, bill_no, customer_id, issued_at, due_at, status,
               subtotal, discount_amount, tax_amount, total
        FROM bills ORDER BY id
        """;

    private static final String SELECT_RECENT = """
        SELECT id, bill_no, customer_id, issued_at, due_at, status,
               subtotal, discount_amount, tax_amount, total
        FROM bills ORDER BY issued_at DESC LIMIT ?
        """;

    private static final String SELECT_BY_CUSTOMER = """
        SELECT id, bill_no, customer_id, issued_at, due_at, status,
               subtotal, discount_amount, tax_amount, total
          FROM bills WHERE customer_id = ? ORDER BY id
        """;

    private static final String SELECT_ISSUED_BETWEEN = """
        SELECT id, bill_no, customer_id, issued_at, due_at, status,
               subtotal, discount_amount, tax_amount, total
          FROM bills WHERE issued_at >= ? AND issued_at < ? ORDER BY issued_at
        """;

    private static final String UPDATE_BILL = """
        UPDATE bills
           SET bill_no=?, customer_id=?, issued_at=?, due_at=?, status=?,
               subtotal=?, discount_amount=?, tax_amount=?, total=?
         WHERE id=?
        """;

    private static final String DELETE_LINES = "DELETE FROM bill_lines WHERE bill_id = ?";
    private static final String DELETE_BILL  = "DELETE FROM bills WHERE id = ?";
    private static final String COUNT_ISSUED_BETWEEN =
            "SELECT COUNT(*) FROM bills WHERE issued_at >= ? AND issued_at < ?";
    private static final String SUM_TOTAL_ISSUED_BETWEEN =
            "SELECT COALESCE(SUM(total),0) FROM bills WHERE issued_at >= ? AND issued_at < ?";

    @Override
    public Bill createBill(Bill bill) {
        if (bill == null) return null;

        // Drop lines with qty <= 0 to avoid weird rows
        bill.getLines().removeIf(l -> l == null || l.getQuantity() <= 0);

        try (Connection conn = DBConnectionFactory.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_BILL, Statement.RETURN_GENERATED_KEYS)) {
                bill.recomputeTotals();

                ps.setString(1, bill.getBillNo());
                ps.setInt(2, bill.getCustomerId());
                ps.setTimestamp(3, ts(bill.getIssuedAt()));
                ps.setTimestamp(4, ts(bill.getDueAt()));
                ps.setString(5, bill.getStatus() == null ? BillStatus.ISSUED.name() : bill.getStatus().name());
                ps.setBigDecimal(6, bill.getSubtotal());
                ps.setBigDecimal(7, bill.getDiscountAmount());
                ps.setBigDecimal(8, bill.getTaxAmount());
                ps.setBigDecimal(9, bill.getTotal());
                ps.executeUpdate();

                int billId;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No generated key for bill");
                    billId = keys.getInt(1);
                }
                bill.setId(billId);

                if (!bill.getLines().isEmpty()) {
                    try (PreparedStatement pl = conn.prepareStatement(INSERT_LINE, Statement.RETURN_GENERATED_KEYS)) {
                        for (BillLine l : bill.getLines()) {
                            l.computeTotals();
                            if (l.getQuantity() <= 0) continue; // extra guard

                            pl.setInt(1, billId);
                            if (l.getItemId() == null) pl.setNull(2, Types.INTEGER);
                            else pl.setInt(2, l.getItemId());
                            pl.setString(3, nz(l.getSku()));   // null -> ""
                            pl.setString(4, nz(l.getName()));  // null -> ""
                            pl.setInt(5, l.getQuantity());
                            pl.setBigDecimal(6, l.getUnitPrice());
                            pl.setBigDecimal(7, l.getLineDiscount());
                            pl.setBigDecimal(8, l.getLineTotal());
                            pl.executeUpdate();

                            try (ResultSet rkl = pl.getGeneratedKeys()) {
                                if (rkl.next()) l.setId(rkl.getInt(1));
                            }
                        }
                    }
                }

                conn.commit();
                return bill;

            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                ex.printStackTrace();
                return null;
            } finally {
                try { conn.setAutoCommit(oldAuto); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateBill(Bill bill) {
        if (bill == null) return false;
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_BILL)) {

            ps.setString(1, bill.getBillNo());
            ps.setInt(2, bill.getCustomerId());
            ps.setTimestamp(3, ts(bill.getIssuedAt()));
            ps.setTimestamp(4, ts(bill.getDueAt()));
            ps.setString(5, bill.getStatus() == null ? BillStatus.ISSUED.name() : bill.getStatus().name());
            ps.setBigDecimal(6, bill.getSubtotal());
            ps.setBigDecimal(7, bill.getDiscountAmount());
            ps.setBigDecimal(8, bill.getTaxAmount());
            ps.setBigDecimal(9, bill.getTotal());
            ps.setInt(10, bill.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBill(int id) {
        try (Connection conn = DBConnectionFactory.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement delLines = conn.prepareStatement(DELETE_LINES);
                 PreparedStatement delBill  = conn.prepareStatement(DELETE_BILL)) {

                delLines.setInt(1, id);
                delLines.executeUpdate();

                delBill.setInt(1, id);
                int n = delBill.executeUpdate();

                conn.commit();
                return n > 0;

            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                ex.printStackTrace();
                return false;
            } finally {
                try { conn.setAutoCommit(oldAuto); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Bill findById(int id) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BILL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Bill bill = mapBill(rs);

                try (PreparedStatement pl = conn.prepareStatement(SELECT_LINES)) {
                    pl.setInt(1, id);
                    try (ResultSet rl = pl.executeQuery()) {
                        while (rl.next()) bill.getLines().add(mapLine(rl));
                    }
                }

                bill.recomputeTotals(); // trust domain math
                return bill;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapBill(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Bill> findRecent(int limit) {
        List<Bill> list = new ArrayList<>();
        if (limit <= 0) return list;
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_RECENT)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBill(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Bill> findByCustomer(int customerId) {
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CUSTOMER)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBill(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Bill> findIssuedBetween(LocalDateTime from, LocalDateTime to) {
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ISSUED_BETWEEN)) {

            ps.setTimestamp(1, ts(from));
            ps.setTimestamp(2, ts(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBill(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int countIssuedBetween(LocalDateTime from, LocalDateTime to) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ISSUED_BETWEEN)) {

            ps.setTimestamp(1, ts(from));
            ps.setTimestamp(2, ts(to));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public BigDecimal sumTotalIssuedBetween(LocalDateTime from, LocalDateTime to) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SUM_TOTAL_ISSUED_BETWEEN)) {

            ps.setTimestamp(1, ts(from));
            ps.setTimestamp(2, ts(to));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    private static Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setBillNo(rs.getString("bill_no"));
        b.setCustomerId(rs.getInt("customer_id"));
        Timestamp issued = rs.getTimestamp("issued_at");
        Timestamp due    = rs.getTimestamp("due_at");
        b.setIssuedAt(issued == null ? null : issued.toLocalDateTime());
        b.setDueAt(due    == null ? null : due.toLocalDateTime());
        b.setStatus(BillStatus.valueOf(rs.getString("status")));
        b.setSubtotal(rs.getBigDecimal("subtotal"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setTaxAmount(rs.getBigDecimal("tax_amount"));
        b.setTotal(rs.getBigDecimal("total"));
        return b;
    }

    private static BillLine mapLine(ResultSet rs) throws SQLException {
        BillLine l = new BillLine();
        l.setId(rs.getInt("id"));
        int itemId = rs.getInt("item_id");
        l.setItemId(rs.wasNull() ? null : itemId);
        l.setSku(rs.getString("sku"));
        l.setName(rs.getString("name"));
        l.setQuantity(rs.getInt("quantity"));
        l.setUnitPrice(rs.getBigDecimal("unit_price"));
        l.setLineDiscount(rs.getBigDecimal("line_discount"));
        l.setLineTotal(rs.getBigDecimal("line_total"));
        return l;
    }

    private static Timestamp ts(java.time.LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
