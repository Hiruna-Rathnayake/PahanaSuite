// File: Bill.java
package com.pahanaedu.pahanasuite.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Minimal stub for TDD. No defaults or logic yet.
 * Tests will compile but assertions should fail.
 */
public class Bill {

    // Intentionally no defaults set here (TDD red)
    private BillStatus status;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;

    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;

    // --- getters/setters ---
    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    /** Stub: no calculation yet (keeps tests red). */
    public void recomputeTotals() {
        // no-op for now
    }
}
