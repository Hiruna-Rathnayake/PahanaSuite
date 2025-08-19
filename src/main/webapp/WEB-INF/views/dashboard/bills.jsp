<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.pahanaedu.pahanasuite.models.Bill,com.pahanaedu.pahanasuite.models.BillLine" %>
<%
    String ctx = request.getContextPath();
    @SuppressWarnings("unchecked")
    List<Bill> bills = (List<Bill>) request.getAttribute("bills");
    if (bills == null) bills = java.util.Collections.emptyList();
    @SuppressWarnings("unchecked")
    Map<Integer, BigDecimal> outstanding = (Map<Integer, BigDecimal>) request.getAttribute("outstanding");
    if (outstanding == null) outstanding = java.util.Collections.emptyMap();
    Bill selected = (Bill) request.getAttribute("selectedBill");
    String flash = (String) request.getAttribute("flash");
    String role = (String) request.getAttribute("userRole");
    boolean canWrite = role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("manager"));
%>

<section class="section panel-section">
    <header class="panel-head">
        <h2 class="section-title">Bills</h2>
    </header>

    <% if (flash != null && !flash.isBlank()) { %>
    <div class="panel" style="margin-bottom:1rem;background:#ecfdf5;border-color:#a7f3d0;">
        <div style="color:#065f46;"> <%= flash %> </div>
    </div>
    <% } %>

    <div class="panel flex-panel">
        <div class="scroll-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th style="width:80px;">ID</th>
                    <th style="width:120px;">Bill No</th>
                    <th style="width:120px;">Customer</th>
                    <th style="width:160px;">Issued At</th>
                    <th style="width:100px;">Status</th>
                    <th style="width:120px;">Outstanding</th>
                    <th style="width:240px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <% if (bills.isEmpty()) { %>
                <tr><td colspan="7">No bills found.</td></tr>
                <% } else { for (Bill b : bills) { %>
                <tr>
                    <td><%= b.getId() %></td>
                    <td><%= b.getBillNo() %></td>
                    <td><%= b.getCustomerId() %></td>
                    <td><%= b.getIssuedAt() %></td>
                    <td><%= b.getStatus() %></td>
                    <td>Rs.<%= String.format("%.2f", outstanding.getOrDefault(b.getId(), BigDecimal.ZERO)) %></td>
                    <td>
                        <a class="btn" href="<%=ctx%>/dashboard/bills?id=<%=b.getId()%>">View</a>
                        <% if (canWrite) { %>
                        <form action="<%=ctx%>/dashboard/bills" method="post" style="display:inline" onsubmit="return confirm('Delete bill <%=b.getBillNo()%>?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%=b.getId()%>">
                            <button class="btn" type="submit" style="background:var(--danger);color:#fff;border-color:transparent;">Delete</button>
                        </form>
                        <% if (b.getStatus() != null && !"PAID".equalsIgnoreCase(b.getStatus().name())) { %>
                        <form action="<%=ctx%>/dashboard/bills" method="post" style="display:inline">
                            <input type="hidden" name="action" value="markPaid">
                            <input type="hidden" name="id" value="<%=b.getId()%>">
                            <button class="btn" type="submit">Mark Paid</button>
                        </form>
                        <% } %>
                        <form action="<%=ctx%>/dashboard/bills" method="post" style="display:inline">
                            <input type="hidden" name="action" value="refund">
                            <input type="hidden" name="id" value="<%=b.getId()%>">
                            <input type="number" step="0.01" name="refundAmount" placeholder="Amount" style="width:80px;">
                            <input type="text" name="reference" placeholder="Ref" style="width:80px;">
                            <button class="btn" type="submit">Refund</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } } %>
                </tbody>
            </table>
        </div>
    </div>

    <% if (selected != null) { %>
    <div class="panel" style="margin-top:1rem;">
        <h3 style="margin-top:0;">Bill #<%= selected.getBillNo() %></h3>
        <div class="scroll-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>SKU</th>
                    <th>Item</th>
                    <th style="width:80px;">Qty</th>
                    <th style="width:100px;">Unit Price</th>
                    <th style="width:100px;">Line Total</th>
                </tr>
                </thead>
                <tbody>
                <%
                    List<BillLine> lines = selected.getLines();
                    if (lines == null || lines.isEmpty()) {
                %>
                <tr><td colspan="5">No lines.</td></tr>
                <%
                    } else {
                        for (BillLine l : lines) {
                %>
                <tr>
                    <td><%= l.getSku()==null? "" : l.getSku() %></td>
                    <td><%= l.getName()==null? "" : l.getName() %></td>
                    <td><%= l.getQuantity() %></td>
                    <td>Rs.<%= String.format("%.2f", l.getUnitPrice()) %></td>
                    <td>Rs.<%= String.format("%.2f", l.getLineTotal()) %></td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>
</section>
