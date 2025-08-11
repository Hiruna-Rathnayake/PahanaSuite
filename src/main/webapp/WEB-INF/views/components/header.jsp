<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String role = (String) request.getAttribute("userRole");
    boolean isCashier = "cashier".equalsIgnoreCase(role);
%>
<header class="dashboard-header" role="banner">
    <div class="header-left">
        <h1 class="app-title">Pahana Edu Bookshop</h1>
        <% if (isCashier) { %>
        <div class="header-actions">
            <a class="btn btn-accent" href="${pageContext.request.contextPath}/dashboard/sales">New Bill</a>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/dashboard/sales?mode=findCustomer">Find Customer</a>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/dashboard/help">Help</a>
        </div>
        <% } %>
    </div>
    <div class="header-right">
        <span class="welcome-text">Welcome, ${username}</span>
        <form action="${pageContext.request.contextPath}/logout" method="post" class="logout-form">
            <button type="submit" class="logout-btn" title="Sign out">Logout</button>
        </form>
    </div>
</header>
