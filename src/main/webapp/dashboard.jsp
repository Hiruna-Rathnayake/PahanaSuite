<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String userRole = (String) request.getAttribute("userRole");
    String username = (String) request.getAttribute("username");
    String currentSection = (String) request.getAttribute("currentSection");
    if (currentSection == null || currentSection.isBlank()) currentSection = "overview";
    String bodyPath = "/WEB-INF/views/dashboard/" + currentSection + ".jsp";
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Pahana Edu - <%= currentSection %></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- use scriptlet for max compatibility -->
    <link rel="stylesheet" href="<%= ctx %>/css/dashboard.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/components/header.jsp" />

<div class="dashboard-layout">
    <main class="main-content" role="main">
        <div class="content-wrapper">
            <!-- avoid EL here; compute the path above -->
            <jsp:include page="<%= bodyPath %>" />
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/components/footer.jsp" />
<script src="<%= ctx %>/js/dashboard.js"></script>
</body>
</html>
