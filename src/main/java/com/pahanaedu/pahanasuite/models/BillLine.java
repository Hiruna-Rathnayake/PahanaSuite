package com.pahanaedu.pahanasuite.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class BillLine {

    private int id;
    private int billId;
    private Integer itemId;

    private String sku;
    private String name;

    private int quantity; // clamped >= 0
    private BigDecimal unitPrice    = nz(BigDecimal.ZERO);
    private BigDecimal lineDiscount = nz(BigDecimal.ZERO);
    private BigDecimal lineTotal    = nz(BigDecimal.ZERO);

    private Map<String, Object> attributes = new HashMap<>(); // never null

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
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = nz(unitPrice); }

    public BigDecimal getLineDiscount() { return lineDiscount; }
    public void setLineDiscount(BigDecimal lineDiscount) { this.lineDiscount = nz(lineDiscount); }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = nz(lineTotal); }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = (attributes == null) ? new HashMap<>() : attributes;
    }

    /** lineTotal = max(0.00, quantity * unitPrice - lineDiscount), normalized to 2 dp */
    public void computeTotals() {
        BigDecimal gross = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal net = gross.subtract(lineDiscount);
        if (net.signum() < 0) net = BigDecimal.ZERO;
        this.lineTotal = nz(net);
    }

    private static BigDecimal nz(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }
}
