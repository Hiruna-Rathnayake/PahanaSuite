<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="section panel-section help-section">
    <header class="panel-head">
        <h2 class="section-title">Help &amp; Documentation</h2>
    </header>
    <div class="panel-body">
        <h3>1. Functionality Overview</h3>

        <h4>1.1 Signing In</h4>
        <ul>
            <li><strong>Login:</strong> Enter your credentials to access the system.</li>
            <li><strong>Session Timeout:</strong> Inactive sessions close automatically; sign in again to continue.</li>
            <li><strong>Logout:</strong> Use the logout button when you finish your work.</li>

        </ul>
        <h4>1.2 Roles</h4>
        <table class="data-table">
            <thead>
            <tr><th>Role</th><th>Privileges</th></tr>
            </thead>
            <tbody>
            <tr><td>Admin</td><td>Full access to all modules: users, items, customers, bills, reports, sales.</td></tr>
            <tr><td>Manager</td><td>Same as admin except restricted system settings (if any).</td></tr>
            <tr><td>Cashier</td><td>Sales operations, bill viewing, customer lookup. No write access to items or users.</td></tr>
            </tbody>
        </table>
        <h4>1.3 Dashboard Sections</h4>

        <p>The dashboard groups functionality into sections such as <em>overview</em>, <em>sales</em>, <em>customers</em>, <em>items</em>, <em>bills</em>, <em>users</em>, and <em>reports</em>. Section availability and the default landing page depend on your role.</p>
        <h4>1.4 Customers</h4>
        <ul>
            <li>List, create, update, and delete customers.</li>
            <li>Account numbers must be unique; units consumed cannot be negative.</li>
            <li>Only admins and managers may modify customer data.</li>
        </ul>
        <h4>1.5 Items</h4>
        <ul>
            <li>Search items by name or SKU and filter by category.</li>

            <li>Create, edit, delete, and adjust stock; low stock features highlight items below threshold.</li>
            <li>Item SKUs must remain unique; stock cannot go below zero.</li>
            <li>Only admins and managers may modify items.</li>
        </ul>

        <h4>1.6 Users</h4>

        <ul>
            <li>List users, create accounts, update roles, reset passwords, or delete users.</li>
            <li>Only admins and managers may access this module.</li>
        </ul>

        <h4>1.7 Bills</h4>
        <ul>
            <li>View bill headers and details, delete bills, mark as paid, or record refunds.</li>
            <li>Write actions are restricted to admins and managers.</li>

        </ul>
        <h4>1.8 Billing &amp; Sales</h4>
        <ul>
            <li>Create bills, add catalog or custom line items, merge quantities, and validate stock.</li>

            <li>Apply invoice‑level discounts and tax for reference.</li>
            <li>Save bills to persist data, adjust stock, update customer units, and process payments.</li>
            <li>Canceling a bill discards the current draft.</li>
            <li>A receipt is displayed after saving and can be reprinted later.</li>
        </ul>
        <h4>1.9 Reports</h4>
        <p>Generate daily or monthly summaries showing bill counts and totals for the selected period.</p>


        <h3>2. User Guide</h3>
        <h4>2.1 Getting Started</h4>
        <ol>

            <li>Go to the login page.</li>
            <li>Enter your username and password.</li>
            <li>Upon success, you are taken to the dashboard section appropriate for your role.</li>
        </ol>
        <h4>2.2 Navigation</h4>
        <p>Use the top navigation bar to switch between sections. Admins and managers land on the overview section; cashiers land on sales.</p>
        <h4>2.3 Managing Customers</h4>
        <ol>
            <li>Open the <em>Customers</em> section.</li>
            <li>Use <em>New Customer</em> to add or the edit/delete actions to modify existing records.</li>
        </ol>
        <h4>2.4 Managing Items</h4>
        <ol>

            <li>Open the <em>Items</em> section.</li>

            <li>Search or filter items, create new entries, adjust stock, or delete as needed.</li>
        </ol>
        <h4>2.5 User Administration</h4>
        <p>Admins and managers can create users, change roles, reset passwords, or delete accounts from the <em>Users</em> section.</p>
        <h4>2.6 Billing &amp; Sales</h4>
        <ol>
            <li>Open the <em>Sales</em> section and select a customer.</li>
            <li>Add catalog or custom items. Quantities merge automatically for identical items.</li>
            <li>Apply discount or tax, then save the bill and record any payment.</li>
            <li>Print or revisit the receipt as required.</li>
        </ol>
        <h4>2.7 Bills Listing</h4>
        <p>The <em>Bills</em> section lists all bills with actions to mark paid, refund, or delete (admin/manager only).</p>
        <h4>2.8 Reports</h4>

        <p>Use the <em>Reports</em> section to view daily or monthly totals.</p>
        <h4>2.9 Logout</h4>
        <p>Click the <em>Logout</em> button in the header to end your session.</p>


        <h3>3. Frequently Asked Questions</h3>
        <dl>
            <dt>How do I reset a forgotten password?</dt>

            <dd>Contact an admin or manager; they can reset your password and provide a temporary one if needed.</dd>
            <dt>Why am I getting permission errors?</dt>
            <dd>Your role lacks permission for that action. Only admins and managers can modify users, items, or customers.</dd>
            <dt>Why was I logged out automatically?</dt>
            <dd>The system signs out users after a period of inactivity. Log in again to continue.</dd>

            <dt>Can I change a bill after saving?</dt>
            <dd>Only bill status and totals can be adjusted. Use the <em>Bills</em> section to delete or refund if necessary.</dd>
            <dt>How do I record a refund?</dt>
            <dd>Open the bill in the <em>Bills</em> section and choose <em>Refund</em>, entering the amount and reference.</dd>
            <dt>Why can’t I add more quantity of an item?</dt>
            <dd>Available stock is insufficient. Adjust inventory in the <em>Items</em> section or reduce the quantity.</dd>

            <dt>What does "units consumed" mean?</dt>
            <dd>It tracks total item quantities purchased by the customer; it updates when bills are saved.</dd>
            <dt>How do I retrieve a receipt?</dt>
            <dd>A receipt is shown after saving a bill and can later be reprinted from the <em>Bills</em> section.</dd>
            <dt>Where are tax values applied?</dt>
            <dd>Tax is informational; the total is calculated as <code>subtotal - discount</code>.</dd>
            <dt>Can I filter reports by custom range?</dt>
            <dd>Currently only daily or monthly presets are supported.</dd>

        </dl>

        <h3>4. Support</h3>
        <p>If you encounter issues not covered here, contact your system administrator or IT support with the steps that led to the problem and any error messages.</p>
    </div>
</section>
