<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.pahanaedu.pahanasuite.models.Customer" %>
<%
    String ctx = request.getContextPath();
    String err = request.getParameter("err");
    String editParam = request.getParameter("edit");
    Integer editId = null;
    try { if (editParam != null) editId = Integer.valueOf(editParam); } catch (Exception ignored) {}

    @SuppressWarnings("unchecked")
    List<Customer> customers = (List<Customer>) request.getAttribute("customers");
    if (customers == null) customers = java.util.Collections.emptyList();

    String role = (String) request.getAttribute("userRole");
    boolean canWrite = role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("manager"));

    Customer editCustomer = null;
    if (editId != null) {
    for (Customer c : customers) { if (c.getId() == editId) { editCustomer = c; break; } }
    }
%>

<section class="section panel-section">
    <header class="panel-head">
        <h2 class="section-title">Customers</h2>
        <div class="quick-actions">
            <% if ("forbidden".equals(err)) { %>
            <span style="color:#b91c1c;background:#fee2e2;border:1px solid #fecaca;padding:.3rem .5rem;border-radius:6px;">
              You don't have permission to modify customers.
            </span>
            <% } else if ("failed".equals(err)) { %>
            <span style="color:#92400e;background:#fef3c7;border:1px solid #fde68a;padding:.3rem .5rem;border-radius:6px;">
              Something went wrong. Try again.
            </span>
            <% } %>
        </div>
    </header>

    <%
        String flash = (String) request.getAttribute("flash");
    %>
    <% if (flash != null && !flash.isBlank()) { %>
    <div class="panel" style="margin-bottom:1rem;background:#ecfdf5;border-color:#a7f3d0;">
        <div style="color:#065f46;"> <%= flash %> </div>
    </div>
    <% } %>

    <% if (canWrite) { %>
    <div class="panel">
        <h3 style="margin:0 0 .5rem 0;">Create Customer</h3>
        <form action="<%=ctx%>/dashboard/customers/actions" method="post"
              style="display:grid;gap:.6rem;grid-template-columns:repeat(6,minmax(0,1fr));align-items:end;">
            <input type="hidden" name="action" value="create">
            <label>
                <div>Account #</div>
                <input name="accountNumber" type="text" required minlength="3" maxlength="20" pattern="[A-Za-z0-9-]+"
                       title="3–20 chars: letters, digits, hyphen"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Name</div>
                <input name="name" type="text" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Address</div>
                <input name="address" type="text"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Telephone</div>
                <input name="telephone" type="text" maxlength="20"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Units Consumed</div>
                <input name="unitsConsumed" type="number" min="0" value="0" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <div>
                <button class="btn btn-accent" type="submit">Create</button>
            </div>
        </form>
    </div>
    <% } %>

    <% if (canWrite && editCustomer != null) { %>
    <div class="panel" style="margin-top:1rem;">
        <h3 style="margin:0 0 .5rem 0;">Edit: <%= editCustomer.getAccountNumber() %> (ID <%= editCustomer.getId() %>)</h3>

        <!-- Update customer -->
        <form action="<%=ctx%>/dashboard/customers/actions" method="post"
              style="display:grid;gap:.6rem;grid-template-columns:repeat(6,minmax(0,1fr));align-items:end;margin-bottom:.75rem;">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= editCustomer.getId() %>">

            <label>
                <div>Account #</div>
                <input name="accountNumber" type="text" required minlength="3" maxlength="20" pattern="[A-Za-z0-9-]+"
                       value="<%= editCustomer.getAccountNumber() %>"
                       title="3–20 chars: letters, digits, hyphen"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Name</div>
                <input name="name" type="text" required value="<%= editCustomer.getName() %>"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Address</div>
                <input name="address" type="text" value="<%= editCustomer.getAddress() %>"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Telephone</div>
                <input name="telephone" type="text" maxlength="20" value="<%= editCustomer.getTelephone() %>"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Units Consumed</div>
                <input name="unitsConsumed" type="number" min="0" required value="<%= editCustomer.getUnitsConsumed() %>"
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>

            <div style="display:flex;gap:.5rem;">
                <button class="btn btn-accent" type="submit">Save</button>
                <a class="btn" href="<%=ctx%>/dashboard/customers">Cancel</a>
            </div>
        </form>
    </div>
    <% } %>

    <div class="panel flex-panel" style="margin-top:1rem;">
        <div class="scroll-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th style="width:56px;">ID</th>
                    <th style="width:160px;">Account #</th>
                    <th>Name</th>
                    <th>Telephone</th>
                    <th style="width:130px;">Units</th>
                    <th style="width:240px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (customers.isEmpty()) {
                %>
                <tr><td colspan="6">No customers found.</td></tr>
                <%
                    } else {
                    for (Customer c : customers) {
                %>
                <tr>
                    <td><%= c.getId() %></td>
                    <td><%= c.getAccountNumber() %></td>
                    <td><%= c.getName() %></td>
                    <td><%= c.getTelephone() == null ? "" : c.getTelephone() %></td>
                    <td><%= c.getUnitsConsumed() %></td>
                    <td>
                        <% if (canWrite) { %>
                        <a class="btn" href="<%=ctx%>/dashboard/customers?edit=<%=c.getId()%>">Edit</a>

                        <form action="<%=ctx%>/dashboard/customers/actions" method="post" style="display:inline"
                              onsubmit="return confirm('Delete customer <%=c.getAccountNumber()%>?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= c.getId() %>">
                            <button class="btn" type="submit"
                                    style="background:var(--danger);color:#fff;border-color:transparent;">Delete</button>
                        </form>
                        <% } else { %>
                        <span style="color:var(--muted);">Read-only</span>
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
