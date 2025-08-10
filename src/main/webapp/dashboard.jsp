<%--
  Created by IntelliJ IDEA.
  User: User
  Date: 8/10/2025
  Time: 7:18 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - PahanaSuite Bookshop</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f5f6fa;
            line-height: 1.6;
        }

        .header {
            background: white;
            padding: 1rem 2rem;
            border-bottom: 1px solid #e1e5e9;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo { font-size: 1.5rem; font-weight: 600; color: #333; }

        .user-info {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .logout-btn {
            background: #dc3545;
            color: white;
            padding: 0.5rem 1rem;
            border: none;
            border-radius: 4px;
            text-decoration: none;
            font-size: 0.9rem;
        }

        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 1rem;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        .stat-card {
            background: white;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .stat-title {
            color: #666;
            font-size: 0.9rem;
            margin-bottom: 0.5rem;
        }

        .stat-value {
            font-size: 2rem;
            font-weight: 600;
            color: #333;
        }

        .stat-change {
            font-size: 0.8rem;
            color: #28a745;
            margin-top: 0.5rem;
        }

        .main-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 1.5rem;
        }

        .card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        .card-header {
            padding: 1rem 1.5rem;
            border-bottom: 1px solid #e1e5e9;
            font-weight: 600;
            background: #f8f9fa;
        }

        .card-content {
            padding: 1.5rem;
        }

        .action-buttons {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 1rem;
            margin-bottom: 2rem;
        }

        .action-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 1rem;
            text-align: center;
            text-decoration: none;
            border-radius: 8px;
            transition: transform 0.2s ease;
            font-weight: 500;
        }

        .action-btn:hover {
            transform: translateY(-2px);
        }

        .action-btn.secondary {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
        }

        .recent-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.75rem 0;
            border-bottom: 1px solid #f0f0f0;
        }

        .recent-item:last-child {
            border-bottom: none;
        }

        .item-info {
            flex: 1;
        }

        .item-title {
            font-weight: 500;
            color: #333;
        }

        .item-subtitle {
            font-size: 0.9rem;
            color: #666;
            margin-top: 0.25rem;
        }

        .item-amount {
            font-weight: 600;
            color: #28a745;
        }

        @media (max-width: 768px) {
            .main-grid {
                grid-template-columns: 1fr;
            }
            .action-buttons {
                grid-template-columns: 1fr;
            }
            .header {
                padding: 1rem;
                flex-direction: column;
                gap: 1rem;
            }
        }
    </style>
</head>
<body>
<div class="header">
    <div class="logo">PahanaSuite Bookshop</div>
    <div class="user-info">
            <span>Welcome, <%
                com.pahanaedu.pahanasuite.models.User user = (com.pahanaedu.pahanasuite.models.User) session.getAttribute("user");
                if (user != null) {
                    out.print(user.getUsername()); // or user.getName() if you have it
                } else {
                    out.print("User");
                }
            %></span>
        <a href="<%= request.getContextPath() %>/logout" class="logout-btn">Logout</a>
    </div>
</div>

<div class="container">
    <!-- Key Statistics -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-title">Today's Sales</div>
            <div class="stat-value">$1,247</div>
            <div class="stat-change">+12% from yesterday</div>
        </div>
        <div class="stat-card">
            <div class="stat-title">Books Sold</div>
            <div class="stat-value">47</div>
            <div class="stat-change">+8 from yesterday</div>
        </div>
        <div class="stat-card">
            <div class="stat-title">Total Customers</div>
            <div class="stat-value">1,293</div>
            <div class="stat-change">+5 new today</div>
        </div>
        <div class="stat-card">
            <div class="stat-title">Low Stock Items</div>
            <div class="stat-value">12</div>
            <div class="stat-change">Need restocking</div>
        </div>
    </div>

    <!-- Quick Actions -->
    <div class="action-buttons">
        <a href="<%= request.getContextPath() %>/sales/new" class="action-btn">New Sale</a>
        <a href="<%= request.getContextPath() %>/books/add" class="action-btn">Add Book</a>
        <a href="<%= request.getContextPath() %>/customers/add" class="action-btn secondary">Add Customer</a>
        <a href="<%= request.getContextPath() %>/inventory" class="action-btn secondary">View Inventory</a>
    </div>

    <!-- Main Content Grid -->
    <div class="main-grid">
        <!-- Recent Sales -->
        <div class="card">
            <div class="card-header">Recent Sales</div>
            <div class="card-content">
                <div class="recent-item">
                    <div class="item-info">
                        <div class="item-title">The Great Gatsby</div>
                        <div class="item-subtitle">Customer: John Smith • 2 copies</div>
                    </div>
                    <div class="item-amount">$25.98</div>
                </div>
                <div class="recent-item">
                    <div class="item-info">
                        <div class="item-title">To Kill a Mockingbird</div>
                        <div class="item-subtitle">Customer: Emma Davis • 1 copy</div>
                    </div>
                    <div class="item-amount">$14.99</div>
                </div>
                <div class="recent-item">
                    <div class="item-info">
                        <div class="item-title">1984 by George Orwell</div>
                        <div class="item-subtitle">Customer: Mike Johnson • 3 copies</div>
                    </div>
                    <div class="item-amount">$35.97</div>
                </div>
                <div class="recent-item">
                    <div class="item-info">
                        <div class="item-title">Pride and Prejudice</div>
                        <div class="item-subtitle">Customer: Sarah Wilson • 1 copy</div>
                    </div>
                    <div class="item-amount">$12.50</div>
                </div>
                <div style="text-align: center; margin-top: 1rem;">
                    <a href="<%= request.getContextPath() %>/sales" style="color: #667eea; text-decoration: none;">View All Sales →</a>
                </div>
            </div>
        </div>

        <!-- Quick Info -->
        <div>
            <!-- Low Stock Alert -->
            <div class="card" style="margin-bottom: 1.5rem;">
                <div class="card-header">Low Stock Alert</div>
                <div class="card-content">
                    <div class="recent-item">
                        <div class="item-info">
                            <div class="item-title">Harry Potter Series</div>
                            <div class="item-subtitle">Only 3 left</div>
                        </div>
                    </div>
                    <div class="recent-item">
                        <div class="item-info">
                            <div class="item-title">Lord of the Rings</div>
                            <div class="item-subtitle">Only 2 left</div>
                        </div>
                    </div>
                    <div class="recent-item">
                        <div class="item-info">
                            <div class="item-title">The Hobbit</div>
                            <div class="item-subtitle">Only 1 left</div>
                        </div>
                    </div>
                    <div style="text-align: center; margin-top: 1rem;">
                        <a href="<%= request.getContextPath() %>/inventory/low-stock" style="color: #dc3545; text-decoration: none;">Manage Stock →</a>
                    </div>
                </div>
            </div>

            <!-- Top Books -->
            <div class="card">
                <div class="card-header">Top Selling Books</div>
                <div class="card-content">
                    <div class="recent-item">
                        <div class="item-info">
                            <div class="item-title">The Great Gatsby</div>
                            <div class="item-subtitle">15 sold this week</div>
                        </div>
                    </div>
                    <div class="recent-item">
                        <div class="item-info">
                            <div class="item-title">1984</div>
                            <div class="item-subtitle">12 sold this week</div>
                        </div>
                    </div>
                    <div class="recent-item">
                        <div class="item-info">
                            <div class="item-title">To Kill a Mockingbird</div>
                            <div class="item-subtitle">9 sold this week</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
