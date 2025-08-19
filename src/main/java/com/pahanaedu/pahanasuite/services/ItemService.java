package com.pahanaedu.pahanasuite.services;

import com.pahanaedu.pahanasuite.dao.ItemDAO;
import com.pahanaedu.pahanasuite.models.Item;

import java.math.BigDecimal;
import java.util.*;

public class ItemService {

    private final ItemDAO itemDAO;

    // Keep this small and grow as needed; sync with DB CHECK if you added one
    private static final Set<String> ALLOWED_CATEGORIES =
            new HashSet<>(Arrays.asList("BOOK", "STATIONERY", "GIFT", "OTHER"));

    public ItemService(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    // ---------- CREATE ----------
    /**
     * Creates a new item after validating inputs and SKU uniqueness.
     * Returns the created item (with id) or null on failure.
     */
    public Item create(String sku, String name, String category, String description,
                       BigDecimal unitPrice, int stockQty, Map<String, Object> attributes) {

        sku = trim(sku);
        name = trim(name);
        category = normalizeCategory(category);
        description = trim(description);

        if (isBlank(sku) || isBlank(name) || category == null) return null;
        if (!isValidPrice(unitPrice) || !isValidStock(stockQty)) return null;

        if (itemDAO.findBySku(sku) != null) return null; // enforce unique SKU

        Item it = new Item();
        it.setSku(sku);
        it.setName(name);
        it.setCategory(category);
        it.setDescription(description);
        it.setUnitPrice(unitPrice);
        it.setStockQty(stockQty);
        it.setAttributes(safeAttributes(attributes));

        return itemDAO.createItem(it);
    }

    // ---------- UPDATE ----------
    /**
     * Updates an existing item. Allows changing SKU if still unique.
     * If attributes is null, keeps existing attributes; if non-null, replaces them.
     * Returns updated item or null on failure.
     */
    public Item update(int id, String sku, String name, String category, String description,
                       BigDecimal unitPrice, int stockQty, Map<String, Object> attributes) {

        if (id <= 0) return null;

        sku = trim(sku);
        name = trim(name);
        category = normalizeCategory(category);
        description = trim(description);

        if (isBlank(sku) || isBlank(name) || category == null) return null;
        if (!isValidPrice(unitPrice) || !isValidStock(stockQty)) return null;

        Item existing = itemDAO.findById(id);
        if (existing == null) return null;

        // If SKU is changed, ensure uniqueness
        if (!sku.equals(existing.getSku())) {
            if (itemDAO.findBySku(sku) != null) return null;
        }

        existing.setSku(sku);
        existing.setName(name);
        existing.setCategory(category);
        existing.setDescription(description);
        existing.setUnitPrice(unitPrice);
        existing.setStockQty(stockQty);
        if (attributes != null) {
            existing.setAttributes(safeAttributes(attributes));
        }

        boolean ok = itemDAO.updateItem(existing);
        return ok ? existing : null;
    }

    // ---------- READ ----------
    public Item getById(int id) {
        if (id <= 0) return null;
        return itemDAO.findById(id);
    }

    public Item getBySku(String sku) {
        sku = trim(sku);
        if (isBlank(sku)) return null;
        return itemDAO.findBySku(sku);
    }

    public List<Item> search(String q, String category, int limit, int offset) {
        String cq = trim(q);
        String cat = normalizeCategoryLoose(category); // allow null/empty => no filter
        if (limit <= 0) limit = 50;
        if (offset < 0) offset = 0;
        return itemDAO.search(cq, cat, limit, offset);
    }

    /** Convenience: list first page of all items. */
    public List<Item> listAll() {
        return itemDAO.search(null, null, 1000, 0);
    }

    // ---------- DELETE ----------
    public boolean delete(int id) {
        if (id <= 0) return false;
        return itemDAO.deleteItem(id);
    }

    // ---------- STOCK ----------
    /**
     * Adjust stock by delta (positive to add, negative to consume).
     * Returns true if applied; false if it would go negative or row missing.
     */
    public boolean adjustStock(int id, int delta) {
        if (id <= 0 || delta == 0) return false;
        return itemDAO.adjustStock(id, delta);
    }

    /** Returns count of items with stock below threshold. */
    public int countLowStock(int threshold) {
        return itemDAO.countLowStock(threshold);
    }

    // ---------- helpers ----------
    private static boolean isValidPrice(BigDecimal p) {
        return p != null && p.compareTo(BigDecimal.ZERO) >= 0;
    }

    private static boolean isValidStock(int s) {
        return s >= 0;
    }

    private static String normalizeCategory(String c) {
        if (c == null) return null;
        String up = c.trim().toUpperCase(Locale.ROOT);
        return ALLOWED_CATEGORIES.contains(up) ? up : null;
    }

    private static String normalizeCategoryLoose(String c) {
        if (c == null || c.trim().isEmpty()) return null;
        String up = c.trim().toUpperCase(Locale.ROOT);
        return ALLOWED_CATEGORIES.contains(up) ? up : null; // else ignore bad filter
    }

    private static Map<String, Object> safeAttributes(Map<String, Object> attrs) {
        if (attrs == null) return new HashMap<>();
        // Shallow copy to avoid caller mutation
        return new HashMap<>(attrs);
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
