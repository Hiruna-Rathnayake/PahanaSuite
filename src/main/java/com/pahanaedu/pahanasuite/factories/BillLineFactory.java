package com.pahanaedu.pahanasuite.factories;

import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.models.Item;

public final class BillLineFactory {
    private BillLineFactory() {}

    public static BillLine from(Item item, int qty) {
        BillLine line = new BillLine();
        if (item != null) {
            line.setItemId(item.getId());
            line.setSku(item.getSku());
            line.setName(item.getName());
            line.setUnitPrice(item.getUnitPrice());
        }
        line.setQuantity(qty);  // clamps to >= 0
        line.computeTotals();
        return line;
    }
}
