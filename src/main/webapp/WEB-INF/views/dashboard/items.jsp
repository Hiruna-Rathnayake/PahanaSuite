<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.pahanaedu.pahanasuite.models.Item" %>
<%
    String ctx = request.getContextPath();
    String err = request.getParameter("err");

    String q   = request.getParameter("q");
    String cat = request.getParameter("category");

    String editParam = request.getParameter("edit");
    Integer editId = null;
    try { if (editParam != null) editId = Integer.valueOf(editParam); } catch (Exception ignored) {}

    @SuppressWarnings("unchecked")
    List<Item> items = (List<Item>) request.getAttribute("items");
    if (items == null) items = java.util.Collections.emptyList();

    String role = (String) request.getAttribute("userRole");
    boolean canWrite = role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("manager"));

    Item editItem = null;
    if (editId != null) {
        for (Item it : items) { if (it.getId() == editId) { editItem = it; break; } }
    }

    String flash = (String) request.getAttribute("flash");
%>

<!-- Enhanced Styles for better UX -->
<style>
    .items-enhanced .quick-search-wrapper {
        position: relative;
        min-width: 260px;
    }

    .items-enhanced .search-icon {
        position: absolute;
        left: 12px;
        top: 50%;
        transform: translateY(-50%);
        color: #6b7280;
        font-size: 16px;
    }

    .items-enhanced .search-input {
        padding: 0.5rem 0.75rem 0.5rem 2.5rem !important;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        transition: all 0.2s ease;
        font-size: 14px;
    }

    .items-enhanced .search-input:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .items-enhanced .btn-icon {
        display: inline-flex;
        align-items: center;
        gap: 0.375rem;
        padding: 0.5rem 0.75rem;
        font-size: 14px;
        font-weight: 500;
        border-radius: 6px;
        transition: all 0.15s ease;
        text-decoration: none;
        border: 1px solid transparent;
    }

    .items-enhanced .btn-primary {
        background: #3b82f6;
        color: white;
        border-color: #3b82f6;
    }

    .items-enhanced .btn-primary:hover {
        background: #2563eb;
        transform: translateY(-1px);
    }

    .items-enhanced .btn-secondary {
        background: #f8fafc;
        color: #475569;
        border-color: #e2e8f0;
    }

    .items-enhanced .btn-secondary:hover {
        background: #f1f5f9;
    }

    .items-enhanced .form-section {
        background: #ffffff;
        border: 1px solid #e5e7eb;
        border-radius: 12px;
        padding: 1.5rem;
        margin-bottom: 1.5rem;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .items-enhanced .form-section-header {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 1rem;
        padding-bottom: 0.75rem;
        border-bottom: 1px solid #f3f4f6;
    }

    .items-enhanced .section-icon {
        width: 20px;
        height: 20px;
        color: #3b82f6;
    }

    .items-enhanced .form-grid {
        display: grid;
        gap: 1rem;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    }

    .items-enhanced .form-grid-wide {
        grid-column: 1 / -1;
    }

    .items-enhanced .form-group {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
    }

    .items-enhanced .form-label {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        margin-bottom: 0.25rem;
    }

    .items-enhanced .form-input {
        padding: 0.625rem 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        font-size: 14px;
        transition: all 0.15s ease;
        background: #ffffff;
    }

    .items-enhanced .form-input:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .items-enhanced .form-textarea {
        resize: vertical;
        min-height: 80px;
    }

    .items-enhanced .category-attrs {
        margin-top: 1rem;
        padding-top: 1rem;
        border-top: 1px solid #f3f4f6;
    }

    .items-enhanced .attr-section {
        display: none;
        animation: fadeIn 0.2s ease-in;
    }

    .items-enhanced .attr-section.active {
        display: block;
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .items-enhanced .action-buttons {
        display: flex;
        gap: 0.5rem;
        align-items: center;
        flex-wrap: wrap;
    }

    .items-enhanced .stock-controls {
        display: inline-flex;
        border-radius: 6px;
        overflow: hidden;
        border: 1px solid #d1d5db;
    }

    .items-enhanced .stock-btn {
        background: #ffffff;
        border: none;
        padding: 0.375rem 0.5rem;
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        cursor: pointer;
        transition: background 0.15s ease;
        border-right: 1px solid #d1d5db;
    }

    .items-enhanced .stock-btn:last-child {
        border-right: none;
    }

    .items-enhanced .stock-btn:hover {
        background: #f9fafb;
    }

    .items-enhanced .delete-btn {
        background: #dc2626 !important;
        color: white !important;
        border: 1px solid #dc2626 !important;
    }

    .items-enhanced .delete-btn:hover {
        background: #b91c1c !important;
    }

    .items-enhanced .table-wrapper {
        overflow-x: auto;
        border-radius: 8px;
        border: 1px solid #e5e7eb;
    }

    .items-enhanced .enhanced-table {
        width: 100%;
        border-collapse: collapse;
    }

    .items-enhanced .enhanced-table th {
        background: #f8fafc;
        padding: 0.75rem;
        text-align: left;
        font-weight: 600;
        color: #374151;
        font-size: 14px;
        border-bottom: 1px solid #e5e7eb;
    }

    .items-enhanced .enhanced-table td {
        padding: 0.75rem;
        border-bottom: 1px solid #f3f4f6;
        font-size: 14px;
    }

    .items-enhanced .enhanced-table tr:hover {
        background: #f9fafb;
    }

    .items-enhanced .stock-badge {
        display: inline-flex;
        align-items: center;
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
    }

    .items-enhanced .stock-low {
        background: #fef3c7;
        color: #92400e;
    }

    .items-enhanced .stock-out {
        background: #fee2e2;
        color: #b91c1c;
    }

    .items-enhanced .stock-good {
        background: #d1fae5;
        color: #065f46;
    }

    .items-enhanced .collapsible-form {
        max-height: 0;
        overflow: hidden;
        transition: max-height 0.3s ease;
    }

    .items-enhanced .collapsible-form.expanded {
        max-height: 1000px;
    }

    .items-enhanced .toggle-form-btn {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        width: 100%;
        padding: 1rem;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        cursor: pointer;
        font-weight: 500;
        color: #475569;
        margin-bottom: 1rem;
    }

    .items-enhanced .toggle-form-btn:hover {
        background: #f1f5f9;
    }

    .items-enhanced .chevron {
        transition: transform 0.2s ease;
    }

    .items-enhanced .chevron.rotated {
        transform: rotate(180deg);
    }
</style>

<section class="section section-items items-enhanced">
    <header class="panel-head">
        <h2 class="section-title">📦 Items Management</h2>
        <div class="quick-actions" style="gap:.75rem;display:flex;align-items:center;flex-wrap:wrap;">
            <form action="<%=ctx%>/dashboard/items" method="get" style="display:flex;gap:.75rem;align-items:center;">
                <div class="quick-search-wrapper">
                    <span class="search-icon">🔍</span>
                    <input type="search" name="q" value="<%= q==null? "" : q %>"
                           placeholder="Search items..." class="search-input">
                </div>
                <select name="category" class="form-input" style="min-width:140px;">
                    <option value="">All categories</option>
                    <option value="BOOK"       <%= "BOOK".equalsIgnoreCase(cat) ? "selected" : "" %>>📚 Books</option>
                    <option value="STATIONERY" <%= "STATIONERY".equalsIgnoreCase(cat) ? "selected" : "" %>>✏️ Stationery</option>
                    <option value="GIFT"       <%= "GIFT".equalsIgnoreCase(cat) ? "selected" : "" %>>🎁 Gifts</option>
                    <option value="OTHER"      <%= "OTHER".equalsIgnoreCase(cat) ? "selected" : "" %>>📋 Other</option>
                </select>
                <button class="btn-icon btn-primary" type="submit">
                    <span>Filter</span>
                </button>
                <a class="btn-icon btn-secondary" href="<%=ctx%>/dashboard/items">
                    <span>Clear</span>
                </a>
            </form>

            <% if ("forbidden".equals(err)) { %>
            <div style="color:#b91c1c;background:#fee2e2;border:1px solid #fecaca;padding:.5rem .75rem;border-radius:6px;font-size:14px;">
                ⚠️ You don't have permission to modify items.
            </div>
            <% } else if ("failed".equals(err)) { %>
            <div style="color:#92400e;background:#fef3c7;border:1px solid #fde68a;padding:.5rem .75rem;border-radius:6px;font-size:14px;">
                ❌ Something went wrong. Please try again.
            </div>
            <% } %>
        </div>
    </header>

    <% if (flash != null && !flash.isBlank()) { %>
    <div class="form-section" style="background:#ecfdf5;border-color:#a7f3d0;">
        <div style="color:#065f46;display:flex;align-items:center;gap:0.5rem;">
            <span>✅</span>
            <%= flash %>
        </div>
    </div>
    <% } %>

    <!-- Create Item Form -->
    <% if (canWrite) { %>
    <div>
        <button class="toggle-form-btn" onclick="toggleCreateForm()" id="createFormToggle">
            <span>➕ Add New Item</span>
            <span class="chevron" id="createChevron">▼</span>
        </button>

        <div class="collapsible-form" id="createFormContainer">
            <div class="form-section">
                <div class="form-section-header">
                    <span class="section-icon">📝</span>
                    <h3 style="margin:0;">Create New Item</h3>
                </div>

                <form action="<%=ctx%>/dashboard/items/actions" method="post" class="item-form">
                    <input type="hidden" name="action" value="create">

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label">SKU *</label>
                            <input name="sku" type="text" required minlength="2" maxlength="64"
                                   class="form-input" placeholder="e.g., BK001">
                        </div>

                        <div class="form-group" style="grid-column: span 2;">
                            <label class="form-label">Item Name *</label>
                            <input name="name" type="text" required class="form-input"
                                   placeholder="Enter item name">
                        </div>

                        <div class="form-group">
                            <label class="form-label">Category *</label>
                            <select name="category" required class="form-input">
                                <option value="">Select category</option>
                                <option value="BOOK">📚 Book</option>
                                <option value="STATIONERY">✏️ Stationery</option>
                                <option value="GIFT">🎁 Gift</option>
                                <option value="OTHER">📋 Other</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Unit Price *</label>
                            <input name="unitPrice" type="number" step="0.01" min="0" required
                                   class="form-input" placeholder="0.00">
                        </div>

                        <div class="form-group">
                            <label class="form-label">Stock Quantity *</label>
                            <input name="stockQty" type="number" min="0" value="0" required
                                   class="form-input">
                        </div>

                        <div class="form-group form-grid-wide">
                            <label class="form-label">Description</label>
                            <textarea name="description" class="form-input form-textarea"
                                      placeholder="Optional description"></textarea>
                        </div>
                    </div>

                    <!-- Category-specific attributes -->
                    <div class="category-attrs">
                        <div class="attr-sections">
                            <!-- BOOK -->
                            <div class="attr attr-book attr-section">
                                <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                                    📚 <span>Book Details</span>
                                </h4>
                                <div class="form-grid">
                                    <div class="form-group">
                                        <label class="form-label">Author</label>
                                        <input name="attr_author" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">ISBN</label>
                                        <input name="attr_isbn" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Publisher</label>
                                        <input name="attr_publisher" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Year</label>
                                        <input name="attr_year" type="number" min="0" class="form-input">
                                    </div>
                                </div>
                            </div>

                            <!-- STATIONERY -->
                            <div class="attr attr-stationery attr-section">
                                <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                                    ✏️ <span>Stationery Details</span>
                                </h4>
                                <div class="form-grid">
                                    <div class="form-group">
                                        <label class="form-label">Brand</label>
                                        <input name="attr_brand" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Size</label>
                                        <input name="attr_size" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Color</label>
                                        <input name="attr_color" type="text" class="form-input">
                                    </div>
                                </div>
                            </div>

                            <!-- GIFT -->
                            <div class="attr attr-gift attr-section">
                                <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                                    🎁 <span>Gift Details</span>
                                </h4>
                                <div class="form-grid">
                                    <div class="form-group">
                                        <label class="form-label">Occasion</label>
                                        <input name="attr_occasion" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Target Audience</label>
                                        <input name="attr_target" type="text" class="form-input">
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Material</label>
                                        <input name="attr_material" type="text" class="form-input">
                                    </div>
                                </div>
                            </div>

                            <!-- OTHER -->
                            <div class="attr attr-other attr-section">
                                <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                                    📋 <span>Additional Notes</span>
                                </h4>
                                <div class="form-group">
                                    <textarea name="attr_notes" rows="3" class="form-input form-textarea"
                                              placeholder="Add any additional notes or details"></textarea>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div style="margin-top:1.5rem;padding-top:1rem;border-top:1px solid #f3f4f6;">
                        <button class="btn-icon btn-primary" type="submit">
                            <span>✅ Create Item</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <% } %>

    <!-- Edit Item Form -->
    <% if (canWrite && editItem != null) { %>
    <div class="form-section">
        <div class="form-section-header">
            <span class="section-icon">✏️</span>
            <h3 style="margin:0;">Edit Item: <%= editItem.getSku() %> (ID: <%= editItem.getId() %>)</h3>
        </div>

        <form action="<%=ctx%>/dashboard/items/actions" method="post" class="item-form">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= editItem.getId() %>">

            <div class="form-grid">
                <div class="form-group">
                    <label class="form-label">SKU *</label>
                    <input name="sku" type="text" required minlength="2" maxlength="64"
                           value="<%= editItem.getSku() %>" class="form-input">
                </div>

                <div class="form-group" style="grid-column: span 2;">
                    <label class="form-label">Item Name *</label>
                    <input name="name" type="text" required value="<%= editItem.getName() %>"
                           class="form-input">
                </div>

                <div class="form-group">
                    <label class="form-label">Category *</label>
                    <select name="category" required class="form-input">
                        <option value="BOOK"       <%= "BOOK".equalsIgnoreCase(editItem.getCategory()) ? "selected" : "" %>>📚 Book</option>
                        <option value="STATIONERY" <%= "STATIONERY".equalsIgnoreCase(editItem.getCategory()) ? "selected" : "" %>>✏️ Stationery</option>
                        <option value="GIFT"       <%= "GIFT".equalsIgnoreCase(editItem.getCategory()) ? "selected" : "" %>>🎁 Gift</option>
                        <option value="OTHER"      <%= "OTHER".equalsIgnoreCase(editItem.getCategory()) ? "selected" : "" %>>📋 Other</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label">Unit Price *</label>
                    <input name="unitPrice" type="number" step="0.01" min="0" required
                           value="<%= editItem.getUnitPrice()==null? "" : editItem.getUnitPrice() %>"
                           class="form-input">
                </div>

                <div class="form-group">
                    <label class="form-label">Stock Quantity *</label>
                    <input name="stockQty" type="number" min="0" required
                           value="<%= editItem.getStockQty() %>" class="form-input">
                </div>

                <div class="form-group form-grid-wide">
                    <label class="form-label">Description</label>
                    <textarea name="description" class="form-input form-textarea"><%= editItem.getDescription()==null? "" : editItem.getDescription() %></textarea>
                </div>
            </div>

            <!-- Category-specific attributes (prefilled) -->
            <div class="category-attrs">
                <div class="attr-sections">
                    <!-- BOOK -->
                    <div class="attr attr-book attr-section">
                        <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                            📚 <span>Book Details</span>
                        </h4>
                        <div class="form-grid">
                            <div class="form-group">
                                <label class="form-label">Author</label>
                                <input name="attr_author" type="text" value="<%= editItem.getAttrString("author") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">ISBN</label>
                                <input name="attr_isbn" type="text" value="<%= editItem.getAttrString("isbn") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Publisher</label>
                                <input name="attr_publisher" type="text" value="<%= editItem.getAttrString("publisher") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Year</label>
                                <input name="attr_year" type="number" min="0" value="<%= editItem.getAttrString("year") %>" class="form-input">
                            </div>
                        </div>
                    </div>

                    <!-- STATIONERY -->
                    <div class="attr attr-stationery attr-section">
                        <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                            ✏️ <span>Stationery Details</span>
                        </h4>
                        <div class="form-grid">
                            <div class="form-group">
                                <label class="form-label">Brand</label>
                                <input name="attr_brand" type="text" value="<%= editItem.getAttrString("brand") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Size</label>
                                <input name="attr_size" type="text" value="<%= editItem.getAttrString("size") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Color</label>
                                <input name="attr_color" type="text" value="<%= editItem.getAttrString("color") %>" class="form-input">
                            </div>
                        </div>
                    </div>

                    <!-- GIFT -->
                    <div class="attr attr-gift attr-section">
                        <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                            🎁 <span>Gift Details</span>
                        </h4>
                        <div class="form-grid">
                            <div class="form-group">
                                <label class="form-label">Occasion</label>
                                <input name="attr_occasion" type="text" value="<%= editItem.getAttrString("occasion") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Target Audience</label>
                                <input name="attr_target" type="text" value="<%= editItem.getAttrString("target") %>" class="form-input">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Material</label>
                                <input name="attr_material" type="text" value="<%= editItem.getAttrString("material") %>" class="form-input">
                            </div>
                        </div>
                    </div>

                    <!-- OTHER -->
                    <div class="attr attr-other attr-section">
                        <h4 style="margin:0 0 1rem 0;display:flex;align-items:center;gap:0.5rem;">
                            📋 <span>Additional Notes</span>
                        </h4>
                        <div class="form-group">
                            <textarea name="attr_notes" rows="3" class="form-input form-textarea"><%= editItem.getAttrString("notes") %></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <div style="margin-top:1.5rem;padding-top:1rem;border-top:1px solid #f3f4f6;">
                <div class="action-buttons">
                    <button class="btn-icon btn-primary" type="submit">
                        <span>💾 Save Changes</span>
                    </button>
                    <a class="btn-icon btn-secondary" href="<%=ctx%>/dashboard/items">
                        <span>❌ Cancel</span>
                    </a>
                </div>
            </div>
        </form>
    </div>
    <% } %>

    <!-- Items Table -->
    <div class="form-section">
        <div class="form-section-header">
            <span class="section-icon">📋</span>
            <h3 style="margin:0;">Items List (<%= items.size() %> items)</h3>
        </div>

        <div class="table-wrapper">
            <table class="enhanced-table">
                <thead>
                <tr>
                    <th style="width:60px;">ID</th>
                    <th style="width:120px;">SKU</th>
                    <th>Name</th>
                    <th style="width:120px;">Category</th>
                    <th>Details</th>
                    <th style="width:100px;">Price</th>
                    <th style="width:100px;">Stock</th>
                    <th style="width:240px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (items.isEmpty()) {
                %>
                <tr>
                    <td colspan="8" style="text-align:center;color:#6b7280;padding:2rem;">
                        📦 No items found. <% if (canWrite) { %>Click "Add New Item" to get started.<% } %>
                    </td>
                </tr>
                <%
                } else {
                    for (Item it : items) {
                        String details;
                        if ("BOOK".equalsIgnoreCase(it.getCategory())) {
                            String author = it.getAttrString("author");
                            String isbn   = it.getAttrString("isbn");
                            details = (author == null ? "" : author)
                                    + (isbn == null || isbn.isEmpty() ? "" : (" • ISBN " + isbn));
                        } else if ("STATIONERY".equalsIgnoreCase(it.getCategory())) {
                            String brand = it.getAttrString("brand");
                            String size  = it.getAttrString("size");
                            details = (brand == null ? "" : brand)
                                    + (size == null || size.isEmpty() ? "" : (" • " + size));
                        } else if ("GIFT".equalsIgnoreCase(it.getCategory())) {
                            String occ = it.getAttrString("occasion");
                            String mat = it.getAttrString("material");
                            details = (occ == null ? "" : occ)
                                    + (mat == null || mat.isEmpty() ? "" : (" • " + mat));
                        } else {
                            details = it.getAttrString("notes");
                        }

                        // Stock status
                        int stock = it.getStockQty();
                        String stockClass = stock == 0 ? "stock-out" : (stock < 10 ? "stock-low" : "stock-good");
                        String stockIcon = stock == 0 ? "🔴" : (stock < 10 ? "🟡" : "🟢");
                %>
                <tr>
                    <td><%= it.getId() %></td>
                    <td><strong><%= it.getSku() %></strong></td>
                    <td><%= it.getName() %></td>
                    <td>
                        <% if ("BOOK".equalsIgnoreCase(it.getCategory())) { %>
                        📚 Book
                        <% } else if ("STATIONERY".equalsIgnoreCase(it.getCategory())) { %>
                        ✏️ Stationery
                        <% } else if ("GIFT".equalsIgnoreCase(it.getCategory())) { %>
                        🎁 Gift
                        <% } else { %>
                        📋 Other
                        <% } %>
                    </td>
                    <td style="color:#6b7280;font-size:13px;"><%= details == null || details.isEmpty() ? "—" : details %></td>
                    <td><strong>$<%= it.getUnitPrice()==null? "0.00" : String.format("%.2f", it.getUnitPrice()) %></strong></td>
                    <td>
                        <span class="stock-badge <%= stockClass %>">
                            <%= stockIcon %> <%= it.getStockQty() %>
                        </span>
                    </td>
                    <td>
                        <% if (canWrite) { %>
                        <div class="action-buttons">
                            <a class="btn-icon btn-secondary" href="<%=ctx%>/dashboard/items?edit=<%=it.getId()%><%= (q!=null? "&q="+q : "") %><%= (cat!=null? "&category="+cat : "") %>" title="Edit item">
                                <span>✏️</span>
                            </a>

                            <div class="stock-controls">
                                <form action="<%=ctx%>/dashboard/items/actions" method="post" style="display:inline">
                                    <input type="hidden" name="action" value="adjustStock">
                                    <input type="hidden" name="id" value="<%= it.getId() %>">
                                    <input type="hidden" name="delta" value="-1">
                                    <button class="stock-btn" type="submit" title="Decrease stock">−</button>
                                </form>
                                <form action="<%=ctx%>/dashboard/items/actions" method="post" style="display:inline">
                                    <input type="hidden" name="action" value="adjustStock">
                                    <input type="hidden" name="id" value="<%= it.getId() %>">
                                    <input type="hidden" name="delta" value="1">
                                    <button class="stock-btn" type="submit" title="Increase stock">+</button>
                                </form>
                            </div>

                            <form action="<%=ctx%>/dashboard/items/actions" method="post" style="display:inline"
                                  onsubmit="return confirm('Are you sure you want to delete <%=it.getSku()%>? This action cannot be undone.');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="<%= it.getId() %>">
                                <button class="btn-icon delete-btn" type="submit" title="Delete item">
                                    <span>🗑️</span>
                                </button>
                            </form>
                        </div>
                        <% } else { %>
                        <span style="color:#9ca3af;font-size:13px;">👁️ Read-only</span>
                        <% } %>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</section>

<script>
    (function(){
        // Toggle attribute sections per form
        function toggleAttrs(selectEl, sectionsBox){
            var val = (selectEl.value || '').toUpperCase();
            sectionsBox.querySelectorAll('.attr-section').forEach(function(el){
                el.classList.remove('active');
            });

            var targetSection = null;
            if (val === 'BOOK') targetSection = sectionsBox.querySelector('.attr-book');
            else if (val === 'STATIONERY') targetSection = sectionsBox.querySelector('.attr-stationery');
            else if (val === 'GIFT') targetSection = sectionsBox.querySelector('.attr-gift');
            else targetSection = sectionsBox.querySelector('.attr-other');

            if (targetSection) {
                targetSection.classList.add('active');
            }
        }

        // Initialize form handlers
        document.querySelectorAll('form.item-form').forEach(function(form){
            var sel = form.querySelector('select[name="category"]');
            var box = form.querySelector('.attr-sections');
            if (sel && box) {
                sel.addEventListener('change', function(){ toggleAttrs(sel, box); });
                toggleAttrs(sel, box); // initial
            }
        });

        // Auto-focus search on page load
        var searchInput = document.querySelector('.search-input');
        if (searchInput && !searchInput.value) {
            searchInput.focus();
        }

        // Enhanced form validation feedback
        document.querySelectorAll('form.item-form').forEach(function(form) {
            form.addEventListener('submit', function(e) {
                var requiredFields = form.querySelectorAll('[required]');
                var valid = true;

                requiredFields.forEach(function(field) {
                    field.style.borderColor = '';
                    if (!field.value.trim()) {
                        field.style.borderColor = '#dc2626';
                        valid = false;
                    }
                });

                if (!valid) {
                    e.preventDefault();
                    alert('Please fill in all required fields (marked with *)');
                }
            });
        });
    })();

    // Toggle create form function
    function toggleCreateForm() {
        var container = document.getElementById('createFormContainer');
        var chevron = document.getElementById('createChevron');
        var isExpanded = container.classList.contains('expanded');

        if (isExpanded) {
            container.classList.remove('expanded');
            chevron.classList.remove('rotated');
        } else {
            container.classList.add('expanded');
            chevron.classList.add('rotated');
            // Focus first input when opening
            setTimeout(function() {
                var firstInput = container.querySelector('input[name="sku"]');
                if (firstInput) firstInput.focus();
            }, 300);
        }
    }

    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            var searchInput = document.querySelector('.search-input');
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }

        // Ctrl/Cmd + N to toggle new item form
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
            e.preventDefault();
            var toggleBtn = document.getElementById('createFormToggle');
            if (toggleBtn) toggleCreateForm();
        }
    });
</script>