package com.pahanaedu.pahanasuite.factories;

import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillStatus;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class BillFactoryTest {

    private static final Pattern INV_PATTERN =
            Pattern.compile("^INV-\\d{8}-\\d{6}-\\d{4}$"); // INV-YYYYMMDD-HHMMSS-####

    @Test
    void create_setsBillNoIssuedAndZeroesMoney() {
        Bill bill = BillFactory.create(123);

        assertEquals(123, bill.getCustomerId());
        assertEquals(BillStatus.ISSUED, bill.getStatus());
        assertNotNull(bill.getIssuedAt(), "IssuedAt should be stamped on creation");

        assertTrue(INV_PATTERN.matcher(bill.getBillNo()).matches(),
                "billNo must look like INV-YYYYMMDD-HHMMSS-####");

        // money starts at 0.00
        assertEquals(0, bill.getSubtotal().compareTo(new java.math.BigDecimal("0.00")));
        assertEquals(0, bill.getTotal().compareTo(new java.math.BigDecimal("0.00")));
    }

    @Test
    void create_generatesUniqueNumbers() {
        Bill a = BillFactory.create(1);
        Bill b = BillFactory.create(1);
        assertNotEquals(a.getBillNo(), b.getBillNo(), "Two consecutive bills must differ");
    }
}
