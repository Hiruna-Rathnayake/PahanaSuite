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
    Customer currentCustomer = (Customer) request.getAttribute("currentCustomer");

    String cq   = (String) request.getAttribute("cq");   // customer query
    String cby  = (String) request.getAttribute("cby");  // name|tel
    String iq   = (String) request.getAttribute("iq");   // item query
    String icat = (String) request.getAttribute("icat"); // item category

    String flash = (String) session.getAttribute("flash");
    if (flash != null) session.removeAttribute("flash");

    int limit = 20;
    try {
        String l = request.getParameter("limit");
        if (l != null && !l.isBlank()) limit = Math.min(200, Math.max(5, Integer.parseInt(l)));
    } catch (Exception ignored) {}

    // Check if bill has items for summary visibility
    boolean hasItems = bill != null && bill.getLines() != null && !bill.getLines().isEmpty();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sales / Billing</title>
    <style>
        :root {
            --border: #e5e7eb;
            --muted: #6b7280;
            --panel-height: 78vh; /* was 600px */
        }

        * { box-sizing: border-box; }

        body {
            font-family: system-ui, -apple-system, sans-serif;
        }

        .section { }

        .panel-head {
            margin-bottom: 1rem;
        }

        .section-title {
            margin: 0;
            color: #111827;
            font-size: 1.5rem;
        }

        .layout {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }

        @media (max-width: 980px) {
            .layout { grid-template-columns: 1fr; }
        }

        /* Left column: maintains consistent height */
        .left-col {
            display: flex;
            flex-direction: column;
            gap: 1rem;
            height: var(--panel-height);
            min-height: 0; /* allow children to shrink & scroll */
        }

        /* Customer panel: dynamic height based on state */
        .customer-panel {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 12px;
            transition: all 0.3s ease;
            overflow: hidden;

            /* flex column so inner scrollers can get height */
            display: flex;
            flex-direction: column;
        }

        .customer-panel.expanded {
            flex: 1;           /* fill when no customer selected */
            min-height: 0;     /* allow inner scroller */
        }

        .customer-panel.collapsed {
            flex: none;        /* shrink when customer selected */
            height: auto;
        }

        /* Make the customer picker a flex column so its scroll area can grow */
        #cust-picker {
            display: flex;
            flex-direction: column;
            gap: .5rem;
            min-height: 0;
        }

        /* Items panel: dynamic height based on customer state */
        .items-panel {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 12px;
            transition: all 0.3s ease;
            overflow: hidden;

            /* flex column like customer panel */
            display: flex;
            flex-direction: column;
        }

        .items-panel.hidden {
            flex: none;
            height: 60px; /* minimal when hidden */
        }

        .items-panel.expanded {
            flex: 1;       /* take remaining height */
            min-height: 0; /* allow inner scroller */
        }

        /* Right column: consistent height always */
        .bill-panel {
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 12px;
            height: var(--panel-height);

            /* ensure internal grid respects height */
            display: flex;
            flex-direction: column;
            min-height: 0; /* added */
        }

        /* Bill layout: lines scroll, summary pinned */
        .bill-wrap {
            display: grid;
            grid-template-rows: auto 1fr auto;
            gap: .75rem;
            height: 100%;
            min-height: 0; /* let middle section scroll */
        }

        /* Bill summary: completely hidden when no items */
        .bill-summary {
            border: 1px solid var(--border);
            border-radius: 10px;
            padding: 10px;
            background: #fff;
            transition: all 0.3s ease;
        }

        .bill-summary.hidden {
            display: none !important;
            height: 0;
            padding: 0;
            border: none;
            margin: 0;
        }

        /* Scrollable areas */
        .scroll-wrap {
            /* removed fixed cap */
            overflow: auto;
            border: 1px solid var(--border);
            border-radius: 10px;
            min-height: 0;

            /* take leftover height within tile */
            flex: 1;
        }

        .scroll-wrap table {
            width: 100%;
            border-collapse: collapse;
        }

        .scroll-wrap th, .scroll-wrap td {
            padding: 8px 10px;
            border-bottom: 1px solid var(--border);
            text-align: left;
        }

        .scroll-wrap thead th {
            position: sticky;
            top: 0;
            background: #fff;
            z-index: 1;
            box-shadow: 0 1px 0 var(--border);
        }

        .bill-lines {
            border: 1px solid var(--border);
            border-radius: 10px;
            overflow: auto;
            min-height: 0; /* allow scrolling */
        }

        .bill-lines table {
            width: 100%;
            border-collapse: collapse;
        }

        .bill-lines th, .bill-lines td {
            padding: 8px 10px;
            border-bottom: 1px solid var(--border);
            text-align: left;
        }

        .bill-lines thead th {
            position: sticky;
            top: 0;
            background: #fff;
            z-index: 1;
            box-shadow: 0 1px 0 var(--border);
        }

        .count {
            color: var(--muted);
            font-size: .9em;
            margin: .25rem 0;
        }

        .muted { color: var(--muted); }

        .btn {
            padding: .45rem .7rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            background: #f9fafb;
            cursor: pointer;
            font-size: 0.875rem;
            transition: all 0.2s ease;
        }

        .btn:hover {
            background: #f3f4f6;
        }

        .btn-accent {
            background: #111827;
            color: #fff;
            border-color: #111827;
        }

        .btn-accent:hover {
            background: #1f2937;
        }

        .btn-link {
            background: transparent;
            border: none;
            color: #2563eb;
            cursor: pointer;
            padding: .25rem;
        }

        .row {
            display: flex;
            gap: .5rem;
            align-items: center;
            flex-wrap: wrap;
        }

        .field {
            padding: .45rem .6rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 0.875rem;
        }

        .minw-200 { min-width: 200px; }

        /* Flash message */
        .flash-message {
            margin: .75rem 0;
        }

        .flash-content {
            color: #065f46;
            background: #ecfdf5;
            border: 1px solid #a7f3d0;
            padding: .5rem .75rem;
            border-radius: 8px;
        }

        /* Custom line details */
        details {
            margin-top: .5rem;
        }
    </style>

</head>
<body>
<section class="section">
    <header class="panel-head">
        <h2 class="section-title">Sales / Billing</h2>
    </header>

    <% if (flash != null) { %>
    <div class="flash-message">
        <div class="flash-content">
            <%= flash %>
        </div>
    </div>
    <% } %>

    <div class="layout">
        <!-- LEFT: selectors with dynamic sizing -->
        <div class="left-col">

            <!-- Customer panel: expands when no customer, collapses when selected -->
            <div class="customer-panel <%= bill == null ? "expanded" : "collapsed" %>">
                <div class="row" style="justify-content:space-between;">
                    <strong>Customer</strong>
                    <% if (bill != null) { %>
                    <button id="btn-toggle-cust" class="btn">Change</button>
                    <% } %>
                </div>

                <!-- Compact bar when selected -->
                <% if (bill != null) { %>
                <div id="cust-compact" style="margin-top:.5rem;">
                    <div>
                        <strong>
                            <%= (currentCustomer!=null && currentCustomer.getName()!=null && !currentCustomer.getName().isBlank())
                                    ? currentCustomer.getName()
                                    : ("#"+bill.getCustomerId()) %>
                        </strong>
                        <% if (currentCustomer!=null && currentCustomer.getTelephone()!=null && !currentCustomer.getTelephone().isBlank()) { %>
                        <span class="muted"> • <%= currentCustomer.getTelephone() %></span>
                        <% } %>
                    </div>
                    <% if (currentCustomer!=null && currentCustomer.getAddress()!=null && !currentCustomer.getAddress().isBlank()) { %>
                    <div class="muted" style="max-width:36ch;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                        <%= currentCustomer.getAddress() %>
                    </div>
                    <% } %>
                </div>
                <% } %>

                <!-- Full picker (hidden when selected unless "Change") -->
                <div id="cust-picker" style="<%= bill!=null ? "display:none" : "" %>; margin-top:.5rem;">
                    <form id="form-customer" action="<%=ctx%>/billing" method="get" class="row">
                        <input type="hidden" id="limit-customer" name="limit" value="<%= limit %>">
                        <input class="field minw-200" type="search" name="cq" value="<%= cq==null? "" : cq %>" placeholder="Find by name or phone…">
                        <select class="field" name="cby">
                            <option value="name" <%= "tel".equalsIgnoreCase(cby)? "" : "selected" %>>By name</option>
                            <option value="tel"  <%= "tel".equalsIgnoreCase(cby)? "selected" : "" %>>By phone</option>
                        </select>
                        <button class="btn" type="submit">Search</button>
                        <a class="btn" href="<%=ctx%>/billing">Reset</a>
                    </form>

                    <div class="count">Showing <%= customers.size() %> match(es)</div>
                    <div class="scroll-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th style="width:80px;">ID</th>
                                <th>Name</th>
                                <th style="width:160px;">Phone</th>
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
                                <td class="muted"><%= c.getAddress()==null? "—" : c.getAddress() %></td>
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

                    <% if (customers.size() >= limit) { %>
                    <div style="display:flex;justify-content:flex-end;margin-top:.5rem;">
                        <button id="more-customers" class="btn-link" type="button">Show more</button>
                    </div>
                    <% } %>
                </div>
            </div>

            <!-- Items panel: hidden when no customer, expands when customer selected -->
            <div class="items-panel <%= bill == null ? "hidden" : "expanded" %>">
                <div class="row" style="justify-content:space-between;">
                    <strong>Items</strong>
                    <% if (bill != null) { %>
                    <form id="form-item" action="<%=ctx%>/billing" method="get" class="row">
                        <input type="hidden" id="limit-item" name="limit" value="<%= limit %>">
                        <input class="field minw-200" type="search" name="iq" value="<%= iq==null? "" : iq %>" placeholder="Search by name or SKU…">
                        <select class="field" name="icat">
                            <option value="">All</option>
                            <option value="BOOK"       <%= "BOOK".equalsIgnoreCase(icat)?"selected":"" %>>Book</option>
                            <option value="STATIONERY" <%= "STATIONERY".equalsIgnoreCase(icat)?"selected":"" %>>Stationery</option>
                            <option value="GIFT"       <%= "GIFT".equalsIgnoreCase(icat)?"selected":"" %>>Gift</option>
                            <option value="OTHER"      <%= "OTHER".equalsIgnoreCase(icat)?"selected":"" %>>Other</option>
                        </select>
                        <button class="btn" type="submit">Search</button>
                    </form>
                    <% } else { %>
                    <span class="muted">Select a customer first</span>
                    <% } %>
                </div>

                <% if (bill != null) { %>
                <div class="count">Showing <%= items.size() %> match(es)</div>
                <div class="scroll-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th style="width:80px;">ID</th>
                            <th style="width:140px;">SKU</th>
                            <th>Name</th>
                            <th style="width:120px;">Price</th>
                            <th style="width:90px;">Stock</th>
                            <th style="width:210px;">Add</th>
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
                                <form action="<%=ctx%>/billing" method="post" class="row">
                                    <input type="hidden" name="action" value="addLine">
                                    <input type="hidden" name="itemId" value="<%= it.getId() %>">
                                    <label>Qty
                                        <input class="field" type="number" name="qty" value="1" min="1" style="width:80px;margin-left:.35rem;">
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

                <% if (items.size() >= Math.max(50, limit)) { %>
                <div style="display:flex;justify-content:flex-end;margin-top:.5rem;">
                    <button id="more-items" class="btn-link" type="button">Show more</button>
                </div>
                <% } %>

                <!-- Custom line -->
                <details style="margin-top:.5rem;">
                    <summary class="btn" style="display:inline-block;cursor:pointer;">Add custom line</summary>
                    <form action="<%=ctx%>/billing" method="post" class="row" style="margin-top:.5rem;">
                        <input type="hidden" name="action" value="addLine">
                        <label>
                            <div>SKU (optional)</div>
                            <input class="field" type="text" name="sku" style="min-width:140px;">
                        </label>
                        <label>
                            <div>Name *</div>
                            <input class="field" type="text" name="name" required style="min-width:180px;">
                        </label>
                        <label>
                            <div>Unit Price *</div>
                            <input class="field" type="number" name="unitPrice" step="0.01" min="0" required style="width:140px;">
                        </label>
                        <label>
                            <div>Qty</div>
                            <input class="field" type="number" name="qty" value="1" min="1" style="width:100px;">
                        </label>
                        <button class="btn" type="submit">Add</button>
                    </form>
                </details>
                <% } %>
            </div>
        </div>

        <!-- RIGHT: bill panel with consistent height -->
        <div class="bill-panel">
            <div class="bill-wrap">
                <div class="row" style="justify-content:space-between;">
                    <h3 style="margin:.25rem 0;">Current Bill</h3>
                    <% if (bill == null) { %>
                    <span class="muted">Select a customer to start.</span>
                    <% } %>
                </div>

                <div class="bill-lines" style="<%= bill==null? "opacity:.45;pointer-events:none;" : "" %>">
                    <table>
                        <thead>
                        <tr>
                            <th>#</th>
                            <th style="width:140px;">SKU</th>
                            <th>Item</th>
                            <th style="width:90px;">Qty</th>
                            <th style="width:120px;">Unit</th>
                            <th style="width:120px;">Line Disc.</th>
                            <th style="width:140px;">Line Total</th>
                            <th style="width:120px;">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            if (bill == null || bill.getLines()==null || bill.getLines().isEmpty()) {
                        %>
                        <tr><td colspan="8">No items yet. Use the left pane to add.</td></tr>
                        <%
                        } else {
                            List<BillLine> lines = bill.getLines();
                            for (int i=0; i<lines.size(); i++) {
                                BillLine l = lines.get(i);
                        %>
                        <tr>
                            <td><%= i+1 %></td>
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
                </div>

                <!-- Bill summary: completely hidden when no items -->
                <div class="bill-summary <%= !hasItems ? "hidden" : "" %>">
                    <% if (hasItems) { %>
                    <div style="display:grid;gap:.25rem;">
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

                    <!-- Adjust + Save aligned to the right -->
                    <div class="row" style="justify-content:flex-end; margin-top:.5rem;">
                        <form action="<%=ctx%>/billing" method="post" class="row" style="margin-right:auto;">
                            <input type="hidden" name="action" value="adjust">
                            <label>
                                <div class="muted" style="font-size:.85em;">Invoice Discount</div>
                                <input class="field" type="number" step="0.01" min="0" name="discount"
                                       value="<%= bill.getDiscountAmount()==null? "0.00" : bill.getDiscountAmount().setScale(2).toPlainString() %>"
                                       style="width:140px;">
                            </label>
                            <label>
                                <div class="muted" style="font-size:.85em;">Tax (info only)</div>
                                <input class="field" type="number" step="0.01" min="0" name="tax"
                                       value="<%= bill.getTaxAmount()==null? "0.00" : bill.getTaxAmount().setScale(2).toPlainString() %>"
                                       style="width:140px;">
                            </label>
                            <button class="btn" type="submit">Apply</button>
                        </form>

                        <form action="<%=ctx%>/billing" method="post" class="row">
                            <input type="hidden" name="action" value="save">
                            <label>
                                <div class="muted" style="font-size:.85em;">Payment</div>
                                <input class="field" type="number" name="paymentAmount" step="0.01" min="0" style="width:140px;">
                            </label>
                            <label>
                                <div class="muted" style="font-size:.85em;">Method</div>
                                <select class="field" name="paymentMethod">
                                    <option value="CASH">Cash</option>
                                    <option value="CARD">Card</option>
                                    <option value="ONLINE">Online</option>
                                </select>
                            </label>
                            <label>
                                <div class="muted" style="font-size:.85em;">Ref</div>
                                <input class="field" type="text" name="paymentRef" style="width:160px;">
                            </label>
                            <button class="btn btn-accent" type="submit">Save Bill</button>
                        </form>

                        <form action="<%=ctx%>/billing" method="post">
                            <input type="hidden" name="action" value="cancel">
                            <button class="btn" type="submit">Cancel</button>
                        </form>
                    </div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
    (function(){
        // Toggle customer picker visibility
        var toggleBtn = document.getElementById('btn-toggle-cust');
        var picker = document.getElementById('cust-picker');
        var compact = document.getElementById('cust-compact');
        var customerPanel = document.querySelector('.customer-panel');
        var itemsPanel = document.querySelector('.items-panel');

        toggleBtn?.addEventListener('click', function(){
            if (!picker) return;
            var show = (picker.style.display === 'none' || picker.style.display === '');
            picker.style.display = show ? 'block' : 'none';
            if (compact) compact.style.display = show ? 'none' : 'block';

            // Update panel classes for proper sizing
            if (show) {
                customerPanel?.classList.remove('collapsed');
                customerPanel?.classList.add('expanded');
                itemsPanel?.classList.remove('expanded');
                itemsPanel?.classList.add('hidden');
            } else {
                customerPanel?.classList.remove('expanded');
                customerPanel?.classList.add('collapsed');
                itemsPanel?.classList.remove('hidden');
                itemsPanel?.classList.add('expanded');
            }
        });

        // Show more customers/items
        function wireMore(btnId, formId, limitId, step, max) {
            var b = document.getElementById(btnId);
            var f = document.getElementById(formId);
            var l = document.getElementById(limitId);
            if (!b || !f || !l) return;
            b.addEventListener('click', function(){
                var v = parseInt(l.value || '20', 10);
                l.value = Math.min(max, v + step);
                f.submit();
            });
        }
        wireMore('more-customers', 'form-customer', 'limit-customer', 20, 200);
        wireMore('more-items', 'form-item', 'limit-item', 50, 200);
    })();
</script>
</body>
</html>