<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pahanaedu.pahanasuite.models.User" %>
<%
    String userRole = (String) request.getAttribute("userRole");
    String username = (String) request.getAttribute("username");
    String currentSection = (String) request.getAttribute("currentSection");
    User user = (User) request.getAttribute("user");

    boolean isAdmin = "admin".equals(userRole);
    boolean isCashier = "cashier".equals(userRole);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pahana Edu - Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">

</head>
<body>
<jsp:include page="/WEB-INF/views/components/header.jsp" />

<div class="dashboard-layout">
    <% if (isAdmin) { %>
    <jsp:include page="/WEB-INF/views/components/navigation.jsp" />
    <% } %>

    <main class="main-content">
        <div class="content-wrapper">
            <jsp:include page="<%= "/WEB-INF/views/dashboard/" + currentSection + ".jsp" %>" />
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/components/footer.jsp" />

<script src="${pageContext.request.contextPath}/js/dashboard.js"></script>
</body>
</html>
