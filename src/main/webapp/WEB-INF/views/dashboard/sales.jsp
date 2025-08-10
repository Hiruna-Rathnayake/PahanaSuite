<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Dashboard - Sales</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <script src="${pageContext.request.contextPath}/js/dashboard.js" defer></script>
</head>
<body>
<jsp:include page="../components/header.jsp" />
<jsp:include page="../components/navigation.jsp" />

<main>
    <h2>Sales Report</h2>
    <p>Summary of recent transactions:</p>
    <table border="1" cellpadding="5" cellspacing="0">
        <tr>
            <th>Date</th>
            <th>Order ID</th>
            <th>Amount</th>
            <th>Customer</th>
        </tr>
        <tr>
            <td>2025-08-10</td>
            <td>#1001</td>
            <td>$250.00</td>
            <td>John Doe</td>
        </tr>
        <tr>
            <td>2025-08-10</td>
            <td>#1002</td>
            <td>$125.00</td>
            <td>Jane Smith</td>
        </tr>
        <tr>
            <td>2025-08-11</td>
            <td>#1003</td>
            <td>$300.00</td>
            <td>Robert Brown</td>
        </tr>
    </table>
</main>

<jsp:include page="../components/footer.jsp" />
</body>
</html>
