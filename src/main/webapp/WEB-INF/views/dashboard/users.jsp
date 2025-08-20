<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.pahanaedu.pahanasuite.models.User" %>
<%
    String ctx = request.getContextPath();
    String err = request.getParameter("err");
    String editParam = request.getParameter("edit");
    Integer editId = null;
    try { if (editParam != null) editId = Integer.valueOf(editParam); } catch (Exception ignored) {}

    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) request.getAttribute("users");
    if (users == null) users = java.util.Collections.emptyList();

    String role = (String) request.getAttribute("userRole");
    boolean canWrite = role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("manager"));

    User editUser = null;
    if (editId != null) {
        for (User u : users) { if (u.getId() == editId) { editUser = u; break; } }
    }
%>

<section class="section panel-section">
    <header class="panel-head">
        <h2 class="section-title">Users</h2>
        <div class="quick-actions">
            <input id="userSearch" type="search" placeholder="Search username…"
                   style="padding:.3rem .5rem;border:1px solid var(--border);border-radius:6px;">

            <% if ("forbidden".equals(err)) { %>
            <span style="color:#b91c1c;background:#fee2e2;border:1px solid #fecaca;padding:.3rem .5rem;border-radius:6px;">
          You don't have permission to modify users.
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
        <h3 style="margin:0 0 .5rem 0;">Create User</h3>
        <form action="<%=ctx%>/dashboard/users/actions" method="post"
              style="display:grid;gap:.6rem;grid-template-columns:repeat(4,minmax(0,1fr));align-items:end;">
            <input type="hidden" name="action" value="create">
            <label>
                <div>Username</div>
                <input name="username" type="text" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Password</div>
                <input name="password" type="password" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Role</div>
                <select name="role" required
                        style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                    <option value="admin">Admin</option>
                    <option value="manager">Manager</option>
                    <option value="cashier">Cashier</option>
                </select>
            </label>
            <div>
                <button class="btn btn-accent" type="submit">Create</button>
            </div>
        </form>
    </div>
    <% } %>

    <% if (canWrite && editUser != null) { %>
    <div class="panel" style="margin-top:1rem;">
        <h3 style="margin:0 0 .5rem 0;">Edit: <%= editUser.getUsername() %> (ID <%= editUser.getId() %>)</h3>

        <!-- Update user -->
        <form action="<%=ctx%>/dashboard/users/actions" method="post"
              style="display:grid;gap:.6rem;grid-template-columns:repeat(4,minmax(0,1fr));align-items:end;margin-bottom:.75rem;">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= editUser.getId() %>">

            <label>
                <div>Username</div>
                <input name="username" type="text" value="<%= editUser.getUsername() %>" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>

            <label>
                <div>Role</div>
                <select name="role" required
                        style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                    <option value="admin"   <%= "admin".equalsIgnoreCase(editUser.getRole()) ? "selected" : "" %>>Admin</option>
                    <option value="manager" <%= "manager".equalsIgnoreCase(editUser.getRole()) ? "selected" : "" %>>Manager</option>
                    <option value="cashier" <%= "cashier".equalsIgnoreCase(editUser.getRole()) ? "selected" : "" %>>Cashier</option>
                </select>
            </label>

            <div style="display:flex;gap:.5rem;">
                <button class="btn btn-accent" type="submit">Save</button>
                <a class="btn" href="<%=ctx%>/dashboard/users">Cancel</a>
            </div>
        </form>

        <!-- Reset password (separate form, no nesting) -->
        <form action="<%=ctx%>/dashboard/users/actions" method="post" style="display:flex;gap:.5rem;">
            <input type="hidden" name="action" value="resetPassword">
            <input type="hidden" name="id" value="<%= editUser.getId() %>">
            <input name="password" type="text" placeholder="New password (optional)"
                   style="flex:1;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            <button class="btn" type="submit">Reset PW</button>
        </form>
    </div>
    <% } %>

    <div class="panel flex-panel" style="margin-top:1rem;">
        <div class="scroll-wrap">
            <table class="data-table" id="userTable">
                <thead>
                <tr>
                    <th style="width:56px;">ID</th>
                    <th>Username</th>
                    <th style="width:140px;">Role</th>
                    <th style="width:240px;">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (users.isEmpty()) {
                %>
                <tr><td colspan="4">No users found.</td></tr>
                <%
                } else {
                    for (User u : users) {
                %>
                <tr>
                    <td><%= u.getId() %></td>
                    <td><%= u.getUsername() %></td>
                    <td><%= u.getRole() %></td>
                    <td>
                        <% if (canWrite) { %>
                        <a class="btn" href="<%=ctx%>/dashboard/users?edit=<%=u.getId()%>">Edit</a>

                        <form action="<%=ctx%>/dashboard/users/actions" method="post" style="display:inline">
                            <input type="hidden" name="action" value="resetPassword">
                            <input type="hidden" name="id" value="<%= u.getId() %>">
                            <button class="btn" type="submit">Reset PW</button>
                        </form>

                        <form action="<%=ctx%>/dashboard/users/actions" method="post" style="display:inline"
                              onsubmit="return confirm('Delete <%=u.getUsername()%>?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= u.getId() %>">
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

<script>
document.addEventListener('DOMContentLoaded', function () {
    var search = document.getElementById('userSearch');
    var table = document.getElementById('userTable');
    if (!search || !table) return;
    var rows = Array.from(table.querySelectorAll('tbody tr'));
    search.addEventListener('input', function () {
        var term = this.value.toLowerCase();
        rows.forEach(function (row) {
            var cells = row.getElementsByTagName('td');
            if (cells.length < 2) return;
            var username = cells[1].textContent.toLowerCase();
            row.style.display = username.indexOf(term) !== -1 ? '' : 'none';
        });
    });
});
</script>
