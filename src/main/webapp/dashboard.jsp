<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String userRole = (String) request.getAttribute("userRole");
    String username = (String) request.getAttribute("username");
    String currentSection = (String) request.getAttribute("currentSection");
    Boolean hasSidebar = (Boolean) request.getAttribute("hasSidebar");
    if (hasSidebar == null) hasSidebar = Boolean.TRUE;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Pahana Edu - <%= currentSection != null ? currentSection : "dashboard" %></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
</head>
<body class="<%= hasSidebar ? "" : "no-sidebar" %>">
<jsp:include page="/WEB-INF/views/components/header.jsp" />

<div class="dashboard-layout">
    <% if (hasSidebar) { %>
    <jsp:include page="/WEB-INF/views/components/navigation.jsp" />
    <% } %>

    <main class="main-content" role="main">
        <div class="content-wrapper">
            <jsp:include page="/WEB-INF/views/dashboard/${currentSection}.jsp" />
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/components/footer.jsp" />
<script src="${pageContext.request.contextPath}/js/dashboard.js"></script>
</body>
</html>
