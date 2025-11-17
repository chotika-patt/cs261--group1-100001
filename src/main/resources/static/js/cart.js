(() => {
  /* -------------------------
     1) Configuration
     ------------------------- */
  const API_BASE = '/api/cart';
  const PRODUCTS_API = '/api/products'; // optional fallback to enrich product data
  const CART_ITEMS_CONTAINER_ID = 'cart-items';
  const EMPTY_TEMPLATE_ID = 'empty-cart-template';

  /* -------------------------
     2) Utilities
     ------------------------- */
  function formatCurrency(amount) {
    // format as THB
    return new Intl.NumberFormat('th-TH', { style: 'currency', currency: 'THB' }).format(amount || 0);
  }

  function getCsrfHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    if (tokenMeta) {
      const token = tokenMeta.getAttribute('content');
      const headerName = headerMeta ? headerMeta.getAttribute('content') : 'X-CSRF-TOKEN';
      headers[headerName] = token;
    }
    return headers;
  }

  async function safeJsonResponse(response) {
    const txt = await response.text();
    try { return txt ? JSON.parse(txt) : null; }
    catch (e) { return txt; }
  }

  async function apiFetch(url, opts = {}) {
    opts.headers = Object.assign({}, getCsrfHeaders(), opts.headers || {});
    const res = await fetch(url, opts);
    if (res.status === 401) {
      // Not logged in: you can redirect to login or show message
      console.warn('Unauthorized (401).');
      throw { status: 401, message: 'Unauthorized' };
    }
    const payload = await safeJsonResponse(res);
    if (!res.ok) {
      throw { status: res.status, body: payload || res.statusText };
    }
    return payload;
  }

  /* -------------------------
     3) DOM helpers
     ------------------------- */
  function byId(id) { return document.getElementById(id); }
  function qs(selector) { return document.querySelector(selector); }
  function qsa(selector) { return Array.from(document.querySelectorAll(selector)); }

  /* -------------------------
     4) Rendering
     ------------------------- */
  function renderEmpty() {
    const tpl = byId(EMPTY_TEMPLATE_ID);
    const container = byId(CART_ITEMS_CONTAINER_ID);
    container.innerHTML = '';
    if (tpl && tpl.content) {
      container.appendChild(document.importNode(tpl.content, true));
    } else {
      container.innerHTML = '<p>ตะกร้าของคุณว่างเปล่า</p>';
    }
  }

  async function renderCart(cart) {
    const container = byId(CART_ITEMS_CONTAINER_ID);
    container.innerHTML = '';
    if (!cart || !Array.isArray(cart.items) || cart.items.length === 0) {
      renderEmpty();
      updateSummary(cart || { subtotalPrice: 0, totalPrice: 0 });
      return;
    }

    // Optionally reverse to show newest first (uncomment if desired)
    // const items = [...cart.items].reverse();
    const items = cart.items;

    for (const item of items) {
      const productId = item.productId || item.product_id || (item.product && item.product.productId);
      const qty = Number(item.quantity || 0);
      const unitPrice = Number(item.price || (item.product && item.product.price) || 0);
      const totalPrice = unitPrice * qty;

      // Build item element
      const itemEl = document.createElement('div');
      itemEl.className = 'cart-item';
      itemEl.dataset.productId = productId;

      // Product name and optional image (uses item.product if provided)
      const productName = (item.product && (item.product.name || item.product.title)) || `Product #${productId}`;
      const imageUrl = (item.product && item.product.image) || null;
      const description = (item.product && item.product.description) || '';

      itemEl.innerHTML = `
        <div class="cart-image">
          ${imageUrl ? `<img src="${imageUrl}" alt="${productName}" style="max-width:80px;max-height:80px;object-fit:cover"/>` : 'รูป'}
        </div>
        <div class="cart-details">
          <div class="row">
            <span class="product-name">${escapeHtml(productName)}</span>
            <span class="price unit-price">${formatCurrency(unitPrice)}</span>
          </div>
          <div class="row product-desc">${escapeHtml(description)}</div>
          <div class="row">
            <span>คาดการณ์วันที่จะจัดส่ง <small>(จัดส่ง 2-3 วัน)</small></span>
            <span class="delivery">รับสินค้าด้วยตัวเอง</span>
          </div>
          <div class="row actions">
            <label>จำนวน:
              <input type="number" class="qty-input" min="0" value="${qty}" data-product-id="${productId}" />
            </label>
            <button class="remove-btn" data-product-id="${productId}">ลบ</button>
            <span class="item-total" style="margin-left:12px">${formatCurrency(totalPrice)}</span>
          </div>
        </div>
      `;

      container.appendChild(itemEl);
    }

    updateSummary(cart);
  }

  // small utility to avoid XSS when injecting product strings
  function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"'`=\/]/g, function(s) {
      return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;', '/': '&#x2F;', '`': '&#x60;', '=': '&#x3D;' })[s];
    });
  }

  /* -------------------------
     5) Summary update
     ------------------------- */
  function updateSummary(cart) {
    // Try to use backend subtotal/total if present; otherwise compute
    let subtotal = Number(cart && cart.subtotalPrice ? cart.subtotalPrice : 0);
    let total = Number(cart && cart.totalPrice ? cart.totalPrice : 0);

    if ((!subtotal || !total) && cart && Array.isArray(cart.items)) {
      subtotal = cart.items.reduce((acc, it) => acc + ((Number(it.price || 0) * Number(it.quantity || 0))), 0);
      total = subtotal + subtotal * 0.07; // VAT 7%
    }

    // Update DOM elements in your HTML
    const subtotalSpan = qs('.summary .row span:last-child');
    if (subtotalSpan) subtotalSpan.textContent = formatCurrency(subtotal);

    const totalAmount = qs('.total .amount');
    if (totalAmount) totalAmount.textContent = formatCurrency(total);

    // update installment text if present
    const installment = qs('.installment');
    if (installment) {
      installment.textContent = `${formatCurrency(total)}/เดือน เป็นเวลา 10 เดือน ดอกเบี้ย 0%`;
    }
  }

  /* -------------------------
     6) API actions
     ------------------------- */
  async function loadCart() {
    try {
      const cart = await apiFetch(`${API_BASE}`, { method: 'GET' });
      await renderCart(cart || {});
    } catch (err) {
      if (err && err.status === 401) {
        // Not logged in: you may redirect to login if desired
        console.warn('Please login to view cart.');
        renderEmpty();
      } else {
        console.error('Failed to load cart', err);
        renderEmpty();
      }
    }
  }

  async function addToCart(productId, quantity = 1) {
    const payload = { productId: Number(productId), quantity: Number(quantity) };
    const cart = await apiFetch(`${API_BASE}/add`, { method: 'POST', body: JSON.stringify(payload) });
    await renderCart(cart || {});
    return cart;
  }

  async function setItemQuantity(productId, quantity) {
    const payload = { productId: Number(productId), quantity: Number(quantity) };
    const cart = await apiFetch(`${API_BASE}/items/${productId}`, { method: 'PUT', body: JSON.stringify(payload) });
    await renderCart(cart || {});
    return cart;
  }

  async function removeItem(productId) {
    const cart = await apiFetch(`${API_BASE}/items/${productId}`, { method: 'DELETE' });
    await renderCart(cart || {});
    return cart;
  }

  /* -------------------------
     7) Event binding
     ------------------------- */
  function attachEvents() {
    const container = byId(CART_ITEMS_CONTAINER_ID);
    if (!container) return;

    // change quantity
    container.addEventListener('change', async (ev) => {
      const t = ev.target;
      if (t.matches('.qty-input')) {
        const pid = t.dataset.productId;
        const val = Number(t.value);
        if (isNaN(val) || val < 0) {
          t.value = 1;
          return;
        }
        try {
          await setItemQuantity(pid, val);
        } catch (e) {
          console.error(e);
          // on error, reload cart to sync UI
          await loadCart();
        }
      }
    });

    // remove button
    container.addEventListener('click', async (ev) => {
      const t = ev.target;
      if (t.matches('.remove-btn')) {
        const pid = t.dataset.productId;
        if (!confirm('คุณแน่ใจว่าต้องการลบสินค้าชิ้นนี้หรือไม่?')) return;
        try {
          await removeItem(pid);
        } catch (e) {
          console.error(e);
          await loadCart();
        }
      }
    });
  }

   qsa('.pay-btn, .pay-btn-top').forEach(btn => {
        btn.addEventListener('click', (e) => {
        });
      });

  /* -------------------------
     8) Expose helper and start
     ------------------------- */
  window.addToCart = addToCart; // so product pages can call addToCart(productId, qty)

  // Startup (must be at bottom so functions are defined)
  document.addEventListener('DOMContentLoaded', () => {
    attachEvents();
    loadCart();
  });

})();