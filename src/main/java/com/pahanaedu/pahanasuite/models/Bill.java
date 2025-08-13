package com.pahanaedu.pahanasuite.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Bill {

    // Defaults
    private BillStatus status = BillStatus.ISSUED;

    // Money always stored at scale=2
    private BigDecimal subtotal       = nz(BigDecimal.ZERO);
    private BigDecimal discountAmount = nz(BigDecimal.ZERO);
    private BigDecimal taxAmount      = nz(BigDecimal.ZERO);
    private BigDecimal total          = nz(BigDecimal.ZERO);

    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;

    // --- getters/setters ---
    public BillStatus getStatus() { return status; }

    public void setStatus(BillStatus next) {
        // Terminal rule: once CANCELLED, no other transitions allowed
        if (this.status == BillStatus.CANCELLED && next != BillStatus.CANCELLED) {
            throw new IllegalStateException("Bill is CANCELLED and cannot change state");
        }
        // Null safety: treat null as ISSUED
        this.status = (next == null) ? BillStatus.ISSUED : next;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = nz(subtotal); }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = nz(discountAmount); }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = nz(taxAmount); }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = nz(total); }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    /** total = max(0.00, subtotal - discountAmount + taxAmount), normalized to 2 dp */
    public void recomputeTotals() {
        BigDecimal net = subtotal.subtract(discountAmount).add(taxAmount);
        if (net.signum() < 0) net = BigDecimal.ZERO;
        this.total = nz(net); // guarantees 0.00, not 0
    }

    /** Null-safe, enforce scale=2 across all money fields */
    private static BigDecimal nz(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }
}
