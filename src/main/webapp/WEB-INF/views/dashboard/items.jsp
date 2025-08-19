<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.pahanaedu.pahanasuite.models.Item" %>
<%
    String ctx = request.getContextPath();
    String err = request.getParameter("err");
    String q   = request.getParameter("q");
    String cat = request.getParameter("category");

    @SuppressWarnings("unchecked")
    List<Item> items = (List<Item>) request.getAttribute("items");
    if (items == null) items = java.util.Collections.emptyList();

    String role = (String) request.getAttribute("userRole");
    boolean canWrite = role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("manager"));

    String flash = (String) request.getAttribute("flash");
%>

<section class="section panel-section">
    <header class="panel-head">
        <h2 class="section-title">Items</h2>
        <div class="quick-actions" style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap;">
            <form action="<%=ctx%>/dashboard/items" method="get" style="display:flex;gap:.5rem;align-items:center;">
                <input type="search" name="q" value="<%= q==null? "" : q %>" placeholder="Search by name or SKU…"
                       style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;min-width:220px;">
                <select name="category"
                        style="padding:.45rem .6rem;border:1px solid var(--border);border-radius:8px;">
                    <option value="">All categories</option>
                    <option value="BOOK"       <%= "BOOK".equalsIgnoreCase(cat) ? "selected" : "" %>>Book</option>
                    <option value="STATIONERY" <%= "STATIONERY".equalsIgnoreCase(cat) ? "selected" : "" %>>Stationery</option>
                    <option value="GIFT"       <%= "GIFT".equalsIgnoreCase(cat) ? "selected" : "" %>>Gift</option>
                    <option value="OTHER"      <%= "OTHER".equalsIgnoreCase(cat) ? "selected" : "" %>>Other</option>
                </select>
                <button class="btn" type="submit">Filter</button>
                <a class="btn" href="<%=ctx%>/dashboard/items">Reset</a>
            </form>

            <% if (canWrite) { %>
            <button class="btn btn-accent" id="btn-open-create">New Item</button>
            <% } %>

            <% if ("forbidden".equals(err)) { %>
            <span style="color:#b91c1c;background:#fee2e2;border:1px solid #fecaca;padding:.3rem .5rem;border-radius:6px;">
              You don't have permission to modify items.
            </span>
            <% } else if ("failed".equals(err)) { %>
            <span style="color:#92400e;background:#fef3c7;border:1px solid #fde68a;padding:.3rem .5rem;border-radius:6px;">
              Something went wrong. Try again.
            </span>
            <% } %>
        </div>
    </header>

    <% if (flash != null && !flash.isBlank()) { %>
    <div class="panel" style="margin-bottom:1rem;">
        <div style="color:#065f46;background:#ecfdf5;border:1px solid #a7f3d0;padding:.5rem .75rem;border-radius:8px;">
            <%= flash %>
        </div>
    </div>
    <% } %>

    <div class="panel flex-panel" style="margin-top:1rem;">
        <div class="scroll-wrap">
            <table class="data-table">
            <thead>
            <tr>
                <th style="width:56px;">ID</th>
                <th style="width:140px;">SKU</th>
                <th>Name</th>
                <th style="width:140px;">Category</th>
                <th>Details</th>
                <th style="width:120px;">Price</th>
                <th style="width:120px;">Stock</th>
                <th style="width:280px;">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (items.isEmpty()) {
            %>
            <tr><td colspan="8">No items found.</td></tr>
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
            %>
            <tr>
                <td><%= it.getId() %></td>
                <td><%= it.getSku() %></td>
                <td><%= it.getName() %></td>
                <td><%= it.getCategory() %></td>
                <td style="color:var(--muted);font-size:.92em;"><%= (details==null||details.isEmpty()) ? "—" : details %></td>
                <td>Rs.<%= it.getUnitPrice()==null? "0.00" : String.format("%.2f", it.getUnitPrice()) %></td>
                <td><%= it.getStockQty() %></td>
                <td>
                    <% if (canWrite) { %>
                    <button
                            class="btn js-edit"
                            type="button"
                            data-id="<%= it.getId() %>"
                            data-sku="<%= it.getSku() %>"
                            data-name="<%= it.getName() %>"
                            data-category="<%= it.getCategory() %>"
                            data-price="<%= it.getUnitPrice()==null? "" : it.getUnitPrice() %>"
                            data-stock="<%= it.getStockQty() %>"
                            data-description="<%= it.getDescription()==null? "" : it.getDescription() %>"
                            data-author="<%= it.getAttrString("author") %>"
                            data-isbn="<%= it.getAttrString("isbn") %>"
                            data-publisher="<%= it.getAttrString("publisher") %>"
                            data-year="<%= it.getAttrString("year") %>"
                            data-brand="<%= it.getAttrString("brand") %>"
                            data-size="<%= it.getAttrString("size") %>"
                            data-color="<%= it.getAttrString("color") %>"
                            data-occasion="<%= it.getAttrString("occasion") %>"
                            data-target="<%= it.getAttrString("target") %>"
                            data-material="<%= it.getAttrString("material") %>"
                            data-notes="<%= it.getAttrString("notes") %>">Edit</button>

                    <form action="<%=ctx%>/dashboard/items/actions" method="post" style="display:inline">
                        <input type="hidden" name="action" value="adjustStock">
                        <input type="hidden" name="id" value="<%= it.getId() %>">
                        <input type="hidden" name="delta" value="-1">
                        <button class="btn" type="submit" title="Decrease stock">−1</button>
                    </form>

                    <form action="<%=ctx%>/dashboard/items/actions" method="post" style="display:inline">
                        <input type="hidden" name="action" value="adjustStock">
                        <input type="hidden" name="id" value="<%= it.getId() %>">
                        <input type="hidden" name="delta" value="1">
                        <button class="btn" type="submit" title="Increase stock">+1</button>
                    </form>

                    <button class="btn" style="background:var(--danger);color:#fff;border-color:transparent;"
                            type="button" data-id="<%= it.getId() %>" data-label="<%= it.getSku() %>" class="js-delete">Delete</button>
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

<!-- Create/Edit dialog -->
<dialog id="item-dialog" style="border:none;border-radius:12px;box-shadow:var(--shadow);padding:0;max-width:720px;width:92%;">
    <form action="<%=ctx%>/dashboard/items/actions" method="post" id="item-form" style="padding:1rem;display:grid;gap:.75rem;">
        <div style="display:flex;justify-content:space-between;align-items:center;">
            <h3 id="item-dialog-title" style="margin:.25rem 0 0 0;">New Item</h3>
            <button type="button" class="btn" id="btn-cancel-item">Close</button>
        </div>
        <input type="hidden" name="action" id="form-action" value="create">
        <input type="hidden" name="id" id="item-id">

        <label>
            <div>SKU *</div>
            <input name="sku" id="f-sku" type="text" required
                   style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
        </label>

        <label>
            <div>Name *</div>
            <input name="name" id="f-name" type="text" required
                   style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
        </label>

        <label>
            <div>Category *</div>
            <select name="category" id="f-category" required
                    style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
                <option value="BOOK">Book</option>
                <option value="STATIONERY">Stationery</option>
                <option value="GIFT">Gift</option>
                <option value="OTHER">Other</option>
            </select>
        </label>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;">
            <label>
                <div>Unit Price *</div>
                <input name="unitPrice" id="f-price" type="number" step="0.01" min="0" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
            <label>
                <div>Stock *</div>
                <input name="stockQty" id="f-stock" type="number" min="0" value="0" required
                       style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;">
            </label>
        </div>

        <label>
            <div>Description</div>
            <textarea name="description" id="f-desc" rows="3"
                      style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></textarea>
        </label>

        <!-- Category-specific fields -->
        <div id="sec-book" class="attr-section">
            <h4 style="margin:.5rem 0 0 0;">Book details</h4>
            <div style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.75rem;">
                <label>Author<input name="attr_author" id="f-author" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>ISBN<input name="attr_isbn" id="f-isbn" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>Publisher<input name="attr_publisher" id="f-publisher" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>Year<input name="attr_year" id="f-year" type="number" min="0" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
            </div>
        </div>

        <div id="sec-stationery" class="attr-section">
            <h4 style="margin:.5rem 0 0 0;">Stationery details</h4>
            <div style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:.75rem;">
                <label>Brand<input name="attr_brand" id="f-brand" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>Size<input name="attr_size" id="f-size" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>Color<input name="attr_color" id="f-color" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
            </div>
        </div>

        <div id="sec-gift" class="attr-section">
            <h4 style="margin:.5rem 0 0 0;">Gift details</h4>
            <div style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:.75rem;">
                <label>Occasion<input name="attr_occasion" id="f-occasion" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>Target<input name="attr_target" id="f-target" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
                <label>Material<input name="attr_material" id="f-material" type="text" style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></label>
            </div>
        </div>

        <div id="sec-other" class="attr-section">
            <h4 style="margin:.5rem 0 0 0;">Other</h4>
            <textarea name="attr_notes" id="f-notes" rows="3"
                      style="width:100%;padding:.5rem;border:1px solid var(--border);border-radius:8px;"></textarea>
        </div>

        <div style="display:flex;justify-content:flex-end;gap:.5rem;margin-top:.5rem;">
            <button type="button" class="btn" id="btn-close-item">Cancel</button>
            <button type="submit" class="btn btn-accent">Save</button>
        </div>
    </form>
</dialog>

<!-- Delete confirm -->
<dialog id="delete-dialog" style="border:none;border-radius:12px;box-shadow:var(--shadow);padding:0;max-width:420px;width:92%;">
    <form action="<%=ctx%>/dashboard/items/actions" method="post" style="padding:1rem;display:grid;gap:.75rem;">
        <h3 style="margin:.25rem 0 0 0;">Delete item?</h3>
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" id="del-id">
        <p id="del-text" style="margin:0;color:var(--muted)">This action cannot be undone.</p>
        <div style="display:flex;justify-content:flex-end;gap:.5rem;">
            <button type="button" class="btn" id="btn-cancel-del">Cancel</button>
            <button class="btn" style="background:var(--danger);color:#fff;border-color:transparent;">Delete</button>
        </div>
    </form>
</dialog>

<script>
    (function(){
        var canWrite = <%= canWrite ? "true" : "false" %>;
        if (!canWrite) return;

        function $(s,root){ return (root||document).querySelector(s); }
        function showAttrs(cat){
            ['book','stationery','gift','other'].forEach(function(k){
                var el = $('#sec-'+k);
                if (!el) return;
                if ((cat||'').toUpperCase()===k.toUpperCase()) el.style.display='block';
                else el.style.display='none';
            });
        }

        var dlg = $('#item-dialog');
        var form = $('#item-form');
        var title = $('#item-dialog-title');
        var fAction = $('#form-action');
        var fId = $('#item-id');
        var fSku = $('#f-sku');
        var fName = $('#f-name');
        var fCat = $('#f-category');
        var fPrice = $('#f-price');
        var fStock = $('#f-stock');
        var fDesc = $('#f-desc');

        var fAuthor = $('#f-author'), fIsbn = $('#f-isbn'), fPublisher = $('#f-publisher'), fYear = $('#f-year');
        var fBrand = $('#f-brand'), fSize = $('#f-size'), fColor = $('#f-color');
        var fOccasion = $('#f-occasion'), fTarget = $('#f-target'), fMaterial = $('#f-material');
        var fNotes = $('#f-notes');

        function resetAttrs(){
            [fAuthor,fIsbn,fPublisher,fYear,fBrand,fSize,fColor,fOccasion,fTarget,fMaterial,fNotes].forEach(function(el){ if(el) el.value=''; });
        }

        function openCreate(){
            form.reset();
            resetAttrs();
            fId.value = '';
            fAction.value = 'create';
            title.textContent = 'New Item';
            showAttrs(fCat.value);
            if (dlg.showModal) dlg.showModal(); else dlg.setAttribute('open','');
            setTimeout(function(){ fSku.focus(); }, 30);
        }

        function openEdit(btn){
            form.reset();
            resetAttrs();
            fAction.value = 'update';
            fId.value = btn.dataset.id || '';
            fSku.value = btn.dataset.sku || '';
            fName.value = btn.dataset.name || '';
            fCat.value  = (btn.dataset.category || '').toUpperCase();
            fPrice.value= btn.dataset.price || '';
            fStock.value= btn.dataset.stock || '0';
            fDesc.value = btn.dataset.description || '';
            if (fAuthor)    fAuthor.value    = btn.dataset.author || '';
            if (fIsbn)      fIsbn.value      = btn.dataset.isbn || '';
            if (fPublisher) fPublisher.value = btn.dataset.publisher || '';
            if (fYear)      fYear.value      = btn.dataset.year || '';
            if (fBrand)     fBrand.value     = btn.dataset.brand || '';
            if (fSize)      fSize.value      = btn.dataset.size || '';
            if (fColor)     fColor.value     = btn.dataset.color || '';
            if (fOccasion)  fOccasion.value  = btn.dataset.occasion || '';
            if (fTarget)    fTarget.value    = btn.dataset.target || '';
            if (fMaterial)  fMaterial.value  = btn.dataset.material || '';
            if (fNotes)     fNotes.value     = btn.dataset.notes || '';
            title.textContent = 'Edit Item';
            showAttrs(fCat.value);
            if (dlg.showModal) dlg.showModal(); else dlg.setAttribute('open','');
            setTimeout(function(){ fName.focus(); }, 30);
        }

        var openBtn = document.getElementById('btn-open-create');
        if (openBtn) openBtn.addEventListener('click', openCreate);
        document.querySelectorAll('.js-edit').forEach(function(b){ b.addEventListener('click', function(){ openEdit(b); }); });

        // delete
        var delDlg = document.getElementById('delete-dialog');
        var delId  = document.getElementById('del-id');
        var delTxt = document.getElementById('del-text');
        document.querySelectorAll('.js-delete').forEach(function(b){
            b.addEventListener('click', function(){
                delId.value = b.getAttribute('data-id');
                delTxt.textContent = 'Delete item "'+(b.getAttribute('data-label')||'')+'"? This cannot be undone.';
                if (delDlg.showModal) delDlg.showModal(); else delDlg.setAttribute('open','');
            });
        });

        // close buttons
        document.getElementById('btn-cancel-item')?.addEventListener('click', function(){ dlg.close(); });
        document.getElementById('btn-close-item')?.addEventListener('click', function(){ dlg.close(); });
        document.getElementById('btn-cancel-del')?.addEventListener('click', function(){ delDlg.close(); });

        // category switch
        fCat?.addEventListener('change', function(){ showAttrs(fCat.value); });

        // simple required check
        form.addEventListener('submit', function(e){
            var ok = fSku.value.trim() && fName.value.trim() && fCat.value.trim() && fPrice.value && fStock.value;
            if (!ok) { e.preventDefault(); alert('Please fill the required fields.'); }
        });
    })();
</script>
