<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.pahanaedu.pahanasuite.models.Bill" %>
<%
    @SuppressWarnings("unchecked")
    List<Bill> bills = (List<Bill>) request.getAttribute("bills");
    if (bills == null) bills = Collections.emptyList();
    String period = (String) request.getAttribute("period");
    if (period == null) period = "daily";
    java.time.LocalDate selDate = (java.time.LocalDate) request.getAttribute("date");
    java.time.LocalDate selMonth = (java.time.LocalDate) request.getAttribute("month");
%>

<section class="section panel-section">
    <header class="panel-head">
        <h2 class="section-title">Reports</h2>
        <div class="actions no-print">
            <button class="btn btn-accent" type="button" onclick="window.print()">Print</button>
        </div>
    </header>

    <form class="filters no-print" method="get" action="${pageContext.request.contextPath}/dashboard/reports">
        <label>Period:
            <select name="period" onchange="this.form.submit()">
                <option value="daily" <%= "daily".equals(period) ? "selected" : "" %>>Daily</option>
                <option value="monthly" <%= "monthly".equals(period) ? "selected" : "" %>>Monthly</option>
            </select>
        </label>
        <span>
        <% if ("monthly".equals(period)) { %>
            <input type="month" name="month" value="<%= selMonth==null? "" : selMonth.toString().substring(0,7) %>" onchange="this.form.submit()">
        <% } else { %>
            <input type="date" name="date" value="<%= selDate==null? "" : selDate.toString() %>" onchange="this.form.submit()">
        <% } %>
        </span>
    </form>

    <div class="panel">
        <table class="data-table">
            <thead>
            <tr>
                <th>Bill No.</th>
                <th>Issued At</th>
                <th>Total</th>
            </tr>
            </thead>
            <tbody>
            <%
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                if (bills.isEmpty()) {
            %>
            <tr><td colspan="3">No bills.</td></tr>
            <%
                } else {
                    for (Bill b : bills) {
            %>
            <tr>
                <td><%= b.getBillNo()==null? ("#"+b.getId()) : b.getBillNo() %></td>
                <td><%= b.getIssuedAt()==null? "" : b.getIssuedAt().format(fmt) %></td>
                <td>LKR <%= b.getTotal()==null? "0.00" : String.format("%,.2f", b.getTotal()) %></td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="2"><strong>Total Bills</strong></td>
                <td><strong><%= request.getAttribute("count") == null ? 0 : request.getAttribute("count") %></strong></td>
            </tr>
            <tr>
                <td colspan="2"><strong>Total Revenue</strong></td>
                <td><strong>LKR <%= request.getAttribute("total") == null ? "0.00" : String.format("%,.2f", (BigDecimal) request.getAttribute("total")) %></strong></td>
            </tr>
            </tfoot>
        </table>
    </div>
</section>

<style>
@media print {
    .no-print{display:none;}
    body{background:#fff;}
    .panel{border:none;box-shadow:none;}
}
</style>

