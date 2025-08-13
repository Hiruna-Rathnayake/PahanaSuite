package com.pahanaedu.pahanasuite.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Failing-first tests for BillLine.
 * Focus: line math, clamping, and null-safety of money/attributes.
 */
class BillLineTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void computeTotals_basic() {
        // total = qty * unitPrice - lineDiscount
        BillLine line = new BillLine();
        line.setQuantity(3);
        line.setUnitPrice(bd("250.00"));
        line.setLineDiscount(bd("50.00"));

        line.computeTotals();

        assertEquals(bd("700.00"), line.getLineTotal(),
                "3*250 - 50 should compute to 700.00");
    }

    @Test
    void computeTotals_clampsNegativeToZero() {
        // Negative totals must clamp to 0.00
        BillLine line = new BillLine();
        line.setQuantity(1);
        line.setUnitPrice(bd("100.00"));
        line.setLineDiscount(bd("150.00"));

        line.computeTotals();

        assertEquals(bd("0.00"), line.getLineTotal(), "Line total must not be negative");
    }

    @Test
    void setters_areNullSafeForMoneyAndAttributes() {
        // Money setters accept null -> treated as zero. Attributes map never stays null.
        BillLine line = new BillLine();
        line.setUnitPrice(null);
        line.setLineDiscount(null);
        line.setLineTotal(null);
        line.setAttributes(null);

        assertEquals(bd("0"), line.getUnitPrice(), "Unit price should default to 0");
        assertEquals(bd("0"), line.getLineDiscount(), "Line discount should default to 0");
        assertEquals(bd("0"), line.getLineTotal(), "Line total should default to 0");
        assertNotNull(line.getAttributes(), "Attributes map should be non-null");
        assertTrue(line.getAttributes().isEmpty(), "Attributes should start empty");
    }

    @Test
    void quantity_cannotBeNegative() {
        // Defensive rule: negative quantity is clamped to zero
        BillLine line = new BillLine();
        line.setQuantity(-5);
        assertEquals(0, line.getQuantity(), "Quantity should clamp to >= 0");
    }

    @Test
    void attributes_roundTrip() {
        // Attributes map should store and retrieve values normally
        BillLine line = new BillLine();
        var attrs = new HashMap<String, Object>();
        attrs.put("edition", "2nd");
        attrs.put("color", "Blue");
        line.setAttributes(attrs);

        assertEquals(2, line.getAttributes().size());
        assertEquals("2nd", line.getAttributes().get("edition"));
    }
}
