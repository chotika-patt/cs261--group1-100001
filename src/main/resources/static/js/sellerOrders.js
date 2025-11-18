(() => {
  'use strict';

  // ---------- CONFIG ----------
  const API_BASE = '/api/seller/orders';
  const POLL_MS = 10000;           // list polling interval (set 0 to disable)
  const SEARCH_DEBOUNCE_MS = 400;

  // ---------- small helpers ----------
  const qs = (s, r = document) => r.querySelector(s);
  const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));
  const safeText = (v) => (v === null || v === undefined) ? '—' : String(v);
  const formatDate = (iso) => {
    if (!iso) return '—';
    try { return new Date(iso).toLocaleString('th-TH'); } catch { return iso; }
  };

  // read orderId from URL
  const params = new URLSearchParams(window.location.search);
  const orderId = params.get('orderId');

  // ---------- UI refs (detail) ----------
  const refs = {
    orderCard: qs('#order-card'),
    created: qs('#order-created'),
    shopName: qs('#shop-name'),
    thumb: qs('#product-thumb'),
    productName: qs('#product-name'),
    productDetails: qs('#product-details'),
    productPrice: qs('#product-price'),
    buyerName: qs('#buyer-name'),
    buyerPhone: qs('#buyer-phone'),
    buyerAddress: qs('#buyer-address'),
    paymentMethod: qs('#payment-method'),
    paymentStatus: qs('#payment-status'),
    trackingNumber: qs('#tracking-number'),
    statusSteps: qs('#status-steps'),
    sellerActions: qs('#seller-actions'),
    btnAccept: qs('#btn-accept'),
    btnReject: qs('#btn-reject'),
    shippingPanel: qs('#shipping-panel'),
    confirmShipping: qs('#confirm-shipping'),
    trackingStatus: qs('#tracking-status'),
    btnConfirmShipping: qs('#btn-confirm-shipping'),
    sellerHint: qs('#seller-hint'),
    confirmOverlay: qs('#confirm-overlay')
  };

  // ---------- create list containers if not present (single page usage) ----------
  let listContainer = qs('#ordersList');
  let summaryContainer = qs('#ordersSummary');
  let paginationContainer = qs('#pagination');
  if (!listContainer) {
    listContainer = document.createElement('div');
    listContainer.id = 'ordersList';
    listContainer.style.margin = '18px 0';
    // insert before order card if exists, otherwise append to body
    if (refs.orderCard && refs.orderCard.parentNode) {
      refs.orderCard.parentNode.insertBefore(listContainer, refs.orderCard);
    } else {
      document.body.appendChild(listContainer);
    }
  }
  if (!summaryContainer) {
    summaryContainer = document.createElement('div');
    summaryContainer.id = 'ordersSummary';
    if (listContainer.parentNode) listContainer.parentNode.insertBefore(summaryContainer, listContainer);
    else document.body.insertBefore(summaryContainer, listContainer);
  }
  if (!paginationContainer) {
    paginationContainer = document.createElement('div');
    paginationContainer.id = 'pagination';
    listContainer.parentNode.insertBefore(paginationContainer, listContainer.nextSibling);
  }

  // ---------- common utilities ----------
  function showHint(msg, kind = '') {
    const h = refs.sellerHint || qs('#errorBanner') || summaryContainer;
    if (!h) return;
    if (refs.sellerHint) {
      refs.sellerHint.textContent = msg || '';
      refs.sellerHint.className = 'seller-actions__hint' + (kind ? ' ' + kind : '');
    } else {
      // fallback banner presentation
      h.textContent = msg || '';
      h.style.background = kind === 'error' ? '#ffe6e6' : '#e6ffe6';
      h.style.padding = '8px';
      h.style.borderRadius = '6px';
    }
  }

  // ---------- DETAIL MODE ----------
  // Fetch order (safe wrapper)
  async function loadOrderDetail(id) {
    if (!id) {
      showHint('ไม่พบ orderId ใน URL', 'error');
      return;
    }
    try {
      const res = await fetch(`${API_BASE}/${encodeURIComponent(id)}`, { credentials: 'same-origin' });
      if (!res.ok) {
        const txt = await res.text().catch(() => '');
        showHint(`ไม่สามารถโหลดออเดอร์: ${res.status} ${txt}`, 'error');
        return;
      }
      const data = await res.json();
      try {
        applyDetailDataToUI(data);
        showHint('', '');
      } catch (renderErr) {
        // Defensive: if rendering fails, show the error but don't break
        console.error('Render error', renderErr);
        showHint('Render error: ' + (renderErr && renderErr.message ? renderErr.message : String(renderErr)), 'error');
      }
    } catch (e) {
      console.error('Network/load error', e);
      showHint('Network error: ' + e.message, 'error');
    }
  }


  // map backend -> UI safely (handles limited SellerOrderResponse fields)
  function applyDetailDataToUI(data) {
    if (!refs.orderCard) return;

    // createdAt (backend returns ISO string)
    refs.created && (refs.created.textContent = data.createdAt ? formatDate(data.createdAt) : '—');

    // shop name
    refs.shopName && (refs.shopName.textContent = data.shopName || (data.shop && data.shop.name) || 'ร้านค้า');

    const hasProductObj = data.product && typeof data.product === 'object';

    // product details (unchanged fallback behavior)
    const productDetails = hasProductObj ? (data.product.details || data.product.description) : (data.productDetails || '—');

    // Decide product image as before
    const productImage = (hasProductObj && data.product.imageUrl) ? data.product.imageUrl :
                         (data.productImageUrl || (Array.isArray(data.items) && data.items[0] && data.items[0].imageUrl));

    // PRODUCT NAME: show "Order: #<id>" when available, otherwise fallback to product name
    let productName = '—';
    if (data.orderId != null) {
      productName = `Order: #${data.orderId}`;
    } else if (hasProductObj) {
      productName = data.product.name || data.product.title || '—';
    } else {
      productName = data.productName || (Array.isArray(data.items) && data.items[0] && (data.items[0].productName || data.items[0].name)) || '—';
    }

    // PRICE: prefer order totalPrice first, then fallback to product price / item totalPrice
    let productPriceNum = null;
    if (data.totalPrice != null) {
      productPriceNum = Number(data.totalPrice);
    } else if (hasProductObj && data.product.price != null) {
      productPriceNum = Number(data.product.price);
    } else if (typeof data.productPrice === 'number') {
      productPriceNum = Number(data.productPrice);
    } else if (Array.isArray(data.items) && data.items[0] && typeof data.items[0].totalPrice === 'number') {
      productPriceNum = Number(data.items[0].totalPrice);
    }

    refs.productName && (refs.productName.textContent = productName || '—');
    refs.productDetails && (refs.productDetails.textContent = (productDetails && productDetails !== '') ? productDetails : '—');
    refs.productPrice && (refs.productPrice.textContent = (productPriceNum != null) ? `฿${productPriceNum.toFixed(2)}` : '—');

    if (refs.thumb) {
      if (productImage) {
        refs.thumb.src = productImage;
      } else {
        // keep placeholder or set a neutral placeholder
        // refs.thumb.src = '/img/placeholder.png';
      }
    }

    // buyer info - backend may supply buyerName / buyerPhone
    refs.buyerName && (refs.buyerName.textContent = data.buyerName || (data.buyer && (data.buyer.fullName || data.buyer.username)) || '—');
    refs.buyerPhone && (refs.buyerPhone.textContent = data.buyerPhone || (data.buyer && (data.buyer.phone || data.buyer.phoneNumber)) || '—');
    refs.buyerAddress && (refs.buyerAddress.textContent = data.buyerAddress || '—');

    // payment & tracking
    refs.paymentMethod && (refs.paymentMethod.textContent = data.paymentMethod || data.paymentRef || '—');
    const payStatus = (data.paymentStatus && typeof data.paymentStatus === 'object') ? (data.paymentStatus.name || String(data.paymentStatus)) : (data.paymentStatus || null);
    refs.paymentStatus && (refs.paymentStatus.textContent = payStatus || '—');
    refs.trackingNumber && (refs.trackingNumber.textContent = data.trackingCode || data.trackingNumber || '—');

    // store order id on DOM
    if (refs.sellerActions && data.orderId) refs.sellerActions.setAttribute('data-order-id', data.orderId || '');

    // status — prefer shipmentStatus if provided
    const shipmentStatus = (data.shipmentStatus || data.trackingStatus || (data.tracking && data.tracking.status) || data.status || 'PENDING');
    setStatusUI(String(shipmentStatus));

    // show shipping panel only when appropriate
    if (refs.shippingPanel) {
      const shouldShow = ['PREPARING','SHIPPED','PAID','PROCESSING'].includes(String(shipmentStatus).toUpperCase()) || String(data.status).toUpperCase() === 'PAID';
      refs.shippingPanel.hidden = !shouldShow;
    }
  }



  // setStatusUI: robust, works with ShipmentTrackingStatus or fallback values
  function setStatusUI(status) {
    if (!refs.statusSteps) return;
    // normalize (remove spaces/hyphens)
    const val = (status || '').toString().toUpperCase().replace(/[-\s]/g, '_');
    const steps = qsa('.step', refs.statusSteps);
    // derive order array from DOM to support custom steps
    const order = steps.map(s => (s.getAttribute('data-step') || '').toUpperCase());
    steps.forEach(s => {
      s.classList.remove('is-current','is-done','is-active');
      const sVal = (s.getAttribute('data-step') || '').toUpperCase();
      if (sVal === val) s.classList.add('is-current');
      const idxVal = order.indexOf(val);
      const idxStep = order.indexOf(sVal);
      if (idxVal > -1 && idxStep > -1 && idxStep < idxVal) s.classList.add('is-done');
    });
  }

  // send decision: ACCEPT / REJECT
  async function sendDecision(decision) {
    const id = orderId || (refs.sellerActions && refs.sellerActions.getAttribute('data-order-id'));
    if (!id) return showHint('ไม่พบ order id', 'error');

    try {
      refs.btnAccept && (refs.btnAccept.disabled = true);
      refs.btnReject && (refs.btnReject.disabled = true);
      const res = await fetch(`${API_BASE}/${encodeURIComponent(id)}/decision`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ decision })
      });
      if (!res.ok) {
        const txt = await res.text().catch(()=> '');
        showHint(`อัปเดตล้มเหลว ${res.status} ${txt}`, 'error');
        return;
      }
      // success -> reload detail to reflect changes
      await loadOrderDetail(id);
      showHint(decision === 'ACCEPT' ? 'รับออเดอร์เรียบร้อย' : 'ปฏิเสธออเดอร์เรียบร้อย', 'success');
    } catch (e) {
      showHint('Network error: ' + e.message, 'error');
    } finally {
      refs.btnAccept && (refs.btnAccept.disabled = false);
      refs.btnReject && (refs.btnReject.disabled = false);
    }
  }

  // map UI select values to ShipmentTrackingStatus enum names backend expects
  function mapUiShipToEnum(uiVal) {
    if (!uiVal) return null;
    const v = uiVal.toString().trim().toLowerCase();
    // pick mappings you need — adjust to your enums
    if (['packed','handover','in-transit','out-for-delivery'].includes(v)) return 'SHIPPED';
    if (['delivered','จัดส่งสำเร็จ','delivered'].includes(v)) return 'DELIVERED';
    if (['cancelled','canceled','ยกเลิก'].includes(v)) return 'CANCELLED';
    // fallback: convert to uppercase with underscores
    return v.toUpperCase().replace(/[^A-Z0-9_]/g,'_');
  }

  // update shipment status
  async function updateShipmentStatus() {
    const id = orderId || (refs.sellerActions && refs.sellerActions.getAttribute('data-order-id'));
    if (!id) return showHint('ไม่พบ order id', 'error');

    // prefer explicit trackingStatus select or confirmShipping select
    const uiVal = (refs.trackingStatus && refs.trackingStatus.value) || (refs.confirmShipping && refs.confirmShipping.value) || '';
    const newStatus = mapUiShipToEnum(uiVal);
    if (!newStatus) return showHint('กรุณาเลือกสถานะการจัดส่ง', 'error');

    try {
      refs.btnConfirmShipping && (refs.btnConfirmShipping.disabled = true);
      const res = await fetch(`${API_BASE}/${encodeURIComponent(id)}/status`, {
        method: 'PUT',
        credentials: 'same-origin',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({ newStatus })
      });
      if (!res.ok) {
        const txt = await res.text().catch(()=>'');
        showHint(`อัปเดตการจัดส่งล้มเหลว: ${res.status} ${txt}`, 'error');
        return;
      }
      showHint('บันทึกการจัดส่งเรียบร้อย', 'success');
      await loadOrderDetail(id);
    } catch (e) {
      showHint('Network error: ' + e.message, 'error');
    } finally {
      refs.btnConfirmShipping && (refs.btnConfirmShipping.disabled = false);
    }
  }

  // wire detail buttons
  function wireDetailButtons() {
    refs.btnAccept && refs.btnAccept.addEventListener('click', () => sendDecision('ACCEPT'));
    refs.btnReject && refs.btnReject.addEventListener('click', () => {
      // confirm overlay usage
      const ok = confirm('คุณต้องการยืนยันการปฏิเสธออเดอร์นี้หรือไม่?');
      if (ok) sendDecision('REJECT');
    });
    refs.btnConfirmShipping && refs.btnConfirmShipping.addEventListener('click', updateShipmentStatus);
  }

  // ---------- LIST MODE ----------
  // state
  const listState = { page: 0, pageSize: 10, search: '', status: '', sort: 'createdAt:desc', dateFrom: '', dateTo: '', pollTimer: null, lastReq: 0 };

  // ensure filter controls exist or create simple ones
  function ensureListControls() {
    if (qs('#controls')) return; // already present
    const wrap = document.createElement('div'); wrap.id = 'controls'; wrap.style.margin = '12px 0';
    wrap.innerHTML = `
      <input id="searchInput" placeholder="ค้นหา order id / product..." style="padding:6px; width:220px"/>
      <select id="statusFilter" style="padding:6px; margin-left:6px">
        <option value="">สถานะทั้งหมด</option>
        <option value="PENDING">PENDING</option>
        <option value="PAID">PAID</option>
        <option value="COMPLETED">COMPLETED</option>
        <option value="CANCELLED">CANCELLED</option>
      </select>
      <select id="sortSelect" style="padding:6px; margin-left:6px">
        <option value="createdAt:desc">วันที่ล่าสุด</option>
        <option value="createdAt:asc">วันที่เก่าสุด</option>
        <option value="totalPrice:desc">ยอดรวมมาก→น้อย</option>
      </select>
      <select id="pageSizeSelect" style="padding:6px; margin-left:6px">
        <option value="10">10/หน้า</option><option value="20">20/หน้า</option><option value="50">50/หน้า</option>
      </select>
    `;
    listContainer.parentNode.insertBefore(wrap, listContainer);
  }

  function renderSummary(summary) {
    summaryContainer.innerHTML = `
      <div style="display:flex;gap:16px;align-items:center;">
        <div><strong>คำสั่งซื้อ:</strong> ${safeText(summary?.totalOrders ?? 0)}</div>
        <div><strong>ยอดขายรวม:</strong> ฿${(summary?.totalSales ?? 0).toFixed(2)}</div>
        <div><strong>ยอดค้างชำระ:</strong> ฿${(summary?.pendingPayments ?? 0).toFixed(2)}</div>
      </div>
    `;
  }

  function renderOrderListItem(order) {
    const row = document.createElement('div');
    row.className = 'order-row';
    row.style.border = '1px solid #eee';
    row.style.padding = '10px';
    row.style.marginBottom = '8px';
    row.innerHTML = `
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <div>#${order.orderId} • ${order.items?.length ?? 0} ชิ้น</div>
          <div style="font-size:12px;color:#666">${order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}</div>
        </div>
        <div>
          <div style="text-align:right">฿${(order.totalPrice ?? 0).toFixed(2)}</div>
          <div style="text-align:right">${order.status || ''}</div>
        </div>
      </div>
      <div style="margin-top:8px;display:flex;gap:8px;align-items:center;">
        <button class="btn-view" data-id="${order.orderId}">ดูรายละเอียด</button>
        <button class="btn-accept" data-id="${order.orderId}">ตกลง</button>
        <button class="btn-reject" data-id="${order.orderId}">ปฏิเสธ</button>
      </div>
    `;
    // wire view button
    row.querySelector('.btn-view')?.addEventListener('click', () => {
      // navigate to same page with ?orderId=
      const url = new URL(window.location.href);
      url.searchParams.set('orderId', String(order.orderId));
      window.location.href = url.toString();
    });
    row.querySelector('.btn-accept')?.addEventListener('click', () => applyDecisionFromList(order.orderId, 'ACCEPT'));
    row.querySelector('.btn-reject')?.addEventListener('click', () => {
      const ok = confirm('ยืนยันปฏิเสธคำสั่งซื้อหรือไม่?');
      if (ok) applyDecisionFromList(order.orderId, 'REJECT');
    });
    return row;
  }

  async function applyDecisionFromList(id, decision) {
    try {
      const res = await fetch(`${API_BASE}/${encodeURIComponent(id)}/decision`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({ decision })
      });
      if (!res.ok) {
        const txt = await res.text().catch(()=>'');
        showHint(`ล้มเหลว: ${res.status} ${txt}`, 'error');
        return;
      }
      // refresh list
      await fetchAndRenderList();
    } catch (e) {
      showHint('Network error: ' + e.message, 'error');
    }
  }

  function renderPagination(meta) {
    paginationContainer.innerHTML = '';
    const page = meta.page ?? 0;
    const totalPages = meta.totalPages ?? 1;
    const totalElements = meta.totalElements ?? 0;
    const info = document.createElement('div');
    info.textContent = `หน้า ${page+1} / ${totalPages} — ทั้งหมด ${totalElements} รายการ`;
    paginationContainer.appendChild(info);

    const nav = document.createElement('div');
    nav.style.marginTop = '8px';
    const prev = document.createElement('button');
    prev.textContent = 'ก่อนหน้า'; prev.disabled = page <= 0;
    prev.addEventListener('click', () => { listState.page = Math.max(0, page - 1); fetchAndRenderList(); });
    const next = document.createElement('button');
    next.textContent = 'ถัดไป'; next.disabled = page >= totalPages-1;
    next.addEventListener('click', () => { listState.page = Math.min(totalPages-1, page + 1); fetchAndRenderList(); });

    nav.appendChild(prev);
    // small window of pages
    const start = Math.max(0, page - 2);
    const end = Math.min(totalPages-1, start + 4);
    for (let i = start; i <= end; i++) {
      const b = document.createElement('button');
      b.textContent = (i+1).toString();
      b.disabled = i === page;
      b.style.marginLeft = '6px';
      b.addEventListener('click', () => { listState.page = i; fetchAndRenderList(); });
      nav.appendChild(b);
    }
    nav.appendChild(next);
    paginationContainer.appendChild(nav);
  }

  async function fetchAndRenderList() {
    listState.lastReq = (listState.lastReq || 0) + 1;
    const token = listState.lastReq;
    showHint('', '');

    try {
      const params = new URLSearchParams();
      if (listState.search) params.set('search', listState.search);
      if (listState.status) params.set('status', listState.status);
      if (listState.dateFrom && listState.dateTo) params.set('dateRange', `${listState.dateFrom},${listState.dateTo}`);
      params.set('page', String(listState.page));
      params.set('pageSize', String(listState.pageSize));
      params.set('sort', listState.sort || 'createdAt:desc');

      const res = await fetch(`${API_BASE}?${params.toString()}`, { credentials: 'same-origin' });
      if (token !== listState.lastReq) return; // ignore stale
      if (!res.ok) {
        if (res.status === 404) {
          // empty
          summaryContainer.innerHTML = '';
          listContainer.innerHTML = '<div class="empty">ยังไม่มีคำสั่งซื้อ</div>';
          renderPagination({ page: 0, totalPages: 0, totalElements: 0 });
          return;
        }
        const txt = await res.text().catch(()=>'');
        showHint(`Error fetching orders: ${res.status} ${txt}`, 'error');
        return;
      }

      const payload = await res.json();
      renderSummary(payload.summary || {});
      const content = payload.content || [];
      listContainer.innerHTML = '';
      if (content.length === 0) {
        listContainer.innerHTML = '<div class="empty">ยังไม่มีคำสั่งซื้อ</div>';
      } else {
        for (const o of content) listContainer.appendChild(renderOrderListItem(o));
      }
      const pageMeta = payload.pageable || { page: listState.page, size: listState.pageSize, totalPages: payload.totalPages || 1, totalElements: payload.totalElements || 0 };
      renderPagination({ page: pageMeta.page ?? pageMeta.number ?? 0, totalPages: pageMeta.totalPages ?? 1, totalElements: pageMeta.totalElements ?? 0 });

    } catch (e) {
      showHint('Network error: ' + e.message, 'error');
    }
  }

  function wireListControls() {
    ensureListControls();
    const searchInput = qs('#searchInput');
    const statusSel = qs('#statusFilter');
    const sortSel = qs('#sortSelect');
    const pageSizeSel = qs('#pageSizeSelect');

    // init values
    if (pageSizeSel) listState.pageSize = Number(pageSizeSel.value) || 10;

    // debounce search
    let to = null;
    if (searchInput) {
      searchInput.addEventListener('input', () => {
        clearTimeout(to);
        to = setTimeout(() => { listState.search = searchInput.value.trim(); listState.page = 0; fetchAndRenderList(); }, SEARCH_DEBOUNCE_MS);
      });
      searchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') { clearTimeout(to); listState.search = searchInput.value.trim(); listState.page = 0; fetchAndRenderList(); }});
    }
    statusSel && statusSel.addEventListener('change', () => { listState.status = statusSel.value; listState.page = 0; fetchAndRenderList(); });
    sortSel && sortSel.addEventListener('change', () => { listState.sort = sortSel.value; listState.page = 0; fetchAndRenderList(); });
    pageSizeSel && pageSizeSel.addEventListener('change', () => { listState.pageSize = Number(pageSizeSel.value); listState.page = 0; fetchAndRenderList(); });
  }

  function startListPolling() {
    if (!POLL_MS || POLL_MS <= 0) return;
    if (listState.pollTimer) clearInterval(listState.pollTimer);
    listState.pollTimer = setInterval(fetchAndRenderList, POLL_MS);
  }
  function stopListPolling() { if (listState.pollTimer) clearInterval(listState.pollTimer); listState.pollTimer = null; }

  // ---------- INIT ----------
  async function initDetailMode() {
    // hide list UI when in detail mode
    summaryContainer && (summaryContainer.style.display = 'none');
    listContainer && (listContainer.style.display = 'none');
    paginationContainer && (paginationContainer.style.display = 'none');

    wireDetailButtons();
    await loadOrderDetail(orderId);
  }

  async function initListMode() {
    // ensure detail card hidden
    if (refs.orderCard) refs.orderCard.style.display = 'none';

    // show list UI
    summaryContainer.style.display = '';
    listContainer.style.display = '';
    paginationContainer.style.display = '';

    wireListControls();
    await fetchAndRenderList();
    startListPolling();
  }

  // run
  if (orderId) {
    // ensure detail card visible
    if (refs.orderCard) refs.orderCard.style.display = '';
    document.addEventListener('DOMContentLoaded', initDetailMode);
  } else {
    document.addEventListener('DOMContentLoaded', initListMode);
  }

})();
