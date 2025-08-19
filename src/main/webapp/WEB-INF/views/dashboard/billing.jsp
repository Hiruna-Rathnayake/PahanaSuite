<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*, java.math.BigDecimal" %>
<%@ page import="com.pahanaedu.pahanasuite.models.Customer" %>
<%@ page import="com.pahanaedu.pahanasuite.models.Bill, com.pahanaedu.pahanasuite.models.BillLine" %>

<%
    String ctx = request.getContextPath();

    String q  = (String) request.getAttribute("q");
    String by = (String) request.getAttribute("by");
    @SuppressWarnings("unchecked")
    List<Customer> customers = (List<Customer>) request.getAttribute("customers");
    if (customers == null) customers = java.util.Collections.emptyList();

    Bill bill = (Bill) session.getAttribute("bill");
    String err = (String) request.getAttribute("error");
Integer savedId = (Integer) request.getAttribute("savedId");
%>

<style>
    .billing-page {
        --border: #e5e7eb;
        --border-light: #f3f4f6;
        --border-focus: #3b82f6;
        --muted: #6b7280;
        --panel-height: 78vh;
        font-family: system-ui, -apple-system, sans-serif;
    }
    .billing-page * { box-sizing: border-box; }
    .layout {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1.5rem;
    }
    @media (max-width: 980px) { .layout { grid-template-columns: 1fr; } }
    .customer-panel, .bill-panel {
        background: #fff;
        border-radius: 16px;
        padding: 19px;
        display: flex;
        flex-direction: column;
        height: var(--panel-height);
        min-height: 0;
        overflow: hidden;
        position: relative;
        font-size: 0.98rem;
    }
    .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.95rem;
        padding-bottom: 0.7rem;
        border-bottom: 2px solid var(--border-light);
    }
    .panel-title {
        font-size: 1.125rem;
        font-weight: 600;
        color: #111827;
        margin: 0;
    }
    .scroll-wrap {
        overflow: auto;
        border: 2px solid var(--border-light);
        border-radius: 12px;
        min-height: 0;
        flex: 1;
        background: #fefefe;
    }
    .scroll-wrap:hover { border-color: var(--border); }
    .scroll-wrap table { width: 100%; border-collapse: collapse; }
    .scroll-wrap th, .scroll-wrap td {
        padding: 8px 10px;
        border-bottom: 1px solid var(--border-light);
        text-align: left;
        font-size: 0.82rem;
    }
    .scroll-wrap thead th {
        position: sticky;
        top: 0;
        background: linear-gradient(135deg, #f8fafc, #f1f5f9);
        z-index: 1;
        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        font-weight: 600;
        color: #374151;
        font-size: 0.78rem;
        text-transform: uppercase;
        letter-spacing: 0.025em;
    }
</style>

<div class="billing-page">
<section class="section">
    <header class="panel-head">
        <h2 class="section-title">Billing</h2>
        <div class="quick-actions">
            <% if (err != null) { %>
            <span style="color:#92400e;background:#fef3c7;border:1px solid #fde68a;padding:.3rem .5rem;border-radius:6px;">
                    <%= err %>
                </span>
            <% } else if (savedId != null) { %>
            <span style="color:#065f46;background:#ecfdf5;border:1px solid #a7f3d0;padding:.3rem .5rem;border-radius:6px;">
                    Bill saved. ID: <%= savedId %>
                </span>
            <% } %>
        </div>
    </header>

    <div class="layout">
        <!-- Customer Selection Panel -->
        <div class="customer-panel">
            <div class="panel-header">
                <h3 class="panel-title">Customers</h3>
            </div>

            <form action="<%=ctx%>/billing" method="get" style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap;">
                <select name="by" style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                    <option value="name" <%= "tel".equalsIgnoreCase(by) ? "" : "selected" %>>By Name</option>
                    <option value="tel"  <%= "tel".equalsIgnoreCase(by) ? "selected" : "" %>>By Telephone</option>
                </select>
                <input type="search" name="q" value="<%= q==null? "" : q %>" placeholder="Search…"
                       style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;min-width:220px;">
                <button class="btn" type="submit">Find</button>
                <a class="btn" href="<%=ctx%>/billing">Reset</a>
            </form>

            <div style="flex:1;display:flex;flex-direction:column;">
                <%
                    if (customers.isEmpty()) {
                %>
                <div style="margin-top:.75rem;color:var(--muted);">No customers found.</div>
                <%
                    } else {
                %>
                <form action="<%=ctx%>/billing" method="post" style="margin-top:.75rem;display:flex;flex-direction:column;flex:1;">
                    <input type="hidden" name="action" value="start">
                    <div class="scroll-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th style="width:56px;"></th>
                                <th style="width:100px;">ID</th>
                                <th style="width:160px;">Account #</th>
                                <th>Name</th>
                                <th style="width:160px;">Telephone</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                for (Customer c : customers) {
                            %>
                            <tr>
                                <td><input type="radio" name="customerId" value="<%= c.getId() %>" required></td>
                                <td><%= c.getId() %></td>
                                <td><%= c.getAccountNumber() %></td>
                                <td><%= c.getName() %></td>
                                <td><%= c.getTelephone()==null? "" : c.getTelephone() %></td>
                            </tr>
                            <%
                                }
                            %>
                            </tbody>
                        </table>
                    </div>
                    <div style="margin-top:.5rem;display:flex;justify-content:flex-end;">
                        <button class="btn btn-accent" type="submit">Use Selected Customer</button>
                    </div>
                </form>
                <%
                    }
                %>
            </div>
        </div>

        <!-- Bill Lines Panel -->
        <div class="bill-panel">
            <div class="panel-header">
                <h3 class="panel-title">Bill</h3>
            </div>
            <% if (bill == null) { %>
            <div style="color:var(--muted);">Select a customer to start billing.</div>
            <% } else {
                List<BillLine> lines = bill.getLines();
            %>
            <h4 style="margin:0 0 .5rem 0;">Customer ID: <%= bill.getCustomerId() %></h4>

            <form action="<%=ctx%>/billing" method="post" style="display:grid;gap:.6rem;grid-template-columns:repeat(6,minmax(0,1fr));align-items:end;margin-bottom:.75rem;">
                <input type="hidden" name="action" value="addLine">
                <label>
                    <div>SKU</div>
                    <input name="sku" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Name *</div>
                    <input name="name" type="text" required style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Unit Price *</div>
                    <input name="unitPrice" type="number" step="0.01" min="0" required style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Qty *</div>
                    <input name="qty" type="number" min="0" value="1" required style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <div></div>
                <div>
                    <button class="btn btn-accent" type="submit">Add Line</button>
                </div>
            </form>

            <div class="scroll-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th style="width:56px;">#</th>
                        <th style="width:140px;">SKU</th>
                        <th>Name</th>
                        <th style="width:120px;">Unit Price</th>
                        <th style="width:90px;">Qty</th>
                        <th style="width:120px;">Line Total</th>
                        <th style="width:140px;">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        if (lines == null || lines.isEmpty()) {
                    %>
                    <tr><td colspan="7">No lines yet.</td></tr>
                    <%
                        } else {
                            for (int i = 0; i < lines.size(); i++) {
                                BillLine ln = lines.get(i);
                    %>
                    <tr>
                        <td><%= (i+1) %></td>
                        <td><%= ln.getSku()==null? "" : ln.getSku() %></td>
                        <td><%= ln.getName()==null? "" : ln.getName() %></td>
                        <td>Rs.<%= String.format("%.2f", ln.getUnitPrice()) %></td>
                        <td><%= ln.getQuantity() %></td>
                        <td>Rs.<%= String.format("%.2f", ln.getLineTotal()) %></td>
                        <td>
                            <form action="<%=ctx%>/billing" method="post" style="display:inline">
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

            <form action="<%=ctx%>/billing" method="post" style="margin-top:.75rem;display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:.5rem;align-items:end;">
                <input type="hidden" name="action" value="adjust">
                <label>
                    <div>Discount</div>
                    <input name="discount" type="number" step="0.01" min="0" value="<%= bill.getDiscountAmount() %>"
                           style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <label>
                    <div>Tax (info only)</div>
                    <input name="tax" type="number" step="0.01" min="0" value="<%= bill.getTaxAmount() %>"
                           style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                </label>
                <div></div>
                <div>
                    <button class="btn" type="submit">Apply</button>
                </div>
            </form>

            <div style="margin-top:.75rem;display:grid;grid-template-columns:1fr auto;gap:.4rem;align-items:center;">
                <div style="color:var(--muted);">Subtotal</div>
                <div><strong>Rs.<%= String.format("%.2f", bill.getSubtotal()) %></strong></div>
                <div style="color:var(--muted);">Discount</div>
                <div>− Rs.<%= String.format("%.2f", bill.getDiscountAmount()) %></div>
                <div style="color:var(--muted);">Tax</div>
                <div>+ Rs.<%= String.format("%.2f", bill.getTaxAmount()) %></div>
                <div style="border-top:1px solid var(--border);margin-top:.25rem;"></div>
                <div style="border-top:1px solid var(--border);margin-top:.25rem;"></div>
                <div style="font-size:1.05em;">Total</div>
                <div style="font-size:1.05em;"><strong>Rs.<%= String.format("%.2f", bill.getTotal()) %></strong></div>
            </div>

            <div style="margin-top:.75rem;display:flex;gap:.5rem;justify-content:flex-end;">
                <form action="<%=ctx%>/billing" method="post" style="display:inline">
                    <input type="hidden" name="action" value="cancel">
                    <button class="btn" type="submit">Cancel</button>
                </form>
                <form action="<%=ctx%>/billing" method="post" style="display:inline">
                    <input type="hidden" name="action" value="save">
                    <button class="btn btn-accent" type="submit">Save Bill</button>
                </form>
            </div>
            <% } %>
        </div>
    </div>
</section>

<script>
(function(){
    var billLines = document.querySelector('.bill-panel .scroll-wrap');
    if (billLines && billLines.scrollHeight > billLines.clientHeight) {
        billLines.scrollTop = billLines.scrollHeight;
    }
})();
</script>
</div>
