<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.pahanaedu.pahanasuite.models.*" %>
<%
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Customer> customers = (List<Customer>) request.getAttribute("customers");
    if (customers == null) customers = java.util.Collections.emptyList();

    @SuppressWarnings("unchecked")
    List<Item> items = (List<Item>) request.getAttribute("items");
    if (items == null) items = java.util.Collections.emptyList();

    Bill bill = (Bill) request.getAttribute("bill");

    String cq   = (String) request.getAttribute("cq");    // customer query
    String cby  = (String) request.getAttribute("cby");   // "name" | "tel"
    String iq   = (String) request.getAttribute("iq");    // item query
    String icat = (String) request.getAttribute("icat");  // item category

    String flash = (String) session.getAttribute("flash");
    if (flash != null) session.removeAttribute("flash");

    // show fewer results by default; can be increased with &limit=...
    int limit = 20;
    try {
        String l = request.getParameter("limit");
        if (l != null && !l.isBlank()) limit = Math.min(200, Math.max(5, Integer.parseInt(l)));
    } catch (Exception ignored) {}
%>

<section class="section">
    <header class="panel-head">
        <h2 class="section-title">Sales / Billing</h2>
    </header>

    <% if (flash != null) { %>
    <div class="panel" style="margin:.75rem 0;">
        <div style="color:#065f46;background:#ecfdf5;border:1px solid #a7f3d0;padding:.5rem .75rem;border-radius:8px;">
            <%= flash %>
        </div>
    </div>
    <% } %>

    <!-- =========================
         CUSTOMER: Search + Results + Use
         ========================= -->
    <div class="panel" style="margin-top:1rem;">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:.75rem;flex-wrap:wrap;">
            <div style="display:flex;align-items:center;gap:.5rem;flex-wrap:wrap;">
                <strong>Customer</strong>
                <% if (bill != null) { %>
                <span style="color:var(--muted)">Current: <strong>#<%= bill.getCustomerId() %></strong></span>
                <button class="btn" type="button" id="btn-toggle-customer">Change</button>
                <% } %>
            </div>

            <!-- search form -->
            <form action="<%=ctx%>/billing" method="get" style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap;">
                <input type="hidden" name="limit" value="<%= limit %>">
                <input type="search" name="cq" value="<%= cq==null? "" : cq %>" placeholder="Find by name or phone…"
                       style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;min-width:240px;">
                <select name="cby" style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                    <option value="name" <%= "tel".equalsIgnoreCase(cby)? "" : "selected" %>>By name</option>
                    <option value="tel"  <%= "tel".equalsIgnoreCase(cby)? "selected" : "" %>>By phone</option>
                </select>
                <button class="btn" type="submit">Search</button>
                <a class="btn" href="<%=ctx%>/billing">Reset</a>
            </form>
        </div>

        <!-- results table (collapsible when bill active) -->
        <div id="customer-results" style="<%= (bill!=null ? "display:none" : "display:block") %>;margin-top:.75rem;">
            <div style="color:var(--muted);margin-bottom:.25rem;">Showing <%= customers.size() %> match(es)</div>
            <table class="data-table">
                <thead>
                <tr>
                    <th style="width:80px;">ID</th>
                    <th>Name</th>
                    <th style="width:180px;">Phone</th>
                    <th>Address</th>
                    <th style="width:120px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (customers.isEmpty()) {
                %>
                <tr><td colspan="5">No customers found. Try a different search.</td></tr>
                <%
                } else {
                    for (Customer c : customers) {
                %>
                <tr>
                    <td><%= c.getId() %></td>
                    <td><%= c.getName()==null? "" : c.getName() %></td>
                    <td><%= c.getTelephone()==null? "" : c.getTelephone() %></td>
                    <td style="color:var(--muted)"><%= c.getAddress()==null? "—" : c.getAddress() %></td>
                    <td>
                        <form action="<%=ctx%>/billing" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="start">
                            <input type="hidden" name="customerId" value="<%= c.getId() %>">
                            <button class="btn btn-accent" type="submit">Use</button>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>

    <% if (bill != null) { %>
    <!-- =========================
         ITEMS: Search + Results + Add
         ========================= -->
    <div class="panel" style="margin-top:1rem;">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:.75rem;flex-wrap:wrap;">
            <div style="display:flex;align-items:center;gap:.5rem;flex-wrap:wrap;">
                <strong>Items</strong>
            </div>
            <!-- item search form -->
            <form action="<%=ctx%>/billing" method="get" style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap;">
                <input type="hidden" name="limit" value="<%= limit %>">
                <input type="search" name="iq" value="<%= iq==null? "" : iq %>" placeholder="Search by name or SKU…"
                       style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;min-width:260px;">
                <select name="icat" style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                    <option value="">All</option>
                    <option value="BOOK"       <%= "BOOK".equalsIgnoreCase(icat)?"selected":"" %>>Book</option>
                    <option value="STATIONERY" <%= "STATIONERY".equalsIgnoreCase(icat)?"selected":"" %>>Stationery</option>
                    <option value="GIFT"       <%= "GIFT".equalsIgnoreCase(icat)?"selected":"" %>>Gift</option>
                    <option value="OTHER"      <%= "OTHER".equalsIgnoreCase(icat)?"selected":"" %>>Other</option>
                </select>
                <button class="btn" type="submit">Search</button>
                <a class="btn" href="<%=ctx%>/billing">Reset</a>
            </form>
        </div>

        <!-- item results table -->
        <div style="margin-top:.75rem;">
            <div style="color:var(--muted);margin-bottom:.25rem;">Showing <%= items.size() %> match(es)</div>
            <table class="data-table">
                <thead>
                <tr>
                    <th style="width:80px;">ID</th>
                    <th style="width:140px;">SKU</th>
                    <th>Name</th>
                    <th style="width:120px;">Price</th>
                    <th style="width:100px;">Stock</th>
                    <th style="width:220px;">Add</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (items.isEmpty()) {
                %>
                <tr><td colspan="6">No items found. Try a different search.</td></tr>
                <%
                } else {
                    for (Item it : items) {
                %>
                <tr>
                    <td><%= it.getId() %></td>
                    <td><%= it.getSku()==null? "" : it.getSku() %></td>
                    <td><%= it.getName()==null? "" : it.getName() %></td>
                    <td>Rs.<%= it.getUnitPrice()==null? "0.00" : String.format("%.2f", it.getUnitPrice()) %></td>
                    <td><%= it.getStockQty() %></td>
                    <td>
                        <form action="<%=ctx%>/billing" method="post" style="display:flex;gap:.5rem;align-items:center;">
                            <input type="hidden" name="action" value="addLine">
                            <input type="hidden" name="itemId" value="<%= it.getId() %>">
                            <label>Qty
                                <input type="number" name="qty" value="1" min="1"
                                       style="width:80px;margin-left:.35rem;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                            </label>
                            <button class="btn" type="submit">Add</button>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>

        <!-- Optional: custom/free-form line right below items -->
        <details style="margin-top:.75rem;">
            <summary class="btn" style="display:inline-block;cursor:pointer;">Add custom line</summary>
            <form action="<%=ctx%>/billing" method="post" style="display:flex;gap:.5rem;align-items:flex-end;flex-wrap:wrap;margin-top:.5rem;">
                <input type="hidden" name="action" value="addLine">
                <label>
                    <div>SKU (optional)</div>
                    <input type="text" name="sku"
                           style="min-width:140px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Name *</div>
                    <input type="text" name="name" required
                           style="min-width:220px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Unit Price *</div>
                    <input type="number" name="unitPrice" step="0.01" min="0" required
                           style="width:140px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Qty</div>
                    <input type="number" name="qty" value="1" min="1"
                           style="width:100px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <button class="btn" type="submit">Add Custom Line</button>
            </form>
        </details>
    </div>

    <!-- =========================
         CURRENT BILL: Lines + Totals + Save
         ========================= -->
    <div class="panel" style="margin-top:1rem;">
        <h3 style="margin:.25rem 0 1rem 0;">Current Bill</h3>

        <table class="data-table">
            <thead>
            <tr>
                <th>#</th>
                <th style="width:140px;">SKU</th>
                <th>Item</th>
                <th style="width:100px;">Qty</th>
                <th style="width:140px;">Unit</th>
                <th style="width:140px;">Line Discount</th>
                <th style="width:140px;">Line Total</th>
                <th style="width:120px;">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                List<BillLine> lines = bill.getLines();
                if (lines == null || lines.isEmpty()) {
            %>
            <tr><td colspan="8">No items yet. Use the item table above to add lines.</td></tr>
            <%
            } else {
                for (int i=0; i<lines.size(); i++) {
                    BillLine l = lines.get(i);
            %>
            <tr>
                <td><%= (i+1) %></td>
                <td><%= l.getSku()==null? "" : l.getSku() %></td>
                <td><%= l.getName()==null? "" : l.getName() %></td>
                <td><%= l.getQuantity() %></td>
                <td>Rs.<%= String.format("%.2f", l.getUnitPrice()) %></td>
                <td>Rs.<%= String.format("%.2f", l.getLineDiscount()) %></td>
                <td><strong>Rs.<%= String.format("%.2f", l.getLineTotal()) %></strong></td>
                <td>
                    <form action="<%=ctx%>/billing" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="removeLine">
                        <input type="hidden" name="index" value="<%= i %>">
                        <button class="btn" type="submit">Remove</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>

        <!-- Adjustments + Summary -->
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-top:1rem;">
            <form action="<%=ctx%>/billing" method="post" style="display:grid;gap:.5rem;">
                <input type="hidden" name="action" value="adjust">
                <div style="display:flex;gap:.75rem;flex-wrap:wrap;">
                    <label>
                        <div>Invoice Discount</div>
                        <input type="number" step="0.01" min="0" name="discount"
                               value="<%= bill.getDiscountAmount()==null? "0.00" : bill.getDiscountAmount().setScale(2).toPlainString() %>"
                               style="width:160px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                    </label>
                    <label>
                        <div>Tax (info only)</div>
                        <input type="number" step="0.01" min="0" name="tax"
                               value="<%= bill.getTaxAmount()==null? "0.00" : bill.getTaxAmount().setScale(2).toPlainString() %>"
                               style="width:160px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                    </label>
                </div>
                <div>
                    <button class="btn" type="submit">Apply</button>
                </div>
            </form>

            <div style="display:grid;gap:.25rem;align-content:start;">
                <div style="display:flex;justify-content:space-between;">
                    <span>Subtotal</span>
                    <strong>Rs.<%= String.format("%.2f", bill.getSubtotal()) %></strong>
                </div>
                <div style="display:flex;justify-content:space-between;">
                    <span>Discount</span>
                    <strong>− Rs.<%= String.format("%.2f", bill.getDiscountAmount()) %></strong>
                </div>
                <div style="display:flex;justify-content:space-between;color:var(--muted);">
                    <span>Tax (included)</span>
                    <span>Rs.<%= String.format("%.2f", bill.getTaxAmount()) %></span>
                </div>
                <hr style="border:none;border-top:1px solid var(--border);margin:.25rem 0;">
                <div style="display:flex;justify-content:space-between;font-size:1.1em;">
                    <span>Total</span>
                    <strong>Rs.<%= String.format("%.2f", bill.getTotal()) %></strong>
                </div>
            </div>
        </div>

        <!-- Save / Cancel -->
        <div style="display:flex;justify-content:flex-end;gap:.5rem;margin-top:1rem;align-items:flex-end;flex-wrap:wrap;">
            <form action="<%=ctx%>/billing" method="post" style="display:flex;gap:.5rem;align-items:flex-end;flex-wrap:wrap;">
                <input type="hidden" name="action" value="save">

                <label>
                    <div>Payment now (optional)</div>
                    <input type="number" name="paymentAmount" step="0.01" min="0"
                           style="width:160px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Method</div>
                    <select name="paymentMethod" style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                        <option value="CASH">Cash</option>
                        <option value="CARD">Card</option>
                        <option value="ONLINE">Online</option>
                    </select>
                </label>
                <label>
                    <div>Reference</div>
                    <input type="text" name="paymentRef"
                           style="min-width:160px;padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                </label>

                <button class="btn btn-accent" type="submit">Save Bill</button>
            </form>

            <form action="<%=ctx%>/billing" method="post" style="display:inline;">
                <input type="hidden" name="action" value="cancel">
                <button class="btn" type="submit">Cancel</button>
            </form>
        </div>

    </div>
    <% } %>
</section>

<script>
    (function(){
        var btn = document.getElementById('btn-toggle-customer');
        var panel = document.getElementById('customer-results');
        if (btn && panel) {
            btn.addEventListener('click', function(){
                panel.style.display = (panel.style.display === 'none') ? 'block' : 'none';
            });
        }
    })();
</script>
