package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.Bill;

import java.util.List;

public interface BillDAO {

    /** Inserts bill header + lines in a single transaction. Returns the bill with id set. */
    Bill createBill(Bill bill);

    /** Updates only the bill header fields (not lines). */
    boolean updateBill(Bill bill);

    /** Deletes bill and its lines. */
    boolean deleteBill(int id);

    /** Loads bill header + lines, or null if not found. */
    Bill findById(int id);

    /** Returns bill headers only (no lines) for simple listings. */
    List<Bill> findAll();
}
