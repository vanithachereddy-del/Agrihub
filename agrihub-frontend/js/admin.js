function requireAdmin() {
    const admin = getSession('agrihubAdmin');
    if (!admin) {
        window.location.href = 'admin-login.html';
        return null;
    }
    return admin;
}

function showAdminSection(sectionId) {
    document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.admin-nav a').forEach(a => a.classList.remove('active'));
    const section = document.getElementById(sectionId);
    if (section) section.classList.add('active');
    const navLink = document.querySelector(`.admin-nav a[data-section="${sectionId}"]`);
    if (navLink) navLink.classList.add('active');
}

async function loadDashboard() {
    try {
        const stats = await fetchJson('/dashboard');
        document.getElementById('statProducts').textContent = stats.totalProducts;
        document.getElementById('statOrders').textContent = stats.totalOrders;
        document.getElementById('statFarmers').textContent = stats.totalFarmers;
        document.getElementById('statExpert').textContent = stats.expertRequests;
        document.getElementById('statRevenue').textContent = formatCurrency(stats.totalRevenue || 0);
        document.getElementById('statPending').textContent = stats.pendingOrders;

        const lowStockList = document.getElementById('lowStockList');
        if (lowStockList) {
            lowStockList.innerHTML = stats.lowStockProducts?.length
                ? stats.lowStockProducts.map(p => `
                    <tr>
                        <td>${p.name}</td>
                        <td>${p.category}</td>
                        <td><span class="stock-badge stock-low">${p.stock}</span></td>
                    </tr>
                `).join('')
                : '<tr><td colspan="3" class="text-muted">All products well stocked</td></tr>';
        }

        const outStockList = document.getElementById('outStockList');
        if (outStockList) {
            outStockList.innerHTML = stats.outOfStockProducts?.length
                ? stats.outOfStockProducts.map(p => `
                    <tr>
                        <td>${p.name}</td>
                        <td>${p.category}</td>
                        <td><span class="stock-badge stock-out">0</span></td>
                    </tr>
                `).join('')
                : '<tr><td colspan="3" class="text-muted">No out-of-stock products</td></tr>';
        }

        renderCharts(stats);
        renderReportCharts(stats);
    } catch (error) {
        console.error(error);
    }
}

function renderCharts(stats) {
    const ordersCtx = document.getElementById('ordersChart');
    const productsCtx = document.getElementById('productsChart');
    if (!ordersCtx || !productsCtx || typeof Chart === 'undefined') return;

    if (window.ordersChartInstance) window.ordersChartInstance.destroy();
    if (window.productsChartInstance) window.productsChartInstance.destroy();

    window.ordersChartInstance = new Chart(ordersCtx, {
        type: 'doughnut',
        data: {
            labels: ['Orders', 'Pending', 'Farmers', 'Expert Requests'],
            datasets: [{
                data: [stats.totalOrders, stats.pendingOrders, stats.totalFarmers, stats.expertRequests],
                backgroundColor: ['#2d8a55', '#e8b84a', '#1a5c38', '#5a6b5e']
            }]
        },
        options: { plugins: { legend: { position: 'bottom' } } }
    });

    window.productsChartInstance = new Chart(productsCtx, {
        type: 'bar',
        data: {
            labels: ['Products', 'Low Stock', 'Out of Stock', 'Orders'],
            datasets: [{
                label: 'Count',
                data: [stats.totalProducts, stats.lowStockProducts?.length || 0, stats.outOfStockProducts?.length || 0, stats.totalOrders],
                backgroundColor: ['#2d8a55', '#e8b84a', '#b02a37', '#1a5c38']
            }]
        },
        options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
    });
}

function renderReportCharts(stats) {
    const monthlyCtx = document.getElementById('monthlyOrdersChart');
    const bestCtx = document.getElementById('bestSellingChart');
    const categoryCtx = document.getElementById('categoryChart');
    if (typeof Chart === 'undefined') return;

    if (monthlyCtx) {
        if (window.monthlyChartInstance) window.monthlyChartInstance.destroy();
        const labels = Object.keys(stats.monthlyOrders || {});
        const data = Object.values(stats.monthlyOrders || {});
        window.monthlyChartInstance = new Chart(monthlyCtx, {
            type: 'line',
            data: { labels, datasets: [{ label: 'Orders', data, borderColor: '#2d8a55', backgroundColor: 'rgba(45,138,85,0.1)', fill: true, tension: 0.3 }] },
            options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
        });
    }

    if (bestCtx) {
        if (window.bestSellingChartInstance) window.bestSellingChartInstance.destroy();
        const labels = Object.keys(stats.bestSellingProducts || {}).slice(0, 6);
        const data = labels.map(k => stats.bestSellingProducts[k]);
        window.bestSellingChartInstance = new Chart(bestCtx, {
            type: 'bar',
            data: { labels, datasets: [{ label: 'Qty Sold', data, backgroundColor: '#2d8a55' }] },
            options: { indexAxis: 'y', plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true } } }
        });
    }

    if (categoryCtx) {
        if (window.categoryChartInstance) window.categoryChartInstance.destroy();
        const labels = Object.keys(stats.categoryWiseProducts || {});
        const data = Object.values(stats.categoryWiseProducts || {});
        window.categoryChartInstance = new Chart(categoryCtx, {
            type: 'pie',
            data: { labels, datasets: [{ data, backgroundColor: ['#2d8a55', '#e8b84a', '#1a5c38', '#5a6b5e', '#b02a37'] }] },
            options: { plugins: { legend: { position: 'bottom' } } }
        });
    }
}

async function loadAdminProducts() {
    const tbody = document.getElementById('productsTableBody');
    if (!tbody) return;
    try {
        const products = await fetchJson('/products');
        tbody.innerHTML = products.map(p => `
            <tr>
                <td>${p.name}</td>
                <td>${p.brand}</td>
                <td>${p.category}</td>
                <td>${formatCurrency(p.price)}</td>
                <td><span class="stock-badge ${stockClass(p.stock)}">${p.stock}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-success btn-action" onclick="editProduct('${p.id}')">Edit</button>
                    <button class="btn btn-sm btn-outline-danger btn-action" onclick="deleteProduct('${p.id}')">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error(error);
    }
}

window.editProduct = async function (id) {
    const product = await fetchJson(`/products/${id}`);
    const form = document.getElementById('productForm');
    form.dataset.editId = id;
    form.name.value = product.name;
    form.brand.value = product.brand;
    form.category.value = product.category;
    form.price.value = product.price;
    form.stock.value = product.stock;
    form.unit.value = product.unit;
    form.quantity.value = product.quantity || '';
    form.description.value = product.description || '';
    document.getElementById('productFormTitle').textContent = 'Update Product';
    showAdminSection('section-products');
};

window.deleteProduct = async function (id) {
    if (!confirm('Delete this product?')) return;
    await fetch(apiUrl(`/products/${id}`), { method: 'DELETE' });
    showToast('Product deleted');
    loadAdminProducts();
    loadDashboard();
};

function handleProductForm() {
    const form = document.getElementById('productForm');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = Object.fromEntries(new FormData(form));
        payload.price = parseFloat(payload.price);
        payload.stock = parseInt(payload.stock, 10);
        const editId = form.dataset.editId;
        if (editId) {
            await putJson(`/products/${editId}`, payload);
            showToast('Product updated');
        } else {
            await postJson('/products', payload);
            showToast('Product added');
            if (typeof addNotification === 'function') addNotification('product', 'New product added by admin', 'products.html');
        }
        form.reset();
        delete form.dataset.editId;
        document.getElementById('productFormTitle').textContent = 'Add Product';
        loadAdminProducts();
        loadDashboard();
    });
}

async function loadAdminOrders() {
    const tbody = document.getElementById('ordersTableBody');
    if (!tbody) return;
    try {
        const orders = await fetchJson('/orders');
        tbody.innerHTML = orders.map(o => `
            <tr>
                <td>${o.customerName}</td>
                <td>${o.items?.map(i => i.productName).join(', ')}</td>
                <td>${formatCurrency(o.totalAmount)}</td>
                <td><span class="order-status status-${o.status?.toLowerCase()}">${o.status}</span></td>
                <td>${o.trackingNumber || '-'}</td>
                <td>
                    <button class="btn btn-sm btn-success btn-action" onclick="updateOrderStatus('${o.id}','Accepted')">Accept</button>
                    <button class="btn btn-sm btn-outline-primary btn-action" onclick="updateOrderStatus('${o.id}','Packed')">Pack</button>
                    <button class="btn btn-sm btn-outline-success btn-action" onclick="updateOrderStatus('${o.id}','Delivered')">Deliver</button>
                    <button class="btn btn-sm btn-outline-danger btn-action" onclick="updateOrderStatus('${o.id}','Cancelled')">Cancel</button>
                </td>
            </tr>
        `).join('') || '<tr><td colspan="6" class="text-muted">No orders yet</td></tr>';
    } catch (error) {
        console.error(error);
    }
}

window.updateOrderStatus = async function (id, status) {
    await putJson(`/orders/${id}/status`, { status });
    showToast(`Order ${status.toLowerCase()}`);
    loadAdminOrders();
    loadDashboard();
};

async function loadAdminExpert() {
    const container = document.getElementById('expertAdminList');
    if (!container) return;
    try {
        const requests = await fetchJson('/expert');
        container.innerHTML = requests.map(r => `
            <div class="list-item-card mb-3">
                <div class="d-flex justify-content-between">
                    <strong>${r.farmerName} — ${r.crop || 'General'}</strong>
                    <span class="order-status status-${r.status?.toLowerCase()}">${r.status}</span>
                </div>
                <p class="small text-muted">${r.question}</p>
                ${r.imageUrl ? `<img src="${r.imageUrl}" class="preview-img" alt="Crop image">` : ''}
                <div class="input-group mt-2">
                    <input type="text" class="form-control form-control-sm" id="reply-${r.id}" placeholder="Type expert reply..." value="${r.adminReply || ''}">
                    <button class="btn btn-sm btn-success" onclick="sendExpertReply('${r.id}')">Reply</button>
                </div>
            </div>
        `).join('') || '<p class="empty-state">No expert requests</p>';
    } catch (error) {
        console.error(error);
    }
}

window.sendExpertReply = async function (id) {
    const input = document.getElementById(`reply-${id}`);
    await putJson(`/expert/${id}`, { adminReply: input.value, status: 'Replied' });
    showToast('Reply sent');
    loadAdminExpert();
};

async function loadAdminFarmers() {
    const tbody = document.getElementById('farmersTableBody');
    if (!tbody) return;
    try {
        const farmers = await fetchJson('/farmers');
        tbody.innerHTML = farmers.map(f => `
            <tr>
                <td>${f.fullName}</td>
                <td>${f.phone || '-'}</td>
                <td>${f.village || f.address || '-'}</td>
                <td>${(f.crops || []).join(', ') || '-'}</td>
                <td>${f.farmSize || '-'}</td>
            </tr>
        `).join('') || '<tr><td colspan="5" class="text-muted">No farmers registered</td></tr>';
    } catch (error) {
        console.error(error);
    }
}

function initAdminNav() {
    document.querySelectorAll('.admin-nav a[data-section]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            showAdminSection(link.dataset.section);
        });
    });

    const toggle = document.getElementById('sidebarToggle');
    const sidebar = document.querySelector('.admin-sidebar');
    if (toggle && sidebar) {
        toggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }

    const logout = document.getElementById('adminLogout');
    if (logout) {
        logout.addEventListener('click', (e) => {
            e.preventDefault();
            clearSession('agrihubAdmin');
            window.location.href = 'admin-login.html';
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.body.dataset.page !== 'admin') return;
    if (!requireAdmin()) return;

    initAdminNav();
    showAdminSection('section-dashboard');
    loadDashboard();
    loadAdminProducts();
    loadAdminOrders();
    loadAdminExpert();
    loadAdminFarmers();
    handleProductForm();
});
