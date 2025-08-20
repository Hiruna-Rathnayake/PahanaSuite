package com.pahanaedu.pahanasuite.web;

import com.pahanaedu.pahanasuite.dao.impl.BillDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.CustomerDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.ItemDAOImpl;
import com.pahanaedu.pahanasuite.dao.impl.PaymentDAOImpl;
import com.pahanaedu.pahanasuite.factories.BillFactory;
import com.pahanaedu.pahanasuite.factories.BillLineFactory;
import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillLine;
import com.pahanaedu.pahanasuite.models.BillStatus;
import com.pahanaedu.pahanasuite.models.Customer;
import com.pahanaedu.pahanasuite.models.Item;
import com.pahanaedu.pahanasuite.services.CustomerService;
import com.pahanaedu.pahanasuite.services.ItemService;
import com.pahanaedu.pahanasuite.services.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/dashboard/sales", "/billing"})
public class BillingServlet extends HttpServlet {

    private BillDAOImpl billDAO = new BillDAOImpl();
    private CustomerService customerService;
    private ItemService itemService;
    private PaymentService paymentService;

    @Override
    public void init() {
        customerService = new CustomerService(new CustomerDAOImpl());
        itemService     = new ItemService(new ItemDAOImpl());
        paymentService  = new PaymentService(new PaymentDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // --- filters ---
        String cq   = trim(req.getParameter("cq"));
        String cby  = trim(req.getParameter("cby"));
        String iq   = trim(req.getParameter("iq"));
        String icat = trim(req.getParameter("icat"));
        int limit   = parseInt(req.getParameter("limit"), 20);
        if (limit <= 0 || limit > 200) limit = 20;

        // --- customers (fuzzy) ---
        List<Customer> allCustomers = customerService.listAll();
        List<Customer> filteredCustomers;
        if (cq != null && !cq.isBlank()) {
            final String[] terms = cq.toLowerCase().split("\\s+");
            final boolean byTel = "tel".equalsIgnoreCase(cby);
            filteredCustomers = allCustomers.stream()
                    .map(c -> new Object[]{ c, scoreCustomer(c, terms, byTel) })
                    .filter(arr -> (double)arr[1] > 0.0)
                    .sorted((a,b) -> Double.compare((double)b[1], (double)a[1]))
                    .limit(limit)
                    .map(arr -> (Customer)arr[0])
                    .collect(Collectors.toList());
        } else {
            filteredCustomers = allCustomers.stream().limit(limit).collect(Collectors.toList());
        }

        // --- items (search then fuzzy rank) ---
        List<Item> baseItems = itemService.search(iq, icat, 1000, 0);
        List<Item> filteredItems;
        if (iq == null || iq.isBlank()) {
            filteredItems = baseItems.stream().limit(50).collect(Collectors.toList());
        } else {
            final String[] termsI = iq.toLowerCase().split("\\s+");
            filteredItems = baseItems.stream()
                    .map(it -> new Object[]{ it, scoreItem(it, termsI) })
                    .filter(arr -> (double)arr[1] > 0.0)
                    .sorted((a,b) -> Double.compare((double)b[1], (double)a[1]))
                    .limit(Math.max(50, limit))
                    .map(arr -> (Item)arr[0])
                    .collect(Collectors.toList());
        }

        // --- current bill + resolved customer ---
        Bill bill = (Bill) session.getAttribute("bill");
        Customer currentCustomer = null;
        if (bill != null) {
            for (Customer c : allCustomers) {
                if (c.getId() == bill.getCustomerId()) { currentCustomer = c; break; }
            }
        }

        // Optional: recent bills for sidebar/widgets if your JSP uses it
        req.setAttribute("recentBills", billDAO.findAll());

        // expose to JSPs
        req.setAttribute("cq", cq);
        req.setAttribute("cby", (cby == null || cby.isBlank()) ? "name" : cby);
        req.setAttribute("iq", iq);
        req.setAttribute("icat", icat);
        req.setAttribute("customers", filteredCustomers);
        req.setAttribute("items", filteredItems);
        req.setAttribute("bill", bill);
        req.setAttribute("currentCustomer", currentCustomer);

        // --- dashboard wrapper expectations ---
        req.setAttribute("currentSection", "sales");
        req.setAttribute("hasSidebar", Boolean.TRUE);
        Object role = session.getAttribute("userRole");
        Object user = session.getAttribute("username");
        if (role != null) req.setAttribute("userRole", role);
        if (user != null) req.setAttribute("username", user);

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        Bill bill = (Bill) session.getAttribute("bill");

        try {
            switch (action == null ? "" : action) {
                case "start" -> {
                    int customerId = parseInt(req.getParameter("customerId"), 0);
                    bill = BillFactory.create(customerId);
                    session.setAttribute("bill", bill);
                }
                case "addLine" -> {
                    ensureBill(bill);

                    // Read requested qty (guard <=0)
                    int addQty = Math.max(1, parseInt(req.getParameter("qty"), 1));

                    // Distinguish catalog vs custom line
                    Integer itemId = tryParseInt(req.getParameter("itemId"));
                    Item snap;

                    if (itemId != null && itemId > 0) {
                        // Catalog item snapshot
                        Item dbItem = itemService.getById(itemId);
                        if (dbItem == null) throw new IllegalArgumentException("Item not found");
                        snap = new Item();
                        snap.setId(dbItem.getId());
                        snap.setSku(dbItem.getSku());
                        snap.setName(dbItem.getName());
                        snap.setUnitPrice(dbItem.getUnitPrice());

                        // Merge on itemId primarily, fallback on SKU if needed
                        Optional<BillLine> existing = findExistingCatalogLine(bill, dbItem.getId(), dbItem.getSku(), dbItem.getUnitPrice());

                        if (existing.isPresent()) {
                            // Stock check against merged total
                            int newTotalQty = existing.get().getQuantity() + addQty;
                            if (!hasSufficientStock(dbItem, newTotalQty)) {
                                session.setAttribute("flash", "Only " + dbItem.getStockQty() + " in stock for " + nn(dbItem.getName()) + ".");
                            } else {
                                existing.get().setQuantity(newTotalQty);
                                bill.recomputeTotals();
                                //session.setAttribute("flash", "Added +" + addQty + " to " + nn(dbItem.getName()) + " (now " + newTotalQty + ").");
                            }
                        } else {
                            // Stock check for new line
                            if (!hasSufficientStock(dbItem, addQty)) {
                                session.setAttribute("flash", "Only " + dbItem.getStockQty() + " in stock for " + nn(dbItem.getName()) + ".");
                            } else {
                                bill.addLine(BillLineFactory.from(snap, addQty));
                                bill.recomputeTotals();
                            }
                        }

                    } else {
                        // Custom line snapshot
                        String sku  = trim(req.getParameter("sku"));
                        String name = trim(req.getParameter("name"));
                        if (name == null || name.isBlank()) {
                            session.setAttribute("flash", "Item name required for custom line.");
                            redirectSales(req, resp);
                            return;
                        }
                        BigDecimal unitPrice = money(req.getParameter("unitPrice"));

                        snap = new Item();
                        snap.setSku(sku);
                        snap.setName(name);
                        snap.setUnitPrice(unitPrice);

                        Optional<BillLine> existing = findExistingCustomLine(bill, sku, name, unitPrice);
                        if (existing.isPresent()) {
                            int newTotalQty = existing.get().getQuantity() + addQty;
                            existing.get().setQuantity(newTotalQty);
                            bill.recomputeTotals();
                            session.setAttribute("flash", "Added +" + addQty + " to " + name + " (now " + newTotalQty + ").");
                        } else {
                            bill.addLine(BillLineFactory.from(snap, addQty));
                            bill.recomputeTotals();
                        }
                    }
                }
                case "removeLine" -> {
                    ensureBill(bill);
                    int idx = parseInt(req.getParameter("index"), -1);
                    List<BillLine> lines = bill.getLines();
                    if (idx >= 0 && idx < lines.size()) {
                        bill.removeLine(lines.get(idx));
                        bill.recomputeTotals();
                    }
                }
                case "adjust" -> {
                    ensureBill(bill);
                    bill.setDiscountAmount(money(req.getParameter("discount")));
                    bill.setTaxAmount(money(req.getParameter("tax"))); // informational (included pricing)
                    bill.recomputeTotals();
                }
                case "save" -> {
                    ensureBill(bill);
                    if (bill.getLines() == null || bill.getLines().isEmpty()) {
                        session.setAttribute("flash", "Cannot save an empty bill.");
                        break;
                    }
                    Bill saved = billDAO.createBill(bill);
                    if (saved != null) {
                        int totalUnits = 0;
                        for (BillLine l : bill.getLines()) {
                            totalUnits += l.getQuantity();
                            Integer itemId = l.getItemId();
                            if (itemId != null) {
                                itemService.adjustStock(itemId, -l.getQuantity());
                            }
                        }
                        customerService.addUnitsConsumed(bill.getCustomerId(), totalUnits);

                        BigDecimal payAmt = money(req.getParameter("paymentAmount"));
                        String payMethod  = req.getParameter("paymentMethod");
                        String payRef     = req.getParameter("paymentRef");
                        if (payAmt.signum() > 0) {
                            paymentService.pay(saved.getId(), payAmt, payMethod, payRef);
                        }

                        BigDecimal remaining = paymentService.remainingBalance(saved.getId(), saved.getTotal());
                        if (remaining.signum() <= 0) {
                            saved.setStatus(BillStatus.PAID);
                            billDAO.updateBill(saved);
                        }

                        session.setAttribute("flash", "Bill saved (ID " + saved.getId() + ").");
                        session.removeAttribute("bill");
                        resp.sendRedirect(req.getContextPath() + "/billing/receipt?id=" + saved.getId());
                        return;
                    } else {
                        session.setAttribute("flash", "Save failed.");
                    }
                }
                case "cancel" -> session.removeAttribute("bill");
                default -> { /* no-op */ }
            }
        } catch (Exception e) {
            session.setAttribute("flash", "Error: " + e.getMessage());
        }

        redirectSales(req, resp);
    }

    // ---------- Merge helpers ----------

    /**
     * Finds an existing catalog line by itemId primarily, then by SKU + identical unit price.
     */
    private Optional<BillLine> findExistingCatalogLine(Bill bill, Integer itemId, String sku, BigDecimal unitPrice) {
        if (bill == null || bill.getLines() == null) return Optional.empty();

        // First pass, strong key: itemId
        if (itemId != null && itemId > 0) {
            for (BillLine l : bill.getLines()) {
                if (Objects.equals(getSafeItemId(l), itemId)) {
                    // Optional price guard, merge only if same unit price
                    if (pricesEqual(l.getUnitPrice(), unitPrice)) return Optional.of(l);
                }
            }
        }

        // Fallback, SKU + identical unit price
        if (sku != null && !sku.isBlank()) {
            for (BillLine l : bill.getLines()) {
                String lSku = nn(l.getSku());
                if (lSku.equalsIgnoreCase(sku) && pricesEqual(l.getUnitPrice(), unitPrice)) {
                    return Optional.of(l);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds an existing custom line by (sku || empty) + name match + identical unit price.
     */
    private Optional<BillLine> findExistingCustomLine(Bill bill, String sku, String name, BigDecimal unitPrice) {
        if (bill == null || bill.getLines() == null) return Optional.empty();
        String keySku  = nn(sku).trim();
        String keyName = nn(name).trim();

        for (BillLine l : bill.getLines()) {
            boolean skuMatch  = keySku.isEmpty() ? nn(l.getSku()).isEmpty() : keySku.equalsIgnoreCase(nn(l.getSku()));
            boolean nameMatch = keyName.equalsIgnoreCase(nn(l.getName()));
            boolean priceMatch = pricesEqual(l.getUnitPrice(), unitPrice);

            if (skuMatch && nameMatch && priceMatch) return Optional.of(l);
        }
        return Optional.empty();
    }

    /**
     * Returns true if prices are equal at scale(2).
     */
    private boolean pricesEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.setScale(2, RoundingMode.HALF_UP).compareTo(b.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    /**
     * Reads an itemId off a BillLine if your model supports it.
     * Falls back to null if absent. Adjust if your BillLine has a different accessor.
     */
    private Integer getSafeItemId(BillLine l) {
        try {
            return l.getItemId(); // assumes BillLine has itemId
        } catch (NoSuchMethodError | Exception ignore) {
            return null;
        }
    }

    /**
     * Stock guard for merged quantities. If stock is null or negative, treat as unlimited.
     */
    private boolean hasSufficientStock(Item dbItem, int desiredQty) {
        try {
            Integer stock = dbItem.getStockQty();
            if (stock == null || stock < 0) return true;
            return desiredQty <= stock;
        } catch (Exception ignore) {
            return true;
        }
    }

    // ---------- misc helpers ----------

    private void redirectSales(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/dashboard/sales");
    }

    private static void ensureBill(Bill bill) {
        if (bill == null) throw new IllegalStateException("Start a bill first");
    }
    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignore) { return def; }
    }
    private static Integer tryParseInt(String s) {
        try { return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s.trim()); }
        catch (Exception ignore) { return null; }
    }
    private static BigDecimal money(String s) {
        try { return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP); }
        catch (Exception ignore) { return BigDecimal.ZERO.setScale(2); }
    }
    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static String nn(String s) { return s == null ? "" : s; }

    // --- fuzzy scoring ---
    private static double scoreCustomer(Customer c, String[] terms, boolean byTel) {
        String name = nn(c.getName()).toLowerCase();
        String tel  = nn(c.getTelephone()).toLowerCase();
        String hay  = byTel ? tel : name;
        if (hay.isEmpty()) return 0.0;

        double score = 0.0;
        for (String t : terms) score += tokenScore(hay, t);
        if (!byTel && terms.length == 1 && !tel.isEmpty()) {
            score += 0.1 * tokenScore(tel, terms[0]);
        }
        return score;
    }
    private static double scoreItem(Item it, String[] terms) {
        String name = nn(it.getName()).toLowerCase();
        String sku  = nn(it.getSku()).toLowerCase();
        double score = 0.0;
        for (String t : terms) {
            score += tokenScore(name, t);
            score += tokenScore(sku, t) * 0.7;
        }
        return score;
    }
    private static double tokenScore(String hay, String needle) {
        if (needle.isEmpty() || hay.isEmpty()) return 0.0;
        if (hay.equals(needle)) return 1.0;
        if (hay.startsWith(needle)) return 0.9;
        if (hay.contains(needle)) return 0.7;
        double best = 0.0;
        for (String part : hay.split("\\s+|[-_/]")) {
            if (part.isEmpty()) continue;
            if (part.startsWith(needle)) best = Math.max(best, 0.85);
            else if (part.contains(needle)) best = Math.max(best, 0.65);
            best = Math.max(best, 0.6 * jaroWinkler(part, needle));
        }
        best = Math.max(best, 0.5 * jaroWinkler(hay, needle));
        return best;
    }
    private static double jaroWinkler(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int s1Len = s1.length(), s2Len = s2.length();
        if (s1Len == 0 || s2Len == 0) return 0.0;

        int matchDistance = Math.max(s1Len, s2Len) / 2 - 1;
        boolean[] s1Matches = new boolean[s1Len];
        boolean[] s2Matches = new boolean[s2Len];
        int matches = 0, transpositions = 0;

        for (int i = 0; i < s1Len; i++) {
            int start = Math.max(0, i - matchDistance);
            int end   = Math.min(i + matchDistance + 1, s2Len);
            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0.0;

        int k = 0;
        for (int i = 0; i < s1Len; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        double m = matches;
        double jaro = (m / s1Len + m / s2Len + (m - transpositions / 2.0) / m) / 3.0;

        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(s1Len, s2Len)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return jaro + prefix * 0.1 * (1.0 - jaro);
    }
}
