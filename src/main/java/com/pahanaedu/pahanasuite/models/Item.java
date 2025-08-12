package com.pahanaedu.pahanasuite.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain model for catalog items.
 * - attributes: stored as JSON in MySQL (serialize/deserialize in DAO).
 */
public class Item {
    private int id;
    private String sku;
    private String name;
    private String category;        // BOOK, STATIONERY, GIFT, ...
    private String description;
    private BigDecimal unitPrice;   // DECIMAL(10,2)
    private int stockQty;

    // JSON column <-> Map (never null)
    private Map<String, Object> attributes = new HashMap<>();

    // Optional: mirrors DB timestamps (set in DAO if you read them)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- ctors ---
    public Item() {}

    public Item(String sku, String name, String category, String description,
                BigDecimal unitPrice, int stockQty) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.description = description;
        this.unitPrice = unitPrice;
        this.stockQty = stockQty;
    }

    public Item(int id, String sku, String name, String category, String description,
                BigDecimal unitPrice, int stockQty) {
        this(sku, name, category, description, unitPrice, stockQty);
        this.id = id;
    }

    // --- getters/setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = (attributes == null) ? new HashMap<>() : attributes;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- attribute helpers ---
    public String getAttrString(String key) {
        Object v = attributes.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    public Integer getAttrInt(String key) {
        Object v = attributes.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception ignore) { return null; }
    }

    public BigDecimal getAttrDecimal(String key) {
        Object v = attributes.get(key);
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(v.toString()); } catch (Exception ignore) { return null; }
    }

    public void setAttr(String key, Object value) {
        if (key == null) return;
        if (value == null) attributes.remove(key);
        else attributes.put(key, value);
    }

    // --- identity/diagnostics ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        // Prefer id when present; fall back to sku uniqueness
        if (id != 0 && item.id != 0) return id == item.id;
        return Objects.equals(sku, item.sku);
    }

    @Override
    public int hashCode() {
        return (id != 0) ? Integer.hashCode(id) : Objects.hashCode(sku);
    }

    @Override
    public String toString() {
        return "Item{id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", unitPrice=" + unitPrice +
                ", stockQty=" + stockQty +
                '}';
    }
}
