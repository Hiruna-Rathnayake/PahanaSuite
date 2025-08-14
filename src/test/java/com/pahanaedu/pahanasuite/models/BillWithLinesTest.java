package com.pahanaedu.pahanasuite.models;

import com.pahanaedu.pahanasuite.factories.BillLineFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillWithLinesTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void addingLines_updatesSubtotal_andTotalWithAdjustments() {
        // Arrange: a fresh bill and two lines
        Bill bill = new Bill();

        Item book = new Item();
        book.setId(1);
        book.setSku("BK-100");
        book.setName("Algorithms");
        book.setUnitPrice(bd("1000.00"));

        Item pen = new Item();
        pen.setId(2);
        pen.setSku("ST-200");
        pen.setName("Pen");
        pen.setUnitPrice(bd("500.00"));

        BillLine l1 = BillLineFactory.from(book, 2); // 2 * 1000 = 2000
        BillLine l2 = BillLineFactory.from(pen, 1);  // 1 * 500  = 500

        // Act: add lines and apply invoice-level adjustments
        bill.addLine(l1);
        bill.addLine(l2);
        bill.setDiscountAmount(bd("100.00")); // invoice-level discount
        bill.setTaxAmount(bd("50.00"));       // invoice-level tax
        bill.recomputeTotals();

        // Assert: subtotal = 2000 + 500 = 2500. total = 2500 - 100 + 50 = 2450
        assertEquals(bd("2500.00"), bill.getSubtotal());
        assertEquals(bd("2450.00"), bill.getTotal());
    }

    @Test
    void removingLine_updatesSubtotalAndTotal() {
        Bill bill = new Bill();

        Item item = new Item();
        item.setId(7);
        item.setSku("X");
        item.setName("Y");
        item.setUnitPrice(bd("200.00"));

        BillLine a = BillLineFactory.from(item, 3); // 600
        BillLine b = BillLineFactory.from(item, 2); // 400

        bill.addLine(a);
        bill.addLine(b);
        bill.recomputeTotals();
        assertEquals(bd("1000.00"), bill.getSubtotal());

        // Remove one line and recompute
        bill.removeLine(a);
        bill.recomputeTotals();
        assertEquals(bd("400.00"), bill.getSubtotal());
        assertEquals(bd("400.00"), bill.getTotal());
    }
}
