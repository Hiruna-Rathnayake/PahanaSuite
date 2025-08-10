<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Dashboard - Overview</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <script src="${pageContext.request.contextPath}/js/dashboard.js" defer></script>
</head>
<body>
<jsp:include page="../components/header.jsp" />
<jsp:include page="../components/navigation.jsp" />

<main>
    <h2>Overview</h2>
    <p>Welcome back, ${username}! Here’s what’s happening today:</p>
    <ul>
        <li>Sales today: $1,250</li>
        <li>New customers: 5</li>
        <li>Pending orders: 12</li>
    </ul>
</main>

<jsp:include page="../components/footer.jsp" />
</body>
</html>
