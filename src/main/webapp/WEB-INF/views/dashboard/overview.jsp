<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*,com.pahanaedu.pahanasuite.models.Bill" %>

<section class="section">
    <h2 class="section-title">Overview</h2>

    <!-- KPI cards -->
    <div class="kpi-grid">
        <div class="kpi-card">
            <div class="kpi-label">Today’s Bills</div>
            <div class="kpi-value"><%= request.getAttribute("kpiDailySales") == null ? 0 : request.getAttribute("kpiDailySales") %></div>
            <div class="kpi-foot"><a href="${pageContext.request.contextPath}/dashboard/sales">Open Billing</a></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-label">Bills this Month</div>
            <div class="kpi-value"><%= request.getAttribute("kpiMonthlySales") == null ? 0 : request.getAttribute("kpiMonthlySales") %></div>
            <div class="kpi-foot"><a href="#">View report</a></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-label">Total Customers</div>
            <div class="kpi-value"><%= request.getAttribute("kpiCustomers") == null ? 0 : request.getAttribute("kpiCustomers") %></div>
            <div class="kpi-foot"><a href="${pageContext.request.contextPath}/dashboard/customers">Manage customers</a></div>
        </div>
        <div class="kpi-card kpi-warn">
            <div class="kpi-label">Low‑stock Items</div>
            <div class="kpi-value"><%= request.getAttribute("kpiLowStockItems") == null ? 0 : request.getAttribute("kpiLowStockItems") %></div>
            <div class="kpi-foot"><a href="${pageContext.request.contextPath}/dashboard/sales">Review items</a></div>
        </div>
    </div>

    <!-- Quick actions -->
    <div class="quick-actions">
        <a class="btn btn-accent" href="${pageContext.request.contextPath}/dashboard/sales">New Bill</a>
        <a class="btn" href="${pageContext.request.contextPath}/dashboard/customers">New Customer</a>
        <a class="btn" href="${pageContext.request.contextPath}/dashboard/users">Add User</a>
        <a class="btn" href="${pageContext.request.contextPath}/dashboard/settings">Settings</a>
    </div>

    <%
        @SuppressWarnings("unchecked")
        List<Bill> recentBills = (List<Bill>) request.getAttribute("recentBills");
        if (recentBills == null) recentBills = Collections.emptyList();
        java.time.format.DateTimeFormatter tFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
    %>

    <div class="panel">
        <div class="panel-head">
            <h3>Recent Bills</h3>
        </div>
        <table class="data-table">
            <thead>
            <tr>
                <th>Time</th>
                <th>Bill No.</th>
                <th>User</th>
                <th>Total</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (recentBills.isEmpty()) {
            %>
            <tr><td colspan="4">No recent bills.</td></tr>
            <%
                } else {
                    for (Bill b : recentBills) {
                        String issuer;
                        try {
                            java.lang.reflect.Method m = b.getClass().getMethod("getIssuedBy");
                            Object val = m.invoke(b);
                            issuer = val == null ? "—" : String.valueOf(val);
                        } catch (Exception e) {
                            issuer = "—";
                        }
            %>
            <tr>
                <td><%= b.getIssuedAt()==null? "" : b.getIssuedAt().format(tFmt) %></td>
                <td><%= b.getBillNo()==null? ("#" + b.getId()) : b.getBillNo() %></td>
                <td><%= issuer %></td>
                <td>LKR <%= b.getTotal()==null? "0.00" : String.format("%,.2f", b.getTotal()) %></td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>

    <!-- Alerts (placeholder) -->
    <div class="panel">
        <div class="panel-head">
            <h3>Alerts</h3>
        </div>
        <ul class="alert-list">
            <li class="alert warn">7 items below reorder level. <a href="#">View list</a></li>
            <li class="alert info">2 pending user access requests. <a href="#">Review</a></li>
        </ul>
    </div>
</section>
