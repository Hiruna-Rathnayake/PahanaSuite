// src/main/java/com/pahanaedu/pahanasuite/models/Bill.java
package com.pahanaedu.pahanasuite.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {

    private BillStatus status = BillStatus.ISSUED;

    // Money normalized to 2 dp
    private BigDecimal subtotal       = nz(BigDecimal.ZERO);
    private BigDecimal discountAmount = nz(BigDecimal.ZERO);
    private BigDecimal taxAmount      = nz(BigDecimal.ZERO);
    private BigDecimal total          = nz(BigDecimal.ZERO);

    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;

    // For factories/tests that expect these
    private String billNo;
    private int customerId;

    // --- NEW: line items are part of the bill aggregate ---
    private final List<BillLine> lines = new ArrayList<>();

    // --- getters/setters (existing + these two) ---
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public List<BillLine> getLines() { return lines; }

    /** Adds a line, keeping null safety, then recomputes totals. */
    public void addLine(BillLine line) {
        if (line == null) return;
        line.computeTotals();        // ensure the line has its own math up to date
        lines.add(line);
        recomputeTotals();           // keep header numbers consistent
    }

    /** Removes a line if present, then recomputes totals. */
    public void removeLine(BillLine line) {
        if (line == null) return;
        lines.remove(line);
        recomputeTotals();
    }

    // ... existing status/time getters/setters remain unchanged ...

    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus next) {
        if (this.status == BillStatus.CANCELLED && next != BillStatus.CANCELLED) {
            throw new IllegalStateException("Bill is CANCELLED and cannot change state");
        }
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

    /**
     * Recomputes header totals from current lines and invoice-level adjustments.
     * subtotal = Σ(lineTotal), total = max(0.00, subtotal - discount + tax).
     */
    public void recomputeTotals() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BillLine l : lines) {
            // defensive refresh, in case a caller changed qty/price without re-calc
            l.computeTotals();
            sum = sum.add(l.getLineTotal());
        }
        this.subtotal = nz(sum);

        BigDecimal net = subtotal.subtract(discountAmount).add(taxAmount);
        if (net.signum() < 0) net = BigDecimal.ZERO;
        this.total = nz(net);
    }

    private static BigDecimal nz(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }
}
