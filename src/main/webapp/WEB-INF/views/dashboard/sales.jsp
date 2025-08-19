<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.pahanaedu.pahanasuite.models.*" %>
<%
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Customer> customers = (List<Customer>) request.getAttribute("customers");
    if (customers == null) customers = java.util.Collections.emptyList();

    @SuppressWarnings("unchecked")
    List<Item> items = (List<Item>) request.getAttribute("items");
    if (items == null) items = java.util.Collections.emptyList();

    Bill bill = (Bill) request.getAttribute("bill");
    Customer currentCustomer = (Customer) request.getAttribute("currentCustomer");

    String cq   = (String) request.getAttribute("cq");   // customer query
    String cby  = (String) request.getAttribute("cby");  // name|tel
    String iq   = (String) request.getAttribute("iq");   // item query
    String icat = (String) request.getAttribute("icat"); // item category

    String flash = (String) session.getAttribute("flash");
    if (flash != null) session.removeAttribute("flash");

    int limit = 20;
    try {
        String l = request.getParameter("limit");
        if (l != null && !l.isBlank()) limit = Math.min(200, Math.max(5, Integer.parseInt(l)));
    } catch (Exception ignored) {}

    // Check if bill has items for summary visibility
    boolean hasItems = bill != null && bill.getLines() != null && !bill.getLines().isEmpty();
%>
<style>
    .sales-page {
        --border: #e5e7eb;
        --border-light: #f3f4f6;
        --border-focus: #3b82f6;
        --muted: #6b7280;
        --panel-height: calc(100vh - 4rem - 6rem); /* viewport minus header and progress bar */
        --primary: #2563eb;
        --primary-dark: #1d4ed8;
        --success: #059669;
        --success-light: #ecfdf5;
        --warning: #d97706;
        --warning-light: #fef3c7;
        --danger: #dc2626;
        --step-active: #3b82f6;
        --step-complete: #059669;
        --step-inactive: #d1d5db;
        font-family: system-ui, -apple-system, sans-serif;
    }

    .sales-page * { box-sizing: border-box; }

    .section { }

        .panel-head {
            margin-bottom: 1rem;
            position: relative;
        }

        .section-title {
            margin: 0;
            color: #111827;
            font-size: 1.5rem;
        }

        /* Progress indicator */
        .progress-steps {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-top: 0.75rem;
            padding: 0.75rem 0;
            border-bottom: 1px solid var(--border-light);
        }

        .step {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
            color: var(--muted);
            transition: all 0.2s ease;
        }

        .step.active {
            color: var(--step-active);
            font-weight: 500;
        }

        .step.complete {
            color: var(--step-complete);
        }

        .step-number {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            background: var(--step-inactive);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.75rem;
            font-weight: 600;
            transition: all 0.2s ease;
        }

        .step.active .step-number {
            background: var(--step-active);
            transform: scale(1.1);
        }

        .step.complete .step-number {
            background: var(--step-complete);
        }

        .step-connector {
            width: 40px;
            height: 2px;
            background: var(--step-inactive);
            transition: all 0.2s ease;
        }

        .step-connector.active {
            background: var(--step-active);
        }

        .step-connector.complete {
            background: var(--step-complete);
        }

        .layout {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1.5rem;
        }

        @media (max-width: 980px) {
            .layout { grid-template-columns: 1fr; }
        }

        /* Left column: maintains consistent height */
        .left-col {
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
            height: var(--panel-height);
            min-height: 0;
        }

        /* Enhanced panel styling with better visual hierarchy */
        .customer-panel, .items-panel, .bill-panel {
            background: #fff;
            border-radius: 16px;
            padding: 19px;
            transition: all 0.3s ease;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            position: relative;
            font-size: 0.98rem;
        }

        /* Customer panel styling */
        .customer-panel {
            border: 2px solid var(--primary);
            box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.1);
        }

        .customer-panel.expanded {
            flex: 1;
            min-height: 0;
            border-color: var(--primary);
            box-shadow: 0 8px 25px -5px rgba(59, 130, 246, 0.2);
        }

        .customer-panel.collapsed {
            flex: none;
            height: auto;
            border-color: var(--step-complete);
            box-shadow: 0 4px 6px -1px rgba(5, 150, 105, 0.1);
        }

        /* Items panel styling */
        .items-panel {
            border: 2px solid var(--warning);
            box-shadow: 0 4px 6px -1px rgba(217, 119, 6, 0.1);
        }

        .items-panel.hidden {
            flex: none;
            height: 80px;
            border-color: var(--border);
            box-shadow: none;
            opacity: 0.6;
        }

        .items-panel.expanded {
            flex: 1;
            min-height: 0;
            border-color: var(--warning);
            box-shadow: 0 8px 25px -5px rgba(217, 119, 6, 0.2);
        }

        /* Bill panel styling */
        .bill-panel {
            border: 2px solid var(--success);
            box-shadow: 0 8px 25px -5px rgba(5, 150, 105, 0.2);
            height: var(--panel-height);
            min-height: 0;
        }

        /* Panel status indicators */
        .panel-status {
            position: absolute;
            top: 16px;
            right: 16px;
            padding: 3px 7px;
            border-radius: 16px;
            font-size: 0.7rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.025em;
            z-index: 1;
        }

        .panel-status.pending {
            background: var(--warning-light);
            color: var(--warning);
        }

        .panel-status.active {
            background: rgba(59, 130, 246, 0.1);
            color: var(--primary);
        }

        .panel-status.complete {
            background: var(--success-light);
            color: var(--success);
        }

        .panel-status.disabled {
            background: var(--border-light);
            color: var(--muted);
        }

        /* Panel headers with better emphasis */
        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 0.95rem;
            padding-bottom: 0.7rem;
            border-bottom: 2px solid var(--border-light);
            padding-right: 80px; /* Space for status badge */
        }

        .panel-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: #111827;
            margin: 0;
        }

        /* Guidance text */
        .guidance-text {
            background: linear-gradient(135deg, #eff6ff, #dbeafe);
            border: 1px solid #bfdbfe;
            border-radius: 8px;
            padding: 0.75rem;
            margin-bottom: 1rem;
            font-size: 0.875rem;
            color: #1e40af;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .guidance-icon {
            width: 16px;
            height: 16px;
            flex-shrink: 0;
        }

        /* Enhanced form controls */
        .field {
            padding: .55rem .75rem;
            border: 2px solid var(--border);
            border-radius: 10px;
            font-size: 0.82rem;
            transition: all 0.2s ease;
        }

        .field:focus {
            outline: none;
            border-color: var(--border-focus);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        /* Enhanced buttons */
        .btn {
            padding: .55rem .8rem;
            border: 2px solid var(--border);
            border-radius: 10px;
            background: #f9fafb;
            cursor: pointer;
            font-size: 0.82rem;
            font-weight: 500;
            transition: all 0.2s ease;
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
        }

        .btn:hover {
            background: #f3f4f6;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .btn-accent {
            background: linear-gradient(135deg, #111827, #1f2937);
            color: #fff;
            border-color: #111827;
        }

        .btn-accent:hover {
            background: linear-gradient(135deg, #1f2937, #374151);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(17, 24, 39, 0.3);
        }

        .btn-primary {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            border-color: var(--primary);
        }

        .btn-primary:hover {
            background: linear-gradient(135deg, var(--primary-dark), #1e40af);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
        }

        .btn-success {
            background: linear-gradient(135deg, var(--success), #047857);
            color: #fff;
            border-color: var(--success);
        }

        .btn-success:hover {
            background: linear-gradient(135deg, #047857, #065f46);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(5, 150, 105, 0.3);
        }

        .btn-link {
            background: transparent;
            border: none;
            color: var(--primary);
            cursor: pointer;
            padding: .5rem;
            font-weight: 500;
            transition: all 0.2s ease;
        }

        .btn-link:hover {
            color: var(--primary-dark);
            text-decoration: underline;
        }

        /* Make the customer picker a flex column so its scroll area can grow */
        #cust-picker {
            display: flex;
            flex-direction: column;
            gap: .75rem;
            min-height: 0;
        }

        /* Bill layout: lines scroll, summary pinned */
        .bill-wrap {
            display: grid;
            grid-template-rows: auto 1fr auto;
            gap: 1rem;
            height: 100%;
            min-height: 0;
        }

        /* Bill summary: completely hidden when no items */
        .bill-summary {
            border: 2px solid var(--success);
            border-radius: 12px;
            padding: 16px;
            background: linear-gradient(135deg, var(--success-light), #f0fdf4);
            transition: all 0.3s ease;
        }

        .bill-summary.hidden {
            display: none !important;
            height: 0;
            padding: 0;
            border: none;
            margin: 0;
        }

        /* Enhanced scrollable areas */
        .scroll-wrap {
            overflow: auto;
            border: 2px solid var(--border-light);
            border-radius: 12px;
            min-height: 0;
            flex: 1;
            background: #fefefe;
        }

        .scroll-wrap:hover {
            border-color: var(--border);
        }

        .scroll-wrap table {
            width: 100%;
            border-collapse: collapse;
        }

        .scroll-wrap th, .scroll-wrap td {
            padding: 8px 10px;
            border-bottom: 1px solid var(--border-light);
            text-align: left;
            font-size: 0.82rem;
        }

        .scroll-wrap thead th {
            position: sticky;
            top: 0;
            background: linear-gradient(135deg, #f8fafc, #f1f5f9);
            z-index: 1;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
            font-weight: 600;
            color: #374151;
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.025em;
        }

        .bill-lines {
            border: 2px solid var(--border-light);
            border-radius: 12px;
            overflow: auto;
            min-height: 0;
            background: #fefefe;
        }

        .bill-lines:hover {
            border-color: var(--border);
        }

        .bill-lines table {
            width: 100%;
            border-collapse: collapse;
        }

        .bill-lines th, .bill-lines td {
            padding: 8px 10px;
            border-bottom: 1px solid var(--border-light);
            text-align: left;
            font-size: 0.82rem;
        }

        .bill-lines thead th {
            position: sticky;
            top: 0;
            background: linear-gradient(135deg, #f8fafc, #f1f5f9);
            z-index: 1;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
            font-weight: 600;
            color: #374151;
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.025em;
        }

        /* Enhanced table rows */
        .scroll-wrap tbody tr:hover,
        .bill-lines tbody tr:hover {
            background: #f8fafc;
        }

        .count {
            color: var(--muted);
            font-size: .9em;
            margin: .5rem 0;
            font-weight: 500;
        }

        .muted { color: var(--muted); }

        .row {
            display: flex;
            gap: .75rem;
            align-items: center;
            flex-wrap: wrap;
        }

        .minw-200 { min-width: 200px; }

        /* Enhanced flash message */
        .flash-message {
            margin: 1rem 0;
            animation: slideIn 0.3s ease-out;
        }

        @keyframes slideIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .flash-content {
            color: #065f46;
            background: linear-gradient(135deg, #ecfdf5, #d1fae5);
            border: 2px solid #a7f3d0;
            padding: .75rem 1rem;
            border-radius: 12px;
            font-weight: 500;
        }

        /* Custom line details enhancement */
        details {
            margin-top: 1rem;
            border: 2px solid var(--border-light);
            border-radius: 12px;
            padding: 0;
            transition: all 0.2s ease;
        }

        details:hover {
            border-color: var(--border);
        }

        details[open] {
            border-color: var(--primary);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
        }

        details summary {
            padding: 0.75rem 1rem;
            cursor: pointer;
            font-weight: 500;
            background: var(--border-light);
            border-radius: 10px;
            transition: all 0.2s ease;
            list-style: none;
        }

        details summary:hover {
            background: #e5e7eb;
        }

        details[open] summary {
            background: rgba(59, 130, 246, 0.1);
            border-bottom-left-radius: 0;
            border-bottom-right-radius: 0;
        }

        details .details-content {
            padding: 1rem;
        }

        /* Loading states */
        .loading {
            opacity: 0.7;
            pointer-events: none;
            position: relative;
        }

        .loading::after {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 20px;
            height: 20px;
            margin: -10px 0 0 -10px;
            border: 2px solid var(--border);
            border-top: 2px solid var(--primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Empty state styling */
        .empty-state {
            text-align: center;
            padding: 2rem 1rem;
            color: var(--muted);
            font-style: italic;
        }

        .empty-state-icon {
            width: 48px;
            height: 48px;
            margin: 0 auto 1rem;
            opacity: 0.5;
        }

        /* Highlight active/selected items */
        .selected-customer {
            background: linear-gradient(135deg, var(--success-light), #f0fdf4) !important;
            border-color: var(--success) !important;
        }

        .item-row-highlight {
            background: linear-gradient(135deg, rgba(217, 119, 6, 0.1), rgba(251, 191, 36, 0.05)) !important;
        }

        /* Responsive improvements */
        @media (max-width: 640px) {
            .row {
                flex-direction: column;
                align-items: stretch;
            }

            .progress-steps {
                flex-wrap: wrap;
                gap: 0.5rem;
            }

            .step-connector {
                display: none;
            }
        }
    </style>
<div class="sales-page">
<section class="section">
    <header class="panel-head">
        <h2 class="section-title">Sales / Billing</h2>

        <!-- Progress Steps -->
        <div class="progress-steps">
            <div class="step <%= bill == null ? "active" : "complete" %>">
                <div class="step-number">1</div>
                <span>Select Customer</span>
            </div>
            <div class="step-connector <%= bill != null ? "complete" : "" %>"></div>
            <div class="step <%= bill != null && !hasItems ? "active" : (hasItems ? "complete" : "") %>">
                <div class="step-number">2</div>
                <span>Add Items</span>
            </div>
            <div class="step-connector <%= hasItems ? "complete" : "" %>"></div>
            <div class="step <%= hasItems ? "active" : "" %>">
                <div class="step-number">3</div>
                <span>Complete Sale</span>
            </div>
        </div>
    </header>

    <% if (flash != null) { %>
    <div class="flash-message">
        <div class="flash-content">
            ✅ <%= flash %>
        </div>
    </div>
    <% } %>

    <div class="layout">
        <!-- LEFT: selectors with dynamic sizing -->
        <div class="left-col">

            <!-- Customer panel: expands when no customer, collapses when selected -->
            <div class="customer-panel <%= bill == null ? "expanded" : "collapsed" %>">
                <div class="panel-status <%= bill == null ? "active" : "complete" %>">
                    <%= bill == null ? "Active" : "Selected" %>
                </div>

                <div class="panel-header">
                    <h3 class="panel-title">🔍 Customer Selection</h3>
                    <% if (bill != null) { %>
                    <button id="btn-toggle-cust" class="btn btn-primary">Change Customer</button>
                    <% } %>
                </div>

                <% if (bill == null) { %>
                <div class="guidance-text">
                    <svg class="guidance-icon" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
                    </svg>
                    Search and select a customer to start creating a new bill
                </div>
                <% } %>

                <!-- Compact bar when selected -->
                <% if (bill != null) { %>
                <div id="cust-compact" class="selected-customer" style="margin-top:.75rem; padding: 1rem; border: 2px solid var(--success); border-radius: 12px;">
                    <div>
                        <strong style="font-size: 1.1em;">
                            <%= (currentCustomer!=null && currentCustomer.getName()!=null && !currentCustomer.getName().isBlank())
                                    ? currentCustomer.getName()
                                    : ("#"+bill.getCustomerId()) %>
                        </strong>
                        <% if (currentCustomer!=null && currentCustomer.getTelephone()!=null && !currentCustomer.getTelephone().isBlank()) { %>
                        <span class="muted"> • 📞 <%= currentCustomer.getTelephone() %></span>
                        <% } %>
                    </div>
                    <% if (currentCustomer!=null && currentCustomer.getAddress()!=null && !currentCustomer.getAddress().isBlank()) { %>
                    <div class="muted" style="max-width:36ch;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-top:0.25rem;">
                        📍 <%= currentCustomer.getAddress() %>
                    </div>
                    <% } %>
                </div>
                <% } %>

                <!-- Full picker (hidden when selected unless "Change") -->
                <div id="cust-picker" style="<%= bill!=null ? "display:none" : "" %>; margin-top:.75rem;">
                    <form id="form-customer" action="<%=ctx%>/billing" method="get" class="row">
                        <input type="hidden" id="limit-customer" name="limit" value="<%= limit %>">
                        <input class="field minw-200" type="search" name="cq" value="<%= cq==null? "" : cq %>" placeholder="🔍 Find by name or phone…" autocomplete="off">
                        <select class="field" name="cby">
                            <option value="name" <%= "tel".equalsIgnoreCase(cby)? "" : "selected" %>>By name</option>
                            <option value="tel"  <%= "tel".equalsIgnoreCase(cby)? "selected" : "" %>>By phone</option>
                        </select>
                        <button class="btn btn-primary" type="submit">🔍 Search</button>
                        <a class="btn" href="<%=ctx%>/billing">🔄 Reset</a>
                    </form>

                    <div class="count">📋 Showing <%= customers.size() %> match(es)</div>
                    <div class="scroll-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th style="width:80px;">ID</th>
                                <th>Name</th>
                                <th style="width:160px;">Phone</th>
                                <th>Address</th>
                                <th style="width:120px;">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                if (customers.isEmpty()) {
                            %>
                            <tr><td colspan="5" class="empty-state">
                                <div class="empty-state-icon">👥</div>
                                <div>No customers found. Try a different search term.</div>
                            </td></tr>
                            <%
                            } else {
                                for (Customer c : customers) {
                            %>
                            <tr>
                                <td><strong>#<%= c.getId() %></strong></td>
                                <td><%= c.getName()==null? "—" : c.getName() %></td>
                                <td>📞 <%= c.getTelephone()==null? "—" : c.getTelephone() %></td>
                                <td class="muted">📍 <%= c.getAddress()==null? "—" : c.getAddress() %></td>
                                <td>
                                    <form action="<%=ctx%>/billing" method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="start">
                                        <input type="hidden" name="customerId" value="<%= c.getId() %>">
                                        <button class="btn btn-success" type="submit">✅ Select</button>
                                    </form>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            </tbody>
                        </table>
                    </div>

                    <% if (customers.size() >= limit) { %>
                    <div style="display:flex;justify-content:flex-end;margin-top:.75rem;">
                        <button id="more-customers" class="btn-link" type="button">📄 Show more customers</button>
                    </div>
                    <% } %>
                </div>
            </div>

            <!-- Items panel: hidden when no customer, expands when customer selected -->
            <div class="items-panel <%= bill == null ? "hidden" : "expanded" %>">
                <div class="panel-status <%= bill == null ? "disabled" : "active" %>">
                    <%= bill == null ? "Waiting" : "Active" %>
                </div>

                <div class="panel-header">
                    <h3 class="panel-title">🛍️ Item Selection</h3>
                    <% if (bill != null) { %>
                    <span class="muted" style="font-size: 0.875rem;">Add items to the bill</span>
                    <% } else { %>
                    <span class="muted">Select a customer first</span>
                    <% } %>
                </div>

                <% if (bill != null) { %>
                <div class="guidance-text">
                    <svg class="guidance-icon" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd"/>
                    </svg>
                    Search for items and add them to the customer's bill
                </div>

                <form id="form-item" action="<%=ctx%>/billing" method="get" class="row" style="margin-bottom: 0.75rem;">
                    <input type="hidden" id="limit-item" name="limit" value="<%= limit %>">
                    <input class="field minw-200" type="search" name="iq" value="<%= iq==null? "" : iq %>" placeholder="🔍 Search by name or SKU…" autocomplete="off">
                    <select class="field" name="icat">
                        <option value="">📋 All Categories</option>
                        <option value="BOOK"       <%= "BOOK".equalsIgnoreCase(icat)?"selected":"" %>>📚 Books</option>
                        <option value="STATIONERY" <%= "STATIONERY".equalsIgnoreCase(icat)?"selected":"" %>>✏️ Stationery</option>
                        <option value="GIFT"       <%= "GIFT".equalsIgnoreCase(icat)?"selected":"" %>>🎁 Gifts</option>
                        <option value="OTHER"      <%= "OTHER".equalsIgnoreCase(icat)?"selected":"" %>>📦 Other</option>
                    </select>
                    <button class="btn btn-primary" type="submit">🔍 Search</button>
                </form>

                <div class="count">📦 Showing <%= items.size() %> match(es)</div>
                <div class="scroll-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th style="width:80px;">ID</th>
                            <th style="width:140px;">SKU</th>
                            <th>Name</th>
                            <th style="width:120px;">Price</th>
                            <th style="width:90px;">Stock</th>
                            <th style="width:210px;">Add to Bill</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            if (items.isEmpty()) {
                        %>
                        <tr><td colspan="6" class="empty-state">
                            <div class="empty-state-icon">📦</div>
                            <div>No items found. Try a different search or browse all categories.</div>
                        </td></tr>
                        <%
                        } else {
                            for (Item it : items) {
                        %>
                        <tr class="item-row-highlight">
                            <td><strong>#<%= it.getId() %></strong></td>
                            <td><code><%= it.getSku()==null? "—" : it.getSku() %></code></td>
                            <td><%= it.getName()==null? "—" : it.getName() %></td>
                            <td><strong>Rs.<%= it.getUnitPrice()==null? "0.00" : String.format("%.2f", it.getUnitPrice()) %></strong></td>
                            <td>
                                <span style="padding: 2px 8px; background: <%= it.getStockQty() > 10 ? "#dcfce7" : (it.getStockQty() > 0 ? "#fef3c7" : "#fee2e2") %>;
                                        color: <%= it.getStockQty() > 10 ? "#166534" : (it.getStockQty() > 0 ? "#92400e" : "#991b1b") %>;
                                        border-radius: 12px; font-size: 0.875rem; font-weight: 600;">
                                    <%= it.getStockQty() %>
                                </span>
                            </td>
                            <td>
                                <form action="<%=ctx%>/billing" method="post" class="row">
                                    <input type="hidden" name="action" value="addLine">
                                    <input type="hidden" name="itemId" value="<%= it.getId() %>">
                                    <label style="display: flex; align-items: center; gap: 0.5rem;">
                                        <span style="font-size: 0.875rem; font-weight: 500;">Qty</span>
                                        <input class="field" type="number" name="qty" value="1" min="1" max="<%= it.getStockQty() %>"
                                               style="width:70px;" required>
                                    </label>
                                    <button class="btn btn-success" type="submit" <%= it.getStockQty() <= 0 ? "disabled" : "" %>>
                                        <%= it.getStockQty() <= 0 ? "🚫 No Stock" : "➕ Add" %>
                                    </button>
                                </form>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                        </tbody>
                    </table>
                </div>

                <% if (items.size() >= Math.max(50, limit)) { %>
                <div style="display:flex;justify-content:flex-end;margin-top:.75rem;">
                    <button id="more-items" class="btn-link" type="button">📄 Show more items</button>
                </div>
                <% } %>

                <!-- Custom line -->
                <details style="margin-top:1rem;">
                    <summary>➕ Add Custom Item</summary>
                    <div class="details-content">
                        <div class="guidance-text" style="margin-bottom: 1rem;">
                            <svg class="guidance-icon" fill="currentColor" viewBox="0 0 20 20">
                                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
                            </svg>
                            Create a custom line item for products not in your inventory
                        </div>
                        <form action="<%=ctx%>/billing" method="post" class="row">
                            <input type="hidden" name="action" value="addLine">
                            <label>
                                <div style="font-weight: 500; margin-bottom: 0.25rem;">SKU (optional)</div>
                                <input class="field" type="text" name="sku" style="min-width:140px;" placeholder="e.g., CUSTOM-001">
                            </label>
                            <label>
                                <div style="font-weight: 500; margin-bottom: 0.25rem;">Item Name *</div>
                                <input class="field" type="text" name="name" required style="min-width:180px;" placeholder="e.g., Special Service">
                            </label>
                            <label>
                                <div style="font-weight: 500; margin-bottom: 0.25rem;">Unit Price *</div>
                                <input class="field" type="number" name="unitPrice" step="0.01" min="0" required style="width:140px;" placeholder="0.00">
                            </label>
                            <label>
                                <div style="font-weight: 500; margin-bottom: 0.25rem;">Quantity</div>
                                <input class="field" type="number" name="qty" value="1" min="1" style="width:100px;">
                            </label>
                            <button class="btn btn-success" type="submit">➕ Add Custom</button>
                        </form>
                    </div>
                </details>
                <% } else { %>
                <div class="guidance-text">
                    <svg class="guidance-icon" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/>
                    </svg>
                    Please select a customer first to enable item selection
                </div>
                <% } %>
            </div>
        </div>

        <!-- RIGHT: bill panel with consistent height -->
        <div class="bill-panel">
            <div class="panel-status <%= bill == null ? "disabled" : (hasItems ? "active" : "pending") %>">
                <%= bill == null ? "Waiting" : (hasItems ? "Ready" : "Empty") %>
            </div>

            <div class="bill-wrap">
                <div class="panel-header">
                    <h3 class="panel-title">🧾 Current Bill</h3>
                    <% if (bill == null) { %>
                    <span class="muted">Select a customer to start</span>
                    <% } else if (!hasItems) { %>
                    <span class="muted">Add items to continue</span>
                    <% } else { %>
                    <span style="color: var(--success); font-weight: 600;">Ready to save</span>
                    <% } %>
                </div>

                <% if (bill == null) { %>
                <div class="guidance-text">
                    <svg class="guidance-icon" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M4 4a2 2 0 00-2 2v1h16V6a2 2 0 00-2-2H4z"/>
                        <path fill-rule="evenodd" d="M18 9H2v5a2 2 0 002 2h12a2 2 0 002-2V9zM4 13a1 1 0 011-1h1a1 1 0 110 2H5a1 1 0 01-1-1zm5-1a1 1 0 100 2h1a1 1 0 100-2H9z" clip-rule="evenodd"/>
                    </svg>
                    Your bill will appear here once you select a customer and add items
                </div>
                <% } else if (!hasItems) { %>
                <div class="guidance-text">
                    <svg class="guidance-icon" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd"/>
                    </svg>
                    Use the Items panel to add products to this bill
                </div>
                <% } %>

                <div class="bill-lines" style="<%= bill==null? "opacity:.3;pointer-events:none;" : "" %>">
                    <table>
                        <thead>
                        <tr>
                            <th>#</th>
                            <th style="width:140px;">SKU</th>
                            <th>Item</th>
                            <th style="width:90px;">Qty</th>
                            <th style="width:120px;">Unit Price</th>
                            <th style="width:120px;">Line Disc.</th>
                            <th style="width:140px;">Line Total</th>
                            <th style="width:120px;">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            if (bill == null || bill.getLines()==null || bill.getLines().isEmpty()) {
                        %>
                        <tr><td colspan="8" class="empty-state">
                            <div class="empty-state-icon">🛒</div>
                            <div>No items added yet. Use the Items panel to add products.</div>
                        </td></tr>
                        <%
                        } else {
                            List<BillLine> lines = bill.getLines();
                            for (int i=0; i<lines.size(); i++) {
                                BillLine l = lines.get(i);
                        %>
                        <tr style="background: linear-gradient(135deg, #f0fdf4, #ecfdf5);">
                            <td><strong><%= i+1 %></strong></td>
                            <td><code><%= l.getSku()==null? "—" : l.getSku() %></code></td>
                            <td><strong><%= l.getName()==null? "—" : l.getName() %></strong></td>
                            <td><span style="background: #dbeafe; color: #1e40af; padding: 2px 8px; border-radius: 12px; font-weight: 600;"><%= l.getQuantity() %></span></td>
                            <td><strong>Rs.<%= String.format("%.2f", l.getUnitPrice()) %></strong></td>
                            <td style="color: var(--danger);">-Rs.<%= String.format("%.2f", l.getLineDiscount()) %></td>
                            <td><strong style="color: var(--success); font-size: 1.05em;">Rs.<%= String.format("%.2f", l.getLineTotal()) %></strong></td>
                            <td>
                                <form action="<%=ctx%>/billing" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="removeLine">
                                    <input type="hidden" name="index" value="<%= i %>">
                                    <button class="btn" type="submit" style="color: var(--danger); border-color: #fecaca;">🗑️ Remove</button>
                                </form>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                        </tbody>
                    </table>
                </div>

                <!-- Bill summary: completely hidden when no items -->
                <div class="bill-summary <%= !hasItems ? "hidden" : "" %>">
                    <% if (hasItems) { %>
                    <div style="display:grid;gap:.5rem;">
                        <div style="display:flex;justify-content:space-between; padding: 0.25rem 0;">
                            <span style="font-weight: 500;">Subtotal</span>
                            <strong style="font-size: 1.1em;">Rs.<%= String.format("%.2f", bill.getSubtotal()) %></strong>
                        </div>
                        <div style="display:flex;justify-content:space-between; padding: 0.25rem 0;">
                            <span style="font-weight: 500; color: var(--danger);">Total Discount</span>
                            <strong style="color: var(--danger);">− Rs.<%= String.format("%.2f", bill.getDiscountAmount()) %></strong>
                        </div>
                        <div style="display:flex;justify-content:space-between;color:var(--muted); padding: 0.25rem 0;">
                            <span>Tax (included)</span>
                            <span>Rs.<%= String.format("%.2f", bill.getTaxAmount()) %></span>
                        </div>
                        <hr style="border:none;border-top:2px solid var(--success);margin:.5rem 0;">
                        <div style="display:flex;justify-content:space-between;font-size:1.2em; padding: 0.5rem 0;">
                            <span style="font-weight: 600;">Final Total</span>
                            <strong style="color: var(--success); font-size: 1.1em;">Rs.<%= String.format("%.2f", bill.getTotal()) %></strong>
                        </div>
                    </div>

                    <!-- Adjust + Save aligned to the right -->
                    <div class="row" style="justify-content:flex-end; margin-top:1rem; gap: 1rem;">
                        <form action="<%=ctx%>/billing" method="post" class="row" style="margin-right:auto;">
                            <input type="hidden" name="action" value="adjust">
                            <label>
                                <div class="muted" style="font-size:.85em; font-weight: 500;">💰 Invoice Discount</div>
                                <input class="field" type="number" step="0.01" min="0" name="discount"
                                       value="<%= bill.getDiscountAmount()==null? "0.00" : bill.getDiscountAmount().setScale(2).toPlainString() %>"
                                       style="width:140px;" placeholder="0.00">
                            </label>
                            <label>
                                <div class="muted" style="font-size:.85em; font-weight: 500;">📊 Tax (info only)</div>
                                <input class="field" type="number" step="0.01" min="0" name="tax"
                                       value="<%= bill.getTaxAmount()==null? "0.00" : bill.getTaxAmount().setScale(2).toPlainString() %>"
                                       style="width:140px;" placeholder="0.00">
                            </label>
                            <button class="btn btn-primary" type="submit">🔄 Apply Changes</button>
                        </form>

                        <form action="<%=ctx%>/billing" method="post" class="row">
                            <input type="hidden" name="action" value="save">
                            <label>
                                <div class="muted" style="font-size:.85em; font-weight: 500;">💳 Payment Amount</div>
                                <input class="field" type="number" name="paymentAmount" step="0.01" min="0"
                                       style="width:140px;" placeholder="<%= String.format("%.2f", bill.getTotal()) %>"
                                       value="<%= String.format("%.2f", bill.getTotal()) %>">
                            </label>
                            <label>
                                <div class="muted" style="font-size:.85em; font-weight: 500;">🏦 Payment Method</div>
                                <select class="field" name="paymentMethod" style="min-width: 120px;">
                                    <option value="CASH">💵 Cash</option>
                                    <option value="CARD">💳 Card</option>
                                    <option value="ONLINE">🌐 Online</option>
                                </select>
                            </label>
                            <label>
                                <div class="muted" style="font-size:.85em; font-weight: 500;">🔖 Reference</div>
                                <input class="field" type="text" name="paymentRef" style="width:160px;" placeholder="Optional reference">
                            </label>
                            <button class="btn btn-success" type="submit" style="font-size: 1rem; padding: 0.7rem 1.2rem;">
                                💾 Complete Sale
                            </button>
                        </form>

                        <form action="<%=ctx%>/billing" method="post">
                            <input type="hidden" name="action" value="cancel">
                            <button class="btn" type="submit" style="color: var(--danger); border-color: #fecaca;">❌ Cancel Bill</button>
                        </form>
                    </div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
    (function(){
        // Toggle customer picker visibility
        var toggleBtn = document.getElementById('btn-toggle-cust');
        var picker = document.getElementById('cust-picker');
        var compact = document.getElementById('cust-compact');
        var customerPanel = document.querySelector('.customer-panel');
        var itemsPanel = document.querySelector('.items-panel');

        toggleBtn?.addEventListener('click', function(){
            if (!picker) return;
            var show = (picker.style.display === 'none' || picker.style.display === '');
            picker.style.display = show ? 'block' : 'none';
            if (compact) compact.style.display = show ? 'none' : 'block';

            // Update panel classes for proper sizing
            if (show) {
                customerPanel?.classList.remove('collapsed');
                customerPanel?.classList.add('expanded');
                itemsPanel?.classList.remove('expanded');
                itemsPanel?.classList.add('hidden');

                // Update panel status
                var customerStatus = customerPanel?.querySelector('.panel-status');
                var itemsStatus = itemsPanel?.querySelector('.panel-status');
                if (customerStatus) {
                    customerStatus.className = 'panel-status active';
                    customerStatus.textContent = 'Active';
                }
                if (itemsStatus) {
                    itemsStatus.className = 'panel-status disabled';
                    itemsStatus.textContent = 'Waiting';
                }
            } else {
                customerPanel?.classList.remove('expanded');
                customerPanel?.classList.add('collapsed');
                itemsPanel?.classList.remove('hidden');
                itemsPanel?.classList.add('expanded');

                // Update panel status
                var customerStatus = customerPanel?.querySelector('.panel-status');
                var itemsStatus = itemsPanel?.querySelector('.panel-status');
                if (customerStatus) {
                    customerStatus.className = 'panel-status complete';
                    customerStatus.textContent = 'Selected';
                }
                if (itemsStatus) {
                    itemsStatus.className = 'panel-status active';
                    itemsStatus.textContent = 'Active';
                }
            }
        });

        // Show more customers/items with loading state
        function wireMore(btnId, formId, limitId, step, max) {
            var b = document.getElementById(btnId);
            var f = document.getElementById(formId);
            var l = document.getElementById(limitId);
            if (!b || !f || !l) return;
            b.addEventListener('click', function(){
                // Add loading state
                b.style.opacity = '0.6';
                b.style.pointerEvents = 'none';
                b.textContent = '⏳ Loading...';

                var v = parseInt(l.value || '20', 10);
                l.value = Math.min(max, v + step);
                f.submit();
            });
        }
        wireMore('more-customers', 'form-customer', 'limit-customer', 20, 200);
        wireMore('more-items', 'form-item', 'limit-item', 50, 200);

        // Auto-focus search fields when panels become active
        var custSearch = document.querySelector('#cust-picker input[name="cq"]');
        var itemSearch = document.querySelector('#form-item input[name="iq"]');

        // Focus customer search if no customer selected
        if (custSearch && !custSearch.closest('#cust-picker').style.display) {
            setTimeout(() => custSearch.focus(), 100);
        }

        // Auto-submit forms on Enter key
        [custSearch, itemSearch].forEach(input => {
            if (input) {
                input.addEventListener('keypress', function(e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        this.closest('form').submit();
                    }
                });
            }
        });

        // Enhance quantity inputs with validation
        document.querySelectorAll('input[name="qty"]').forEach(qtyInput => {
            qtyInput.addEventListener('input', function() {
                var max = parseInt(this.getAttribute('max')) || Infinity;
                var val = parseInt(this.value) || 1;

                if (val > max) {
                    this.style.borderColor = 'var(--danger)';
                    this.style.background = '#fef2f2';
                    this.title = 'Quantity exceeds available stock (' + max + ')';
                } else {
                    this.style.borderColor = '';
                    this.style.background = '';
                    this.title = '';
                }
            });
        });

        // Add confirmation for bill cancellation
        document.querySelector('button[type="submit"][formaction*="cancel"]')?.addEventListener('click', function(e) {
            if (!confirm('🚨 Are you sure you want to cancel this bill? All items will be removed.')) {
                e.preventDefault();
            }
        });

        // Animate panel transitions
        function animatePanel(panel, fromClass, toClass) {
            if (!panel) return;
            panel.style.transition = 'all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1)';
            panel.classList.remove(fromClass);
            panel.classList.add(toClass);
        }

        // Auto-scroll to newly added items in bill
        var billLines = document.querySelector('.bill-lines');
        if (billLines && billLines.scrollHeight > billLines.clientHeight) {
            billLines.scrollTop = billLines.scrollHeight;
        }

    })();
    </script>
</div>
