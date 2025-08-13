package com.pahanaedu.pahanasuite.models;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BillTest {

    // Helper for BigDecimal values
    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void newBill_defaults() {
        // New bill should have ISSUED status and zero amounts
        Bill bill = new Bill();
        assertEquals(BillStatus.ISSUED, bill.getStatus(), "New bill must be ISSUED");
        assertEquals(bd("0.00"), bill.getTotal(), "Total should be 0.00");
    }

    @Test
    void cancelledIsTerminal_noStatusChange() {
        // Cannot change status after CANCELLED
        Bill bill = new Bill();
        bill.setStatus(BillStatus.CANCELLED);
        assertThrows(IllegalStateException.class,
                () -> bill.setStatus(BillStatus.PAID),
                "Should prevent status change from CANCELLED");
    }

    @Test
    void recomputeTotals_noLines_zerosOut() {
        // With no lines, total should be zero even with discount/tax
        Bill bill = new Bill();
        bill.setDiscountAmount(bd("50.00"));
        bill.setTaxAmount(bd("10.00"));
        bill.recomputeTotals();
        assertEquals(bd("0.00"), bill.getTotal(), "Total should be 0.00 with no lines");
    }
}