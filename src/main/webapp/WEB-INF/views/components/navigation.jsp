<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String currentSection = (String) request.getAttribute("currentSection");
%>
<nav class="sidebar">
    <ul>
        <li><a class="<%= "overview".equals(currentSection) ? "active" : "" %>" href="${pageContext.request.contextPath}/dashboard/overview">Overview</a></li>
        <li><a class="<%= "users".equals(currentSection) ? "active" : "" %>" href="${pageContext.request.contextPath}/dashboard/users">Users</a></li>
        <li><a class="<%= "customers".equals(currentSection) ? "active" : "" %>" href="${pageContext.request.contextPath}/dashboard/customers">Customers</a></li>
        <li><a class="<%= "sales".equals(currentSection) ? "active" : "" %>" href="${pageContext.request.contextPath}/dashboard/sales">Sales</a></li>
        <li><a class="<%= "settings".equals(currentSection) ? "active" : "" %>" href="${pageContext.request.contextPath}/dashboard/settings">Settings</a></li>
    </ul>
</nav>
