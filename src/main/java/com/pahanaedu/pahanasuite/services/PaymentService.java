package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.PaymentDAO;
import com.pahanaedu.pahanasuite.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentService {
    private final PaymentDAO dao;
    public PaymentService(PaymentDAO dao) { this.dao = dao; }

    /** Record a payment; positive for normal payments. */
    public Payment pay(int billId, BigDecimal amount, String method, String ref) {
        return record(billId, amount, method == null ? "CASH" : method, ref);
    }

    /** Record a refund as a negative payment row. */
    public Payment refund(int billId, BigDecimal amount, String ref) {
        if (amount == null) return null;
        // ensure negative
        BigDecimal neg = amount.signum() > 0 ? amount.negate() : amount;
        return record(billId, neg, "REFUND", ref);
    }

    /** Sum of all payments (refunds included as negatives). */
    public BigDecimal totalPaid(int billId) {
        return dao.sumByBillId(billId);
    }

    /** Remaining balance for a bill (total minus payments). */
    public BigDecimal remainingBalance(int billId, BigDecimal billTotal) {
        BigDecimal diff = dao.outstandingAmount(billId);
        if (diff == null) {
            return billTotal == null ? BigDecimal.ZERO.setScale(2) : billTotal.setScale(2);
        }
        return diff.negate();
    }

    private Payment record(int billId, BigDecimal amount, String method, String ref) {
        if (billId <= 0) return null;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return null;

        Payment p = new Payment();
        p.setBillId(billId);
        p.setAmount(amount);
        p.setMethod(method == null ? "CASH" : method.trim());
        p.setReference(ref == null ? null : ref.trim());
        p.setPaidAt(LocalDateTime.now());
        return dao.create(p);
    }
}
