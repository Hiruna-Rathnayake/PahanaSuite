// File: src/main/java/com/pahanaedu/pahanasuite/dao/impl/ItemDAOImpl.java
package com.pahanaedu.pahanasuite.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.dao.ItemDAO;
import com.pahanaedu.pahanasuite.models.Item;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ItemDAOImpl implements ItemDAO {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private static final String BASE_COLS =
            "id, sku, name, category, description, unit_price, stock_qty, attributes, created_at, updated_at";

    private static final String SELECT_BY_ID =
            "SELECT " + BASE_COLS + " FROM items WHERE id = ?";
    private static final String SELECT_BY_SKU =
            "SELECT " + BASE_COLS + " FROM items WHERE sku = ?";

    private static final String INSERT_ITEM =
            "INSERT INTO items (sku, name, category, description, unit_price, stock_qty, attributes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_ITEM =
            "UPDATE items SET sku=?, name=?, category=?, description=?, unit_price=?, stock_qty=?, attributes=? " +
                    "WHERE id=?";

    private static final String DELETE_ITEM =
            "DELETE FROM items WHERE id=?";
    private static final String COUNT_LOW_STOCK =
            "SELECT COUNT(*) FROM items WHERE stock_qty < ?";

    // MySQL atomic stock update, prevents going negative
    private static final String ADJUST_STOCK =
            "UPDATE items SET stock_qty = stock_qty + ? WHERE id = ? AND stock_qty + ? >= 0";

    @Override
    public Item findById(int id) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Item findBySku(String sku) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_SKU)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Item> search(String q, String category, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT ").append(BASE_COLS).append(" FROM items WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(sku) LIKE ?)");
            String like = "%" + q.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
        sql.append(" ORDER BY id DESC");
        if (limit <= 0) limit = 50;
        if (offset < 0) offset = 0;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Item> list = new ArrayList<>();
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Item createItem(Item item) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT_ITEM, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getSku());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getDescription());
            ps.setBigDecimal(5, safePrice(item.getUnitPrice()));
            ps.setInt(6, item.getStockQty());
            ps.setString(7, writeJson(item.getAttributes()));

            int n = ps.executeUpdate();
            if (n == 0) return null;

            int newId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) newId = keys.getInt(1);
            }

            // Re-read to capture timestamps (and any DB defaults)
            return newId > 0 ? findById(newId) : null;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // likely duplicate SKU
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateItem(Item item) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE_ITEM)) {

            ps.setString(1, item.getSku());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getDescription());
            ps.setBigDecimal(5, safePrice(item.getUnitPrice()));
            ps.setInt(6, item.getStockQty());
            ps.setString(7, writeJson(item.getAttributes()));
            ps.setInt(8, item.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // duplicate SKU on update
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItem(int id) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_ITEM)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean adjustStock(int id, int delta) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(ADJUST_STOCK)) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0; // false means would go negative or row missing
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countLowStock(int threshold) {
        try (Connection c = DBConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(COUNT_LOW_STOCK)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // --- helpers ---

    private static Item map(ResultSet rs) throws Exception {
        Item it = new Item();
        it.setId(rs.getInt("id"));
        it.setSku(rs.getString("sku"));
        it.setName(rs.getString("name"));
        it.setCategory(rs.getString("category"));
        it.setDescription(rs.getString("description"));
        it.setUnitPrice(rs.getBigDecimal("unit_price"));
        it.setStockQty(rs.getInt("stock_qty"));

        String json = rs.getString("attributes");
        Map<String, Object> attrs = (json == null || json.isBlank())
                ? new HashMap<>()
                : MAPPER.readValue(json, MAP_TYPE);
        it.setAttributes(attrs);

        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (cAt != null) it.setCreatedAt(cAt.toLocalDateTime());
        if (uAt != null) it.setUpdatedAt(uAt.toLocalDateTime());
        return it;
    }

    private static String writeJson(Map<String, Object> map) throws Exception {
        if (map == null || map.isEmpty()) return "{}";
        return MAPPER.writeValueAsString(map);
    }

    private static BigDecimal safePrice(BigDecimal p) {
        return (p == null) ? BigDecimal.ZERO : p.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
