/* ---- Wishlist ---- */
function getWishlist() {
    try { return JSON.parse(localStorage.getItem('agrihubWishlist') || '[]'); }
    catch { return []; }
}

function saveWishlist(list) {
    localStorage.setItem('agrihubWishlist', JSON.stringify(list));
}

function isInWishlist(productId) {
    return getWishlist().includes(productId);
}

function toggleWishlist(productId, productName) {
    let list = getWishlist();
    if (list.includes(productId)) {
        list = list.filter(id => id !== productId);
        saveWishlist(list);
        showToast(t('toast.removedWishlist'));
    } else {
        list.push(productId);
        saveWishlist(list);
        showToast(`${productName || ''} ${t('toast.addedWishlist')}`);
    }
    return list.includes(productId);
}

window.toggleWishlist = toggleWishlist;
window.isInWishlist = isInWishlist;

/* ---- Notifications ---- */
function getNotifications() {
    try { return JSON.parse(localStorage.getItem('agrihubNotifications') || '[]'); }
    catch { return []; }
}

function saveNotifications(list) {
    localStorage.setItem('agrihubNotifications', JSON.stringify(list.slice(0, 50)));
}

function addNotification(type, message, link) {
    const list = getNotifications();
    list.unshift({ id: Date.now(), type, message, link: link || '', read: false, time: new Date().toISOString() });
    saveNotifications(list);
    updateNotificationBadge();
}

function markAllNotificationsRead() {
    saveNotifications(getNotifications().map(n => ({ ...n, read: true })));
    updateNotificationBadge();
    renderNotificationDropdown();
}

function updateNotificationBadge() {
    const badge = document.getElementById('notifBadge');
    if (!badge) return;
    const unread = getNotifications().filter(n => !n.read).length;
    badge.textContent = unread;
    badge.classList.toggle('d-none', unread === 0);
}

function renderNotificationDropdown() {
    const listEl = document.getElementById('notifList');
    if (!listEl) return;
    const items = getNotifications();
    if (!items.length) {
        listEl.innerHTML = `<div class="notif-empty">${t('common.noNotifications')}</div>`;
        return;
    }
    listEl.innerHTML = items.slice(0, 8).map(n => `
        <a href="${n.link || '#'}" class="notif-item${n.read ? '' : ' unread'}" data-notif-id="${n.id}">
            <span class="notif-type">${n.type}</span>
            <span class="notif-msg">${n.message}</span>
            <small class="text-muted">${new Date(n.time).toLocaleString()}</small>
        </a>
    `).join('');
    listEl.querySelectorAll('.notif-item').forEach(el => {
        el.addEventListener('click', () => {
            const id = Number(el.dataset.notifId);
            saveNotifications(getNotifications().map(n => n.id === id ? { ...n, read: true } : n));
            updateNotificationBadge();
        });
    });
}

async function checkForNotifications() {
    try {
        const farmer = getSession('agrihubFarmer');
        const [orders, expert, products] = await Promise.all([
            fetchJson('/orders').catch(() => []),
            fetchJson('/expert').catch(() => []),
            fetchJson('/products').catch(() => [])
        ]);

        const prevOrders = JSON.parse(localStorage.getItem('agrihubOrderSnapshot') || '{}');
        const prevExpert = JSON.parse(localStorage.getItem('agrihubExpertSnapshot') || '{}');
        const prevProductCount = parseInt(localStorage.getItem('agrihubProductCount') || '0', 10);

        if (farmer?.email) {
            orders.filter(o => o.email === farmer.email).forEach(o => {
                const prev = prevOrders[o.id];
                if (prev && prev !== o.status) {
                    if (o.status === 'Accepted') addNotification('order', t('notify.orderAccepted'), 'orders.html');
                    if (o.status === 'Delivered') addNotification('order', t('notify.orderDelivered'), 'orders.html');
                }
                prevOrders[o.id] = o.status;
            });

            expert.filter(r => r.email === farmer.email).forEach(r => {
                const prev = prevExpert[r.id];
                if (r.adminReply && prev !== r.adminReply) {
                    addNotification('expert', t('notify.expertReply'), 'expert.html');
                }
                prevExpert[r.id] = r.adminReply || '';
            });
        }

        if (products.length > prevProductCount && prevProductCount > 0) {
            addNotification('product', t('notify.newProduct'), 'products.html');
        }

        localStorage.setItem('agrihubOrderSnapshot', JSON.stringify(prevOrders));
        localStorage.setItem('agrihubExpertSnapshot', JSON.stringify(prevExpert));
        localStorage.setItem('agrihubProductCount', String(products.length));

        updateNotificationBadge();
        renderNotificationDropdown();
    } catch (e) {
        console.warn('Notification check failed', e);
    }
}

/* ---- Animated Stats ---- */
function animateCounter(el, target, duration = 1200) {
    if (!el) return;
    const start = 0;
    const startTime = performance.now();
    function step(now) {
        const progress = Math.min((now - startTime) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.floor(start + (target - start) * eased);
        if (progress < 1) requestAnimationFrame(step);
        else el.textContent = target;
    }
    requestAnimationFrame(step);
}

function renderAnimatedStats(stats) {
    const container = document.getElementById('homeStats');
    if (!container) return;
    const cropsCount = stats.cropsSupported || stats.totalCrops || 0;
    container.innerHTML = `
        <div class="col-6"><div class="stat-card stat-animate"><h4 class="fw-bold" data-stat="${stats.totalFarmers}">0</h4><p data-i18n="home.farmersServed">${t('home.farmersServed')}</p></div></div>
        <div class="col-6"><div class="stat-card stat-animate"><h4 class="fw-bold" data-stat="${stats.totalProducts}">0</h4><p data-i18n="home.productsAvailable">${t('home.productsAvailable')}</p></div></div>
        <div class="col-6"><div class="stat-card stat-animate"><h4 class="fw-bold" data-stat="${stats.totalOrders}">0</h4><p data-i18n="home.ordersCompleted">${t('home.ordersCompleted')}</p></div></div>
        <div class="col-6"><div class="stat-card stat-animate"><h4 class="fw-bold" data-stat="${cropsCount}">0</h4><p data-i18n="home.cropsSupported">${t('home.cropsSupported')}</p></div></div>
    `;
    container.querySelectorAll('[data-stat]').forEach(el => {
        animateCounter(el, parseInt(el.dataset.stat, 10) || 0);
    });
}

/* ---- Smart Search Autocomplete ---- */
let searchCache = { products: [], crops: [] };

async function loadSearchData() {
    try {
        const [products, crops] = await Promise.all([fetchJson('/products'), fetchJson('/crops')]);
        searchCache = { products, crops };
    } catch { /* backend offline */ }
}

function getSearchSuggestions(query) {
    if (!query || query.length < 2) return [];
    const q = query.toLowerCase();
    const results = [];

    searchCache.products.forEach(p => {
        if (p.name?.toLowerCase().includes(q) || p.brand?.toLowerCase().includes(q) || p.category?.toLowerCase().includes(q)) {
            results.push({ type: 'product', label: p.name, sub: `${p.category} · ${p.brand}`, href: `product-detail.html?id=${p.id}`, icon: '🛒' });
        }
    });
    searchCache.crops.forEach(c => {
        if (c.name?.toLowerCase().includes(q) || c.category?.toLowerCase().includes(q)) {
            results.push({ type: 'crop', label: c.name, sub: c.bestSeason || c.category, href: `crop-detail.html?id=${c.id}`, icon: '🌱' });
        }
    });
    return results.slice(0, 8);
}

function initSmartSearch(inputId, dropdownId, onSubmit) {
    const input = document.getElementById(inputId);
    const dropdown = document.getElementById(dropdownId);
    if (!input || !dropdown) return;

    let debounce;
    input.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => {
            const suggestions = getSearchSuggestions(input.value.trim());
            if (!suggestions.length) {
                dropdown.classList.add('d-none');
                dropdown.innerHTML = '';
                return;
            }
            dropdown.innerHTML = suggestions.map(s => `
                <a href="${s.href}" class="search-suggestion">
                    <span class="suggestion-icon">${s.icon}</span>
                    <span><strong>${s.label}</strong><small class="d-block text-muted">${s.sub}</small></span>
                </a>
            `).join('');
            dropdown.classList.remove('d-none');
        }, 200);
    });

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && onSubmit) {
            e.preventDefault();
            onSubmit(input.value.trim());
        }
    });

    document.addEventListener('click', (e) => {
        if (!input.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.add('d-none');
        }
    });
}

/* ---- Weather Card (dummy data placeholder) ---- */
function renderWeatherCard() {
    const el = document.getElementById('weatherCard');
    if (!el) return;
    const conditions = [
        { temp: 32, humidity: 68, rain: 20, icon: '☀️', label: 'Sunny' },
        { temp: 28, humidity: 82, rain: 65, icon: '🌧️', label: 'Rainy' },
        { temp: 30, humidity: 55, rain: 10, icon: '⛅', label: 'Partly Cloudy' }
    ];
    const day = new Date().getDate();
    const w = conditions[day % conditions.length];
    el.innerHTML = `
        <div class="weather-card animate-in">
            <div class="weather-icon">${w.icon}</div>
            <div class="weather-info">
                <h4 class="fw-bold mb-1">${w.temp}°C</h4>
                <p class="mb-1 small">${w.label}</p>
                <div class="weather-meta">
                    <span><i class="bi bi-droplet"></i> ${w.humidity}%</span>
                    <span><i class="bi bi-cloud-rain"></i> ${w.rain}%</span>
                </div>
            </div>
            <small class="weather-placeholder">${t('home.weatherPlaceholder')}</small>
        </div>
    `;
}

/* ---- Daily Farming Tip ---- */
const FARMING_TIPS = [
    'Water crops early morning to reduce evaporation and fungal diseases.',
    'Test soil every season before applying fertilizers for balanced nutrition.',
    'Rotate crops annually to break pest cycles and improve soil health.',
    'Remove weeds regularly — they compete for nutrients and water.',
    'Use drip irrigation to save up to 50% water compared to flood irrigation.',
    'Apply neem oil as preventive spray during humid weather.',
    'Store seeds in cool, dry places to maintain germination rate.',
    'Monitor crops weekly for early signs of pest or disease damage.',
    'Add organic compost to improve soil structure and microbial activity.',
    'Avoid over-fertilizing — it can burn roots and pollute groundwater.'
];

function renderDailyTip() {
    const el = document.getElementById('dailyTip');
    if (!el) return;
    const dayOfYear = Math.floor((Date.now() - new Date(new Date().getFullYear(), 0, 0)) / 86400000);
    el.innerHTML = `
        <div class="tip-card animate-in">
            <div class="tip-icon">💡</div>
            <p class="mb-0">${FARMING_TIPS[dayOfYear % FARMING_TIPS.length]}</p>
        </div>
    `;
}

/* ---- Seasonal Recommendations ---- */
function getCurrentSeason() {
    const month = new Date().getMonth() + 1;
    if (month >= 6 && month <= 9) return ['Monsoon', 'Kharif', 'Rainy'];
    if (month >= 10 && month <= 12) return ['Post-Monsoon', 'Rabi', 'Winter'];
    if (month >= 1 && month <= 2) return ['Winter', 'Rabi', 'Cold'];
    return ['Summer', 'Zaid', 'Hot'];
}

function renderSeasonalRecommendations(crops) {
    const el = document.getElementById('seasonalCrops');
    if (!el || !crops?.length) return;
    const seasons = getCurrentSeason().map(s => s.toLowerCase());
    const recommended = crops.filter(c => {
        const bs = (c.bestSeason || '').toLowerCase();
        return seasons.some(s => bs.includes(s)) || bs.includes('year');
    });
    const list = recommended.length ? recommended : crops;
    el.innerHTML = list.slice(0, 4).map(crop => `
        <div class="col-md-6 col-lg-3">
            <div class="season-card animate-in">
                <span class="season-badge">${crop.bestSeason}</span>
                <h6 class="fw-bold">${crop.name}</h6>
                <p class="small text-muted mb-2">${crop.description?.substring(0, 80)}...</p>
                <a href="crop-detail.html?id=${crop.id}" class="btn btn-sm btn-outline-success">${t('crop.fullGuide')}</a>
            </div>
        </div>
    `).join('');
}

/* ---- Fertilizer Calculator ---- */
const FERT_RATES = {
    Rice: { urea: 50, dap: 40, npk: 25 },
    Wheat: { urea: 45, dap: 35, npk: 20 },
    Tomato: { urea: 30, dap: 25, npk: 15 },
    Cotton: { urea: 55, dap: 45, npk: 30 },
    default: { urea: 40, dap: 30, npk: 20 }
};

function initFertilizerCalculator() {
    if (getPageName() !== 'calculator') return;
    const form = document.getElementById('calcForm');
    const result = document.getElementById('calcResult');
    const cropSelect = document.getElementById('calcCrop');
    if (!form || !result) return;

    fetchJson('/crops').then(crops => {
        if (cropSelect) {
            cropSelect.innerHTML = `<option value="">${t('calc.selectCrop')}</option>` +
                crops.map(c => `<option value="${c.name}">${c.name}</option>`).join('');
        }
    }).catch(() => {});

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const cropName = cropSelect?.value;
        const acres = parseFloat(document.getElementById('calcAcres')?.value) || 0;
        if (!cropName || acres <= 0) {
            result.innerHTML = `<p class="text-danger">${t('calc.noCrop')}</p>`;
            return;
        }
        const baseName = Object.keys(FERT_RATES).find(k => cropName.includes(k)) || 'default';
        const rates = FERT_RATES[baseName] || FERT_RATES.default;
        const ureaKg = Math.round(rates.urea * acres);
        const dapKg = Math.round(rates.dap * acres);
        const npkKg = Math.round(rates.npk * acres);
        result.innerHTML = `
            <h5 class="fw-bold text-success mb-3">${t('calc.result')} — ${cropName} (${acres} acres)</h5>
            <div class="calc-result-grid">
                <div class="calc-result-item"><label>${t('calc.requiredFert')}</label><strong>Urea</strong><span>${t('calc.quantity')}: ${ureaKg} kg</span></div>
                <div class="calc-result-item"><label>${t('calc.requiredFert')}</label><strong>DAP</strong><span>${t('calc.quantity')}: ${dapKg} kg</span></div>
                <div class="calc-result-item"><label>${t('calc.requiredFert')}</label><strong>NPK 19:19:19</strong><span>${t('calc.quantity')}: ${npkKg} kg</span></div>
            </div>
            <div class="content-card mt-3">
                <h6 class="fw-bold">${t('calc.schedule')}</h6>
                <ul class="mb-0 small">
                    <li>Basal: 50% DAP + 25% Urea at sowing/transplanting</li>
                    <li>30 days: 25% Urea + NPK top dressing</li>
                    <li>45-60 days: Remaining Urea based on crop stage</li>
                </ul>
            </div>
            <a href="products.html?category=Fertilizers" class="btn btn-success mt-3">${t('disease.searchProducts')}</a>
        `;
    });
}

/* ---- QR Code for Products ---- */
function renderProductQR(productId) {
    const el = document.getElementById('productQR');
    if (!el || typeof QRCode === 'undefined') return;
    el.innerHTML = '';
    const url = `${window.location.origin}/product-detail.html?id=${productId}`;
    new QRCode(el, { text: url, width: 128, height: 128, colorDark: '#1a5c38', colorLight: '#ffffff' });
}

/* ---- Order Status Label ---- */
function orderStatusLabel(status) {
    const key = status?.toLowerCase();
    const map = { pending: 'orders.status.pending', accepted: 'orders.status.accepted', packed: 'orders.status.packed', delivered: 'orders.status.delivered', cancelled: 'orders.status.cancelled', rejected: 'orders.status.rejected' };
    return t(map[key] || 'orders.status.pending');
}

/* ---- Init Features ---- */
document.addEventListener('DOMContentLoaded', () => {
    loadSearchData();
    renderWeatherCard();
    renderDailyTip();
    initFertilizerCalculator();

    initSmartSearch('homeSearchInput', 'homeSearchDropdown', (q) => {
        window.location.href = q ? `products.html?search=${encodeURIComponent(q)}` : 'products.html';
    });
    initSmartSearch('searchInput', 'productsSearchDropdown', (q) => {
        window.location.href = q ? `products.html?search=${encodeURIComponent(q)}` : 'products.html';
    });

    const markReadBtn = document.getElementById('markNotifRead');
    if (markReadBtn) markReadBtn.addEventListener('click', markAllNotificationsRead);

    if (document.getElementById('notifList')) {
        renderNotificationDropdown();
        checkForNotifications();
        setInterval(checkForNotifications, 60000);
    }
});

document.addEventListener('agrihub:langchange', () => {
    renderWeatherCard();
    renderDailyTip();
    renderNotificationDropdown();
});

window.renderAnimatedStats = renderAnimatedStats;
window.renderSeasonalRecommendations = renderSeasonalRecommendations;
window.renderProductQR = renderProductQR;
window.orderStatusLabel = orderStatusLabel;
window.addNotification = addNotification;
