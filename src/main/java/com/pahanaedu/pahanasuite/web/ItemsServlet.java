// File: src/main/java/com/pahanaedu/pahanasuite/web/ItemsServlet.java
package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.ItemDAOImpl;
import com.pahanaedu.pahanasuite.models.Item;
import com.pahanaedu.pahanasuite.services.ItemService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "/dashboard/items/actions")
public class ItemsServlet extends HttpServlet {

    private ItemService itemService;

    @Override
    public void init() {
        itemService = new ItemService(new ItemDAOImpl());
        // For memory testing later: itemService = new ItemService(new ItemDAOMemoryImpl());
    }

    // No GET here. DashboardServlet renders GET /dashboard/items.

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Only admin/manager can mutate items
        String role = String.valueOf(session.getAttribute("userRole"));
        boolean canWrite = "admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role);
        if (!canWrite) {
            resp.sendRedirect(req.getContextPath() + "/dashboard/items?err=forbidden");
            return;
        }

        String action = trim(req.getParameter("action"));
        try {
            switch (action == null ? "" : action) {
                case "create"      -> handleCreate(req);
                case "update"      -> handleUpdate(req);
                case "delete"      -> handleDelete(req);
                case "adjustStock" -> handleAdjustStock(req);
                default -> { /* ignore unknown */ }
            }
        } catch (Exception e) {
            session.setAttribute("flash", "Operation failed. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/dashboard/items?err=failed");
            return;
        }

        // PRG: back to list
        resp.sendRedirect(req.getContextPath() + "/dashboard/items");
    }

    // ---- actions ----

    private void handleCreate(HttpServletRequest req) {
        String sku         = trim(req.getParameter("sku"));
        String name        = trim(req.getParameter("name"));
        String category    = trim(req.getParameter("category"));
        String description = trim(req.getParameter("description"));
        BigDecimal price   = parseBigDecimal(req.getParameter("unitPrice"));
        int stock          = parseInt(req.getParameter("stockQty"));

        Map<String,Object> attrs = buildAttributes(req, category);

        Item created = itemService.create(sku, name, category, description, price, Math.max(stock,0), attrs);
        req.getSession().setAttribute("flash",
                created != null ? ("Item created: " + created.getSku())
                        : "Could not create item (invalid fields or duplicate SKU).");
    }

    private void handleUpdate(HttpServletRequest req) {
        int id             = parseInt(req.getParameter("id"));
        String sku         = trim(req.getParameter("sku"));
        String name        = trim(req.getParameter("name"));
        String category    = trim(req.getParameter("category"));
        String description = trim(req.getParameter("description"));
        BigDecimal price   = parseBigDecimal(req.getParameter("unitPrice"));
        int stock          = parseInt(req.getParameter("stockQty"));

        Map<String,Object> attrs = buildAttributes(req, category); // replace existing attrs

        Item updated = itemService.update(id, sku, name, category, description, price, Math.max(stock,0), attrs);
        req.getSession().setAttribute("flash", updated != null ? "Item updated." : "Update failed.");
    }

    private void handleDelete(HttpServletRequest req) {
        int id = parseInt(req.getParameter("id"));
        boolean ok = id > 0 && itemService.delete(id);
        req.getSession().setAttribute("flash", ok ? "Item deleted." : "Delete failed.");
    }

    private void handleAdjustStock(HttpServletRequest req) {
        int id = parseInt(req.getParameter("id"));

        Integer delta = tryParseInt(req.getParameter("delta"));
        if (delta == null) {
            String op = trim(req.getParameter("op")); // "inc" or "dec"
            int amount = Math.max(1, parseInt(req.getParameter("amount")));
            delta = "dec".equalsIgnoreCase(op) ? -amount : amount;
        }

        boolean ok = (id > 0 && delta != 0) && itemService.adjustStock(id, delta);
        req.getSession().setAttribute("flash",
                ok ? ("Stock " + (delta > 0 ? "increased" : "decreased") + " by " + Math.abs(delta) + ".")
                        : "Stock update failed (would go negative or item missing).");
    }

    // ---- attribute builder ----

    private Map<String,Object> buildAttributes(HttpServletRequest req, String category) {
        Map<String,Object> a = new HashMap<>();
        String cat = category == null ? "" : category.trim().toUpperCase();

        switch (cat) {
            case "BOOK" -> {
                putIfNotBlank(a, "author",    req.getParameter("attr_author"));
                putIfNotBlank(a, "isbn",      req.getParameter("attr_isbn"));
                putIfNotBlank(a, "publisher", req.getParameter("attr_publisher"));
                putIntIfParsable(a, "year",   req.getParameter("attr_year"));
            }
            case "STATIONERY" -> {
                putIfNotBlank(a, "brand", req.getParameter("attr_brand"));
                putIfNotBlank(a, "size",  req.getParameter("attr_size"));
                putIfNotBlank(a, "color", req.getParameter("attr_color"));
            }
            case "GIFT" -> {
                putIfNotBlank(a, "occasion", req.getParameter("attr_occasion"));
                putIfNotBlank(a, "target",   req.getParameter("attr_target"));
                putIfNotBlank(a, "material", req.getParameter("attr_material"));
            }
            default -> {
                putIfNotBlank(a, "notes", req.getParameter("attr_notes"));
            }
        }
        return a;
    }

    private static void putIfNotBlank(Map<String,Object> map, String key, String val) {
        if (val != null && !val.trim().isEmpty()) map.put(key, val.trim());
    }
    private static void putIntIfParsable(Map<String,Object> map, String key, String val) {
        try {
            if (val != null && !val.trim().isEmpty()) map.put(key, Integer.parseInt(val.trim()));
        } catch (NumberFormatException ignore) { /* skip */ }
    }

    // ---- helpers ----

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
    private static Integer tryParseInt(String s) {
        try { return (s == null) ? null : Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }
    private static BigDecimal parseBigDecimal(String s) {
        try { return (s == null || s.trim().isEmpty()) ? null : new BigDecimal(s.trim()); }
        catch (Exception e) { return null; }
    }
}
