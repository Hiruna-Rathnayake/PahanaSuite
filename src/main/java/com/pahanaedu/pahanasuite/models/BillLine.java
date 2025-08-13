// File: BillLine.java
package com.pahanaedu.pahanasuite.models;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Minimal stub for TDD. No math, no null-safety yet.
 * Tests will compile but should fail on assertions.
 */
public class BillLine {

    private int id;
    private int billId;
    private Integer itemId;

    private String sku;
    private String name;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineDiscount;
    private BigDecimal lineTotal;

    private Map<String, Object> attributes; // left null for now

    // --- getters/setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineDiscount() { return lineDiscount; }
    public void setLineDiscount(BigDecimal lineDiscount) { this.lineDiscount = lineDiscount; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    /** Stub: no calculation yet (keeps tests red). */
    public void computeTotals() {
        // no-op for now
    }
}
