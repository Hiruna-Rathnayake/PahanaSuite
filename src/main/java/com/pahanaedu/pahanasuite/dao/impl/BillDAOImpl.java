package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.models.BillStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAOImpl implements BillDAO {

    // --- SQL ---
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

    private static final String UPDATE_BILL = """
        UPDATE bills
           SET bill_no=?, customer_id=?, issued_at=?, due_at=?, status=?,
               subtotal=?, discount_amount=?, tax_amount=?, total=?
         WHERE id=?
        """;

    private static final String DELETE_LINES = "DELETE FROM bill_lines WHERE bill_id = ?";
    private static final String DELETE_BILL  = "DELETE FROM bills WHERE id = ?";

    // --- Create (header + lines in one TX) ---
    @Override
    public Bill createBill(Bill bill) {
        try (Connection conn = DBConnectionFactory.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(INSERT_BILL, Statement.RETURN_GENERATED_KEYS)) {
                // ensure header totals reflect current lines & adjustments
                bill.recomputeTotals();

                ps.setString(1, bill.getBillNo());
                ps.setInt(2, bill.getCustomerId());
                ps.setTimestamp(3, ts(bill.getIssuedAt()));
                ps.setTimestamp(4, ts(bill.getDueAt()));
                ps.setString(5, bill.getStatus().name());
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

                try (PreparedStatement pl = conn.prepareStatement(INSERT_LINE, Statement.RETURN_GENERATED_KEYS)) {
                    for (BillLine l : bill.getLines()) {
                        // keep each line’s computed totals fresh
                        l.computeTotals();

                        pl.setInt(1, billId);
                        if (l.getItemId() == null) pl.setNull(2, Types.INTEGER);
                        else pl.setInt(2, l.getItemId());
                        pl.setString(3, l.getSku());
                        pl.setString(4, l.getName());
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

                conn.commit();
                conn.setAutoCommit(oldAuto);
                return bill;

            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // failure
    }

    // --- Update header only (lines are not touched) ---
    @Override
    public boolean updateBill(Bill bill) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_BILL)) {

            // caller should have recomputed totals if lines/adjustments changed
            ps.setString(1, bill.getBillNo());
            ps.setInt(2, bill.getCustomerId());
            ps.setTimestamp(3, ts(bill.getIssuedAt()));
            ps.setTimestamp(4, ts(bill.getDueAt()));
            ps.setString(5, bill.getStatus().name());
            ps.setBigDecimal(6, bill.getSubtotal());
            ps.setBigDecimal(7, bill.getDiscountAmount());
            ps.setBigDecimal(8, bill.getTaxAmount());
            ps.setBigDecimal(9, bill.getTotal());
            ps.setInt(10, bill.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Delete bill + lines ---
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
                conn.setAutoCommit(oldAuto);
                return n > 0;

            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Load header + lines ---
    @Override
    public Bill findById(int id) {
        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BILL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Bill bill = mapBill(rs);

                // load lines
                try (PreparedStatement pl = conn.prepareStatement(SELECT_LINES)) {
                    pl.setInt(1, id);
                    try (ResultSet rl = pl.executeQuery()) {
                        while (rl.next()) bill.getLines().add(mapLine(rl));
                    }
                }

                // defensive recompute for consistency with domain rules
                bill.recomputeTotals();
                return bill;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- List headers only ---
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

    // --- Mappers (keep password-like data out; not applicable here) ---
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
}
