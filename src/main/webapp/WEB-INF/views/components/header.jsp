<%@ page contentType="text/html;charset=UTF-8" %>
<header class="dashboard-header">
    <div class="header-left">
        <h1>Pahana Suite Dashboard</h1>
    </div>
    <div class="header-right">
        <span>Welcome, ${username}</span>
        <form action="${pageContext.request.contextPath}/logout" method="post" class="logout-form">
            <button type="submit" class="logout-btn">Logout</button>
        </form>
    </div>
</header>
