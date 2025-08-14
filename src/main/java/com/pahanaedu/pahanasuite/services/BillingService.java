package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.factories.BillFactory;
import com.pahanaedu.pahanasuite.factories.BillLineFactory;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillStatus;
import com.pahanaedu.pahanasuite.models.Item;

import java.math.BigDecimal;

public class BillingService {

    /** Starts a new bill using the app’s BillFactory. */
    public Bill startBill(int customerId) {
        return BillFactory.create(customerId);
    }

    /** Adds a line from an Item snapshot then recomputes totals. */
    public void addLineFromItem(Bill bill, Item item, int qty) {
        bill.addLine(BillLineFactory.from(item, qty));
        bill.recomputeTotals();
    }

    /** Applies invoice-level discount and tax, then recomputes totals. */
    public void applyAdjustments(Bill bill, BigDecimal discount, BigDecimal tax) {
        bill.setDiscountAmount(discount);
        bill.setTaxAmount(tax);
        bill.recomputeTotals();
    }

    /** Cancels the bill; future status changes are blocked by Bill. */
    public void cancelBill(Bill bill) {
        bill.setStatus(BillStatus.CANCELLED);
    }
}
