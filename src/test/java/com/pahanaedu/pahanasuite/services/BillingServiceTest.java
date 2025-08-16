//package com.pahanaedu.pahanasuite.services;
//
//import com.pahanaedu.pahanasuite.factories.BillFactory;
//import com.pahanaedu.pahanasuite.factories.BillLineFactory;
//import com.pahanaedu.pahanasuite.models.*;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.regex.Pattern;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BillingServiceTest {
//
//    private static BigDecimal bd(String v) { return new BigDecimal(v); }
//    private static final Pattern INV_PATTERN =
//            Pattern.compile("^INV-\\d{8}-\\d{6}-\\d{4}$"); // INV-YYYYMMDD-HHMMSS-####
//
//    @Test
//    void startBill_setsNumberStatusIssuedAndZeroes() {
//        BillingService svc = new BillingService();
//        Bill bill = svc.startBill(321); // internally uses BillFactory
//
//        assertEquals(321, bill.getCustomerId());
//        assertEquals(BillStatus.ISSUED, bill.getStatus());
//        assertNotNull(bill.getIssuedAt());
//        assertTrue(INV_PATTERN.matcher(bill.getBillNo()).matches());
//        assertEquals(0, bill.getTotal().compareTo(bd("0.00")));
//    }
//
//    @Test
//    void addLineFromItem_updatesTotals() {
//        BillingService svc = new BillingService();
//        Bill bill = svc.startBill(1);
//
//        Item book = new Item();
//        book.setId(7);
//        book.setSku("BK-007");
//        book.setName("Clean Architecture");
//        book.setUnitPrice(bd("1500.00"));
//
//        svc.addLineFromItem(bill, book, 2); // 3000
//        assertEquals(bd("3000.00"), bill.getSubtotal());
//        assertEquals(bd("3000.00"), bill.getTotal());
//
//        // add another
//        Item pen = new Item();
//        pen.setId(2);
//        pen.setSku("ST-200");
//        pen.setName("Pen");
//        pen.setUnitPrice(bd("100.00"));
//
//        svc.addLineFromItem(bill, pen, 3); // +300
//        assertEquals(bd("3300.00"), bill.getSubtotal());
//        assertEquals(bd("3300.00"), bill.getTotal());
//    }
//
//    @Test
//    void applyAdjustments_affectsTotal_andClampsAtZero() {
//        BillingService svc = new BillingService();
//        Bill bill = svc.startBill(1);
//
//        Item i = new Item();
//        i.setSku("X"); i.setName("Y"); i.setUnitPrice(bd("500.00"));
//        svc.addLineFromItem(bill, i, 1); // subtotal 500
//
//        // tax-inclusive: total = subtotal - discount (taxAmount informational only)
//        svc.applyAdjustments(bill, bd("100.00"), bd("50.00"));
//        assertEquals(bd("500.00"), bill.getSubtotal());
//        assertEquals(bd("400.00"), bill.getTotal()); // 500 - 100 = 400
//
//        // clamp at zero when discount exceeds subtotal
//        svc.applyAdjustments(bill, bd("1000.00"), bd("0.00"));
//        assertEquals(bd("0.00"), bill.getTotal());
//    }
//
//    @Test
//    void cancelBill_setsCancelled_andFurtherChangesAreBlocked() {
//        BillingService svc = new BillingService();
//        Bill bill = svc.startBill(1);
//        svc.cancelBill(bill);
//        assertEquals(BillStatus.CANCELLED, bill.getStatus());
//
//        // trying to change after CANCELLED should explode (enforced by Bill)
//        IllegalStateException ex = assertThrows(IllegalStateException.class,
//                () -> bill.setStatus(BillStatus.PAID));
//        assertTrue(ex.getMessage().toLowerCase().contains("cancel"));
//    }
//}
