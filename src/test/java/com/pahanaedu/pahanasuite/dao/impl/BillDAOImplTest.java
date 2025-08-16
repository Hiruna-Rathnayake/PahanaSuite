package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.models.BillStatus;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BillDAOImplTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void findById_returnsBillWithLines() throws Exception {
        // Mocks
        Connection conn = mock(Connection.class);
        PreparedStatement psBill  = mock(PreparedStatement.class);
        PreparedStatement psLines = mock(PreparedStatement.class);
        ResultSet rsBill  = mock(ResultSet.class);
        ResultSet rsLines = mock(ResultSet.class);

        // DAO prepares two statements in this order: bill header, then lines
        when(conn.prepareStatement(anyString())).thenReturn(psBill, psLines);

        // Bill row (header)
        when(psBill.executeQuery()).thenReturn(rsBill);
        when(rsBill.next()).thenReturn(true);
        when(rsBill.getInt("id")).thenReturn(5);
        when(rsBill.getString("bill_no")).thenReturn("INV-20250814-120000-0001");
        when(rsBill.getInt("customer_id")).thenReturn(99);
        when(rsBill.getTimestamp("issued_at")).thenReturn(Timestamp.valueOf(java.time.LocalDateTime.now()));
        when(rsBill.getTimestamp("due_at")).thenReturn(null);
        when(rsBill.getString("status")).thenReturn(BillStatus.ISSUED.name());
        // header money (DB snapshot)
        when(rsBill.getBigDecimal("subtotal")).thenReturn(bd("2150.00"));
        when(rsBill.getBigDecimal("discount_amount")).thenReturn(bd("100.00"));
        when(rsBill.getBigDecimal("tax_amount")).thenReturn(bd("50.00"));
        when(rsBill.getBigDecimal("total")).thenReturn(bd("2050.00")); // tax-inclusive total

        // Lines (2 rows): 2000 + 150 = 2150
        when(psLines.executeQuery()).thenReturn(rsLines);
        when(rsLines.next()).thenReturn(true, true, false);
        when(rsLines.getInt("id")).thenReturn(21, 22);
        when(rsLines.getInt("item_id")).thenReturn(1, 2);
        when(rsLines.wasNull()).thenReturn(false, false);
        when(rsLines.getString("sku")).thenReturn("BK-001", "ST-010");
        when(rsLines.getString("name")).thenReturn("Algorithms", "Pen");
        when(rsLines.getInt("quantity")).thenReturn(2, 3);
        when(rsLines.getBigDecimal("unit_price")).thenReturn(bd("1000.00"), bd("50.00"));
        when(rsLines.getBigDecimal("line_discount")).thenReturn(bd("0.00"), bd("0.00"));
        when(rsLines.getBigDecimal("line_total")).thenReturn(bd("2000.00"), bd("150.00"));

        try (MockedStatic<DBConnectionFactory> mocked = mockStatic(DBConnectionFactory.class)) {
            mocked.when(DBConnectionFactory::getConnection).thenReturn(conn);

            BillDAOImpl dao = new BillDAOImpl();
            Bill bill = dao.findById(5);

            assertNotNull(bill);
            assertEquals(5, bill.getId());
            assertEquals("INV-20250814-120000-0001", bill.getBillNo());
            assertEquals(99, bill.getCustomerId());

            // subtotal derived from lines = 2150.00
            assertEquals(bd("2150.00"), bill.getSubtotal());
            // tax-inclusive: total = subtotal - discount = 2150 - 100 = 2050.00
            assertEquals(bd("2050.00"), bill.getTotal());

            assertEquals(2, bill.getLines().size());
            assertEquals("BK-001", bill.getLines().get(0).getSku());
            assertEquals(bd("2000.00"), bill.getLines().get(0).getLineTotal());

            verify(conn, times(2)).prepareStatement(anyString());
            verify(psBill).setInt(eq(1), eq(5));   // WHERE id = ?
            verify(psLines).setInt(eq(1), eq(5));  // WHERE bill_id = ?
        }
    }

    @Test
    void createBill_insertsHeaderAndLines_setsIds() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.getAutoCommit()).thenReturn(true); // original autocommit state

        PreparedStatement psInsertBill  = mock(PreparedStatement.class);
        PreparedStatement psInsertLine  = mock(PreparedStatement.class);
        ResultSet keysBill  = mock(ResultSet.class);
        ResultSet keysLines = mock(ResultSet.class);

        // DAO prepares (in this order) INSERT header, then INSERT line; both with RETURN_GENERATED_KEYS
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(psInsertBill, psInsertLine);

        // header insert returns 1 row + key 101
        when(psInsertBill.executeUpdate()).thenReturn(1);
        when(psInsertBill.getGeneratedKeys()).thenReturn(keysBill);
        when(keysBill.next()).thenReturn(true);
        when(keysBill.getInt(1)).thenReturn(101);

        // two line inserts each return 1 row + keys 201, 202
        when(psInsertLine.executeUpdate()).thenReturn(1);
        when(psInsertLine.getGeneratedKeys()).thenReturn(keysLines);
        when(keysLines.next()).thenReturn(true, true, false);
        when(keysLines.getInt(1)).thenReturn(201, 202);

        try (MockedStatic<DBConnectionFactory> mocked = mockStatic(DBConnectionFactory.class)) {
            mocked.when(DBConnectionFactory::getConnection).thenReturn(conn);

            // Build bill like production code
            Bill bill = new Bill();
            bill.setBillNo("INV-20250814-120500-0002");
            bill.setCustomerId(77);
            bill.setStatus(BillStatus.ISSUED);

            BillLine l1 = new BillLine();
            l1.setItemId(1); l1.setSku("BK-001"); l1.setName("Algorithms");
            l1.setQuantity(2); l1.setUnitPrice(bd("1000.00")); l1.computeTotals(); // 2000

            BillLine l2 = new BillLine();
            l2.setItemId(2); l2.setSku("ST-010"); l2.setName("Pen");
            l2.setQuantity(3); l2.setUnitPrice(bd("50.00")); l2.computeTotals();   // 150

            bill.addLine(l1);
            bill.addLine(l2);
            bill.setDiscountAmount(bd("100.00"));
            bill.setTaxAmount(bd("50.00")); // informational in tax-inclusive flow
            bill.recomputeTotals(); // subtotal 2150, total 2050 (tax-inclusive)

            BillDAOImpl dao = new BillDAOImpl();
            Bill created = dao.createBill(bill);

            assertNotNull(created);
            assertEquals(101, created.getId());
            assertEquals(201, created.getLines().get(0).getId());
            assertEquals(202, created.getLines().get(1).getId());

            // header numbers persisted/reloaded as passed in
            assertEquals(bd("2150.00"), created.getSubtotal());
            assertEquals(bd("2050.00"), created.getTotal());

            verify(conn).setAutoCommit(false);
            verify(psInsertBill).setString(eq(1), eq("INV-20250814-120500-0002"));
            verify(psInsertBill).setInt(eq(2), eq(77));
            verify(psInsertBill).setString(eq(5), eq(BillStatus.ISSUED.name()));
            verify(conn).commit();
            verify(conn).setAutoCommit(true);
        }
    }
}
