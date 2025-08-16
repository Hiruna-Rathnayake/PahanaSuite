package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.BillDAO;
import com.pahanaedu.pahanasuite.factories.BillFactory;
import com.pahanaedu.pahanasuite.factories.BillLineFactory;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillStatus;
import com.pahanaedu.pahanasuite.models.Item;

import java.math.BigDecimal;
import java.util.List;

/**
 * Minimal service for assignment use:
 * - Starts bills with factory
 * - Adds lines from items
 * - Applies simple adjustments
 * - Persists via BillDAO
 */
public class BillingService {

    private final BillDAO billDAO;

    public BillingService(BillDAO billDAO) {
        this.billDAO = billDAO;
    }

    /** Start a new bill for a customer. */
    public Bill startBill(int customerId) {
        return BillFactory.create(customerId);
    }

    /** Add a line from an Item (snapshot). */
    public void addLineFromItem(Bill bill, Item item, int qty) {
        if (bill == null || item == null || qty <= 0) return;
        bill.addLine(BillLineFactory.from(item, qty));
        bill.recomputeTotals();
    }

    /** Apply invoice-level discount and tax (tax is informational). */
    public void applyAdjustments(Bill bill, BigDecimal discount, BigDecimal tax) {
        if (bill == null) return;
        bill.setDiscountAmount(discount == null ? BigDecimal.ZERO : discount);
        bill.setTaxAmount(tax == null ? BigDecimal.ZERO : tax);
        bill.recomputeTotals();
    }

    /** Mark bill as cancelled (domain guard prevents reactivation). */
    public void cancelBill(Bill bill) {
        if (bill == null) return;
        bill.setStatus(BillStatus.CANCELLED);
    }

    // --- Persistence convenience methods ---

    /** Persist a new bill with its lines. Returns bill with id set, or null on failure. */
    public Bill save(Bill bill) {
        if (bill == null) return null;
        bill.recomputeTotals();
        return billDAO.createBill(bill);
    }

    /** Update header-only fields of an existing bill. */
    public boolean updateHeader(Bill bill) {
        if (bill == null || bill.getId() <= 0) return false;
        bill.recomputeTotals();
        return billDAO.updateBill(bill);
    }

    /** Load a full bill (header + lines). */
    public Bill load(int id) {
        if (id <= 0) return null;
        return billDAO.findById(id);
    }

    /** List headers (no lines). */
    public List<Bill> listAll() {
        return billDAO.findAll();
    }

    /** Delete bill and its lines. */
    public boolean delete(int id) {
        if (id <= 0) return false;
        return billDAO.deleteBill(id);
    }
}
