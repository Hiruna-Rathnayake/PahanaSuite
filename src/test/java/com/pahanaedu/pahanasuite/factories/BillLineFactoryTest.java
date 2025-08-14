package com.pahanaedu.pahanasuite.factories;

import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.models.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillLineFactoryTest {

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void from_snapshotsItemAndComputesTotals() {
        Item item = new Item();
        item.setId(42);
        item.setSku("BK-001");
        item.setName("Clean Code");
        item.setUnitPrice(bd("1500.00"));

        BillLine line = BillLineFactory.from(item, 2);

        assertEquals("BK-001", line.getSku());
        assertEquals("Clean Code", line.getName());
        assertEquals(bd("1500.00"), line.getUnitPrice());
        assertEquals(2, line.getQuantity());
        assertEquals(bd("3000.00"), line.getLineTotal());
        assertEquals(42, line.getItemId(), "We keep a soft link to the catalog item id");
    }

    @Test
    void from_isSnapshot_notLiveLinkedToItem() {
        Item item = new Item();
        item.setSku("BK-777");
        item.setName("DDD");
        item.setUnitPrice(bd("2000.00"));

        BillLine line = BillLineFactory.from(item, 1);
        // mutate item later
        item.setUnitPrice(bd("50.00"));
        item.setName("DDD 2nd");

        assertEquals("BK-777", line.getSku());
        assertEquals("DDD", line.getName());
        assertEquals(bd("2000.00"), line.getUnitPrice(), "Price snapshot must not change after item edits");
        assertEquals(bd("2000.00"), line.getLineTotal());
    }

    @Test
    void from_clampsNegativeQtyToZero() {
        Item item = new Item();
        item.setSku("X");
        item.setName("Y");
        item.setUnitPrice(bd("100.00"));

        BillLine line = BillLineFactory.from(item, -5);

        assertEquals(0, line.getQuantity());
        assertEquals(bd("0.00"), line.getLineTotal());
    }
}
