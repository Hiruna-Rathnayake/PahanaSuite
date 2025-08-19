<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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

    <!-- Recent activity (placeholder) -->
    <div class="panel">
        <div class="panel-head">
            <h3>Recent Activity</h3>
            <a href="#">See all</a>
        </div>
        <table class="data-table">
            <thead>
            <tr>
                <th>Time</th>
                <th>Type</th>
                <th>Reference</th>
                <th>User</th>
                <th>Total</th>
            </tr>
            </thead>
            <tbody>
            <!-- TODO: loop real activity; placeholders for now -->
            <tr>
                <td>09:42</td>
                <td>Bill</td>
                <td>#B-10293</td>
                <td>kasun</td>
                <td>LKR 4,550.00</td>
            </tr>
            <tr>
                <td>09:15</td>
                <td>Customer</td>
                <td>#C-00871</td>
                <td>admin</td>
                <td>—</td>
            </tr>
            <tr>
                <td>08:58</td>
                <td>Stock</td>
                <td>“Grade 10 Maths”</td>
                <td>manager</td>
                <td>—</td>
            </tr>
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
