package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.Bill;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

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

    /**
     * Returns the most recently issued bills ordered by {@code issued_at DESC}.
     *
     * @param limit maximum number of bills to return
     */
    List<Bill> findRecent(int limit);

    /** Returns bill headers for a given customer. */
    List<Bill> findByCustomer(int customerId);

    /** Returns bill headers issued between [from, to). */
    List<Bill> findIssuedBetween(LocalDateTime from, LocalDateTime to);

    /** Counts bills issued between [from, to). */
    int countIssuedBetween(LocalDateTime from, LocalDateTime to);

    /** Sums total field of bills issued between [from, to). */
    BigDecimal sumTotalIssuedBetween(LocalDateTime from, LocalDateTime to);
}
