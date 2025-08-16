<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,java.math.BigDecimal,com.pahanaedu.pahanasuite.models.*" %>
<%
    String ctx = request.getContextPath();

    Bill bill = (Bill) request.getAttribute("bill");
    Customer customer = (Customer) request.getAttribute("customer");
    BigDecimal paid = (BigDecimal) request.getAttribute("paid");

    if (bill == null) { response.sendRedirect(ctx + "/dashboard/sales"); return; }
    List<BillLine> lines = bill.getLines() == null ? java.util.Collections.emptyList() : bill.getLines();

    // --- Balance display logic ---
    BigDecimal total      = (bill.getTotal()==null ? BigDecimal.ZERO : bill.getTotal()).setScale(2);
    BigDecimal paidSafe   = (paid==null ? BigDecimal.ZERO : paid).setScale(2);
    BigDecimal rawBalance = total.subtract(paidSafe);     // can be negative if overpaid/refunded
    boolean overpaid      = rawBalance.signum() < 0;
    BigDecimal displayBalance = (overpaid ? rawBalance.abs() : rawBalance).setScale(2);
    String balanceLabel   = overpaid ? "Change Due" : (rawBalance.signum()==0 ? "Settled" : "Balance Due");
    String payTag         = overpaid ? "OVERPAID" : (rawBalance.signum()==0 ? "PAID" : "DUE");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Receipt - <%= bill.getBillNo() %></title>
    <style>
        :root{
            --text:#111827;--muted:#6b7280;--border:#e5e7eb;
        }
        *{box-sizing:border-box}
        body{
            font-family:system-ui,-apple-system,Segoe UI,Roboto,Ubuntu,'Helvetica Neue',Arial,'Noto Sans',sans-serif;
            color:var(--text); margin:0; background:#fff;
        }
        .wrap{max-width:820px;margin:24px auto;padding:16px;}
        .head{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;margin-bottom:16px;}
        .brand h1{margin:0;font-size:20px;}
        .meta{font-size:14px;color:var(--muted)}
        .box{border:1px solid var(--border);border-radius:12px;padding:16px;margin-top:12px;}
        table{width:100%;border-collapse:collapse;}
        th,td{padding:8px;border-bottom:1px solid var(--border);text-align:left;}
        tfoot td{border-bottom:none;}
        .right{text-align:right}
        .muted{color:var(--muted)}
        .actions{display:flex;gap:.5rem;justify-content:flex-end;margin-top:12px;}
        .btn{padding:.5rem .75rem;border:1px solid var(--border);border-radius:8px;background:#f9fafb;cursor:pointer}
        .btn-accent{background:#111827;color:#fff;border-color:#111827}
        .tag{display:inline-block;padding:.15rem .45rem;border:1px solid var(--border);border-radius:999px;font-size:12px;margin-left:6px;}
        @media print {
            .actions{display:none}
            body{background:#fff}
            .wrap{margin:0;padding:0;max-width:none}
            .box{border:none;padding:0;margin:0}
            .brand h1{font-size:18px}
        }
    </style>
</head>
<body>
<div class="wrap">
    <div class="head">
        <div class="brand">
            <h1>PahanaSuite</h1>
            <div class="muted">Receipt / Invoice</div>
        </div>
        <div class="meta">
            <div><strong>Bill No:</strong> <%= bill.getBillNo() %></div>
            <div><strong>Date:</strong> <%= bill.getIssuedAt()==null? "" : bill.getIssuedAt() %></div>
            <div><strong>Status:</strong> <%= bill.getStatus() %> <span class="tag"><%= payTag %></span></div>
        </div>
    </div>

    <div class="box" style="display:flex;gap:24px;flex-wrap:wrap;">
        <div style="min-width:260px;flex:1;">
            <div class="muted" style="margin-bottom:4px;">Billed To</div>
            <div><strong><%= customer==null? "Walk-in" : customer.getName() %></strong></div>
            <div class="muted"><%= customer==null? "" : (customer.getTelephone()==null? "" : customer.getTelephone()) %></div>
            <div style="white-space:pre-line"><%= customer==null? "" : (customer.getAddress()==null? "" : customer.getAddress()) %></div>
        </div>
        <div style="min-width:220px;">
            <div><strong>Subtotal:</strong> Rs.<%= String.format("%.2f", bill.getSubtotal()) %></div>
            <div><strong>Discount:</strong> − Rs.<%= String.format("%.2f", bill.getDiscountAmount()) %></div>
            <div class="muted">Tax (included): Rs.<%= String.format("%.2f", bill.getTaxAmount()) %></div>
            <div style="margin-top:6px;font-size:18px;"><strong>Total: Rs.<%= String.format("%.2f", total) %></strong></div>
        </div>
    </div>

    <div class="box" style="margin-top:16px;">
        <table>
            <thead>
            <tr>
                <th style="width:120px;">SKU</th>
                <th>Item</th>
                <th class="right" style="width:80px;">Qty</th>
                <th class="right" style="width:120px;">Unit</th>
                <th class="right" style="width:120px;">Line Disc.</th>
                <th class="right" style="width:120px;">Line Total</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (lines.isEmpty()) {
            %>
            <tr><td colspan="6" class="muted">No items.</td></tr>
            <%
            } else {
                for (BillLine l : lines) {
            %>
            <tr>
                <td><%= l.getSku()==null? "" : l.getSku() %></td>
                <td><%= l.getName()==null? "" : l.getName() %></td>
                <td class="right"><%= l.getQuantity() %></td>
                <td class="right">Rs.<%= String.format("%.2f", l.getUnitPrice()) %></td>
                <td class="right">Rs.<%= String.format("%.2f", l.getLineDiscount()) %></td>
                <td class="right"><strong>Rs.<%= String.format("%.2f", l.getLineTotal()) %></strong></td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
            <tfoot>
            <tr>
                <td colspan="5" class="right"><strong>Paid to date</strong></td>
                <td class="right"><strong>Rs.<%= String.format("%.2f", paidSafe) %></strong></td>
            </tr>
            <tr>
                <td colspan="5" class="right"><strong><%= balanceLabel %></strong></td>
                <td class="right">
                    <strong>
                        <% if ("Settled".equals(balanceLabel)) { %>
                        Rs.0.00
                        <% } else { %>
                        Rs.<%= String.format("%.2f", displayBalance) %>
                        <% } %>
                    </strong>
                </td>
            </tr>
            </tfoot>
        </table>
    </div>

    <div class="box" style="margin-top:12px;">
        <div class="muted">Thank you for your purchase.</div>
    </div>

    <div class="actions">
        <a class="btn" href="<%= ctx %>/dashboard/sales">Back to Sales</a>
        <button class="btn btn-accent" onclick="window.print()">Print</button>
    </div>
</div>
</body>
</html>
