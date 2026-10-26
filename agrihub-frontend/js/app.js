function getPageName() {
    return document.body?.dataset?.page || 'home';
}

async function fetchJson(path, options = {}) {
    const response = await fetch(apiUrl(path), {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options
    });
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `Request failed: ${response.status}`);
    }
    if (response.status === 204) return null;
    return response.json();
}

async function postJson(path, payload) {
    return fetchJson(path, { method: 'POST', body: JSON.stringify(payload) });
}

async function putJson(path, payload) {
    return fetchJson(path, { method: 'PUT', body: JSON.stringify(payload) });
}

function getCart() {
    return JSON.parse(localStorage.getItem('agrihubCart') || '[]');
}

function saveCart(cart) {
    localStorage.setItem('agrihubCart', JSON.stringify(cart));
}

function stockClass(stock) {
    if (stock <= 0) return 'stock-out';
    if (stock < 20) return 'stock-low';
    return 'stock-ok';
}

function stockLabel(stock) {
    if (typeof t === 'function') {
        if (stock <= 0) return t('stock.outOfStock');
        if (stock < 20) return `${t('stock.lowStock')} (${stock})`;
        return `${t('stock.inStock')} (${stock})`;
    }
    if (stock <= 0) return 'Out of stock';
    if (stock < 20) return `Low stock (${stock})`;
    return `In stock (${stock})`;
}

function renderProductCard(product) {
    const wished = typeof isInWishlist === 'function' && isInWishlist(product.id);
    const img = product.imageUrl
        ? `<img src="${product.imageUrl}" alt="${product.name}" class="product-img" loading="lazy">`
        : `<div class="product-img d-flex align-items-center justify-content-center text-muted">🌿</div>`;
    return `
        <div class="col-md-6 col-xl-4 animate-in">
            <div class="product-card">
                ${img}
                <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                    <h5 class="fw-bold mb-0">${product.name}</h5>
                    <span class="badge-category">${product.category}</span>
                </div>
                <p class="text-muted small mb-1">${t('products.brand')}: ${product.brand}</p>
                <p class="fw-bold product-price mb-2">${formatCurrency(product.price)} / ${product.unit}</p>
                <p class="small text-muted mb-2">${product.quantity || ''}</p>
                <p class="small mb-1"><strong>${t('stock.available')}:</strong> ${product.stock} ${product.unit}</p>
                <span class="stock-badge ${stockClass(product.stock)}">${stockLabel(product.stock)}</span>
                <div class="d-flex gap-2 mt-3 flex-wrap">
                    <a href="product-detail.html?id=${product.id}" class="btn btn-sm btn-outline-success">${t('common.details')}</a>
                    <button class="btn btn-sm btn-success" onclick="addToCart('${product.id}')" ${product.stock <= 0 ? 'disabled' : ''}>${t('common.addToCart')}</button>
                    <button class="btn btn-sm btn-outline-success" onclick="toggleWishlist('${product.id}','${product.name.replace(/'/g, "\\'")}')" title="${wished ? t('common.removeFromWishlist') : t('common.addToWishlist')}">${wished ? '❤️' : '🤍'}</button>
                </div>
            </div>
        </div>
    `;
}

async function addToCart(productId) {
    try {
        const product = await fetchJson(`/products/${productId}`);
        const cart = getCart();
        const existing = cart.find(item => item.productId === productId);
        if (existing) {
            existing.quantity += 1;
        } else {
            cart.push({
                productId: product.id,
                name: product.name,
                price: product.price,
                quantity: 1
            });
        }
        saveCart(cart);
        showToast(`${product.name} ${t('toast.addedCart')}`);
        renderCartSummary();
    } catch (error) {
        showToast('Could not add to cart', 'error');
    }
}

window.addToCart = addToCart;

function renderCartSummary() {
    const el = document.getElementById('cartSummary');
    if (!el) return;
    const cart = getCart();
    if (!cart.length) {
        el.innerHTML = `<p class="empty-state">${t('orders.emptyCart')}</p>`;
        return;
    }
    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    el.innerHTML = `
        ${cart.map(item => `
            <div class="cart-item">
                <div>
                    <strong>${item.name}</strong>
                    <div class="small text-muted">Qty: ${item.quantity} × ${formatCurrency(item.price)}</div>
                </div>
                <button class="btn btn-sm btn-outline-danger" onclick="removeFromCart('${item.productId}')">×</button>
            </div>
        `).join('')}
        <div class="d-flex justify-content-between fw-bold mt-3 pt-2 border-top">
            <span>${t('orders.total')}</span>
            <span class="product-price">${formatCurrency(total)}</span>
        </div>
    `;
}

window.removeFromCart = function (productId) {
    const cart = getCart().filter(item => item.productId !== productId);
    saveCart(cart);
    renderCartSummary();
    showToast(t('toast.removedCart'));
};

/* ---- Home Page ---- */
async function loadHomePage() {
    if (getPageName() !== 'home') return;
    try {
        const [products, crops, stats] = await Promise.all([
            fetchJson('/products'),
            fetchJson('/crops'),
            fetchJson('/dashboard')
        ]);

        if (typeof renderAnimatedStats === 'function') {
            renderAnimatedStats(stats);
        }
        if (typeof renderSeasonalRecommendations === 'function') {
            renderSeasonalRecommendations(crops);
        }

        const fertilizers = products.filter(p => p.category?.toLowerCase().includes('fertilizer')).slice(0, 3);
        const pesticides = products.filter(p => p.category?.toLowerCase().includes('pesticide')).slice(0, 3);

        const fertEl = document.getElementById('featuredFertilizers');
        if (fertEl) fertEl.innerHTML = fertilizers.map(renderProductCard).join('') || '<p class="text-muted">No fertilizers listed yet.</p>';

        const pestEl = document.getElementById('featuredPesticides');
        if (pestEl) pestEl.innerHTML = pesticides.map(renderProductCard).join('') || '<p class="text-muted">No pesticides listed yet.</p>';

        const cropPreview = document.getElementById('cropPreview');
        if (cropPreview) {
            cropPreview.innerHTML = crops.slice(0, 3).map(crop => `
                <div class="col-md-4">
                    <div class="crop-card">
                        <h5 class="fw-bold">${crop.name}</h5>
                        <p class="text-muted small">${crop.description?.substring(0, 100)}...</p>
                        <a href="crop-detail.html?id=${crop.id}" class="btn btn-sm btn-outline-success">View Guide</a>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Home load error:', error);
    }

    const homeSearch = document.getElementById('homeSearchForm');
    if (homeSearch) {
        homeSearch.addEventListener('submit', (e) => {
            e.preventDefault();
            const q = document.getElementById('homeSearchInput').value;
            window.location.href = `products.html?search=${encodeURIComponent(q)}`;
        });
    }
}

/* ---- Products Page ---- */
async function loadProducts() {
    if (getPageName() !== 'products') return;
    const list = document.getElementById('productList');
    if (!list) return;

    const params = new URLSearchParams(window.location.search);
    const search = params.get('search') || '';
    const category = params.get('category') || '';

    if (search) {
        const input = document.getElementById('searchInput');
        if (input) input.value = search;
    }

    try {
        let path = '/products';
        const query = new URLSearchParams();
        if (search) query.set('search', search);
        if (category) query.set('category', category);
        if (query.toString()) path += `?${query}`;

        const products = await fetchJson(path);
        list.innerHTML = products.length
            ? products.map(renderProductCard).join('')
            : '<p class="empty-state">No products found.</p>';
    } catch {
        list.innerHTML = '<p class="text-danger">Unable to load products. Check that the backend API is reachable.</p>';
    }

    document.querySelectorAll('.filter-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            const cat = pill.dataset.category;
            window.location.href = cat ? `products.html?category=${encodeURIComponent(cat)}` : 'products.html';
        });
        if (pill.dataset.category === category || (!category && !pill.dataset.category)) {
            pill.classList.add('active');
        }
    });

    const searchBtn = document.getElementById('searchButton');
    const searchInput = document.getElementById('searchInput');
    if (searchBtn && searchInput) {
        searchBtn.addEventListener('click', () => {
            window.location.href = `products.html?search=${encodeURIComponent(searchInput.value)}`;
        });
    }
}

/* ---- Product Detail ---- */
async function loadProductDetail() {
    if (getPageName() !== 'product-detail') return;
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');
    const container = document.getElementById('productDetail');
    if (!id || !container) return;

    try {
        const product = await fetchJson(`/products/${id}`);
        const wished = typeof isInWishlist === 'function' && isInWishlist(product.id);
        container.innerHTML = `
            <div class="row g-4">
                <div class="col-lg-5">
                    ${product.imageUrl
                        ? `<img src="${product.imageUrl}" class="w-100 rounded-4 product-detail-img" alt="${product.name}">`
                        : '<div class="product-img" style="height:300px">🌿</div>'}
                    <div class="qr-wrap mt-3 text-center">
                        <p class="small fw-semibold mb-2" data-i18n="products.scanQr">${t('products.scanQr')}</p>
                        <div id="productQR" class="d-inline-block"></div>
                        <p class="small text-muted mt-2" data-i18n="products.qrHint">${t('products.qrHint')}</p>
                    </div>
                </div>
                <div class="col-lg-7">
                    <span class="badge-category">${product.category}</span>
                    <h1 class="page-title mt-2">${product.name}</h1>
                    <p class="text-muted">${t('products.brand')}: <strong>${product.brand}</strong></p>
                    <h3 class="product-price">${formatCurrency(product.price)} <small class="text-muted fs-6">/ ${product.unit}</small></h3>
                    <p class="mb-2"><span class="stock-badge ${stockClass(product.stock)}">${stockLabel(product.stock)}</span></p>
                    <p class="mb-1"><strong>${t('stock.available')}:</strong> ${product.stock} ${product.unit}</p>
                    <p class="mb-1"><strong>${t('products.package')}:</strong> ${product.quantity || 'N/A'}</p>
                    <p class="text-muted mb-4">${product.description || 'No description available.'}</p>
                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-success btn-lg" onclick="addToCart('${product.id}')" ${product.stock <= 0 ? 'disabled' : ''}>${t('common.orderNow')}</button>
                        <button class="btn btn-outline-success btn-lg" onclick="toggleWishlist('${product.id}','${product.name.replace(/'/g, "\\'")}')">${wished ? '❤️ ' + t('common.removeFromWishlist') : '🤍 ' + t('common.addToWishlist')}</button>
                        <a href="orders.html" class="btn btn-outline-success btn-lg">${t('common.addToCart')}</a>
                    </div>
                </div>
            </div>
        `;
        if (typeof renderProductQR === 'function') renderProductQR(product.id);
    } catch {
        container.innerHTML = '<p class="text-danger">Product not found.</p>';
    }
}

/* ---- Crop Guide ---- */
async function loadCropGuides() {
    if (getPageName() !== 'crop-guide') return;
    const list = document.getElementById('cropGuideList');
    if (!list) return;
    try {
        const crops = await fetchJson('/crops');
        list.innerHTML = crops.map(crop => `
            <div class="col-md-6 col-lg-4 animate-in">
                <div class="crop-card">
                    ${crop.imageUrl ? `<img src="${crop.imageUrl}" class="product-img" alt="${crop.name}">` : ''}
                    <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                        <h5 class="fw-bold mb-0">${crop.name}</h5>
                        <span class="badge-category">${crop.category}</span>
                    </div>
                    <p class="text-muted small">${crop.description?.substring(0, 120)}...</p>
                    <p class="small mb-2"><strong>${t('crop.bestSeason')}:</strong> ${crop.bestSeason}</p>
                    <a href="crop-detail.html?id=${crop.id}" class="btn btn-sm btn-outline-success">${t('crop.fullGuide')}</a>
                </div>
            </div>
        `).join('');
    } catch (error) {
        list.innerHTML = '<p class="text-danger">Unable to load crop guides.</p>';
    }
}

async function loadCropDetail() {
    if (getPageName() !== 'crop-detail') return;
    const id = new URLSearchParams(window.location.search).get('id');
    const container = document.getElementById('cropDetail');
    if (!id || !container) return;

    try {
        const crop = await fetchJson(`/crops/${id}`);
        const stages = crop.growthStages?.split(/\d+\./).filter(Boolean) || [];
        container.innerHTML = `
            <div class="crop-detail-hero">
                <span class="badge bg-light text-dark">${crop.category}</span>
                <h1 class="mt-2 mb-2">${crop.name}</h1>
                <p class="mb-0 opacity-75">${crop.description}</p>
            </div>
            <div class="detail-grid mb-4">
                <div class="detail-item"><label data-i18n="crop.bestSeason">${t('crop.bestSeason')}</label><strong>${crop.bestSeason}</strong></div>
                <div class="detail-item"><label data-i18n="crop.harvestDuration">${t('crop.harvestDuration')}</label><strong>${crop.harvestDuration}</strong></div>
                <div class="detail-item"><label data-i18n="crop.suitableSoil">${t('crop.suitableSoil')}</label><strong>${crop.suitableSoil}</strong></div>
                <div class="detail-item"><label data-i18n="crop.waterReq">${t('crop.waterReq')}</label><strong>${crop.irrigationSchedule?.split('.')[0] || crop.irrigationSchedule}</strong></div>
            </div>
            <div class="row g-4">
                <div class="col-md-6">
                    <div class="content-card">
                        <h5 class="fw-bold">🌱 ${t('crop.growthStages')}</h5>
                        <ol class="mb-0">${stages.map(s => `<li>${s.trim()}</li>`).join('')}</ol>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="content-card">
                        <h5 class="fw-bold">💧 ${t('crop.waterReq')}</h5>
                        <p class="mb-0">${crop.irrigationSchedule}</p>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="content-card">
                        <h5 class="fw-bold">🧪 ${t('crop.fertilizerSchedule')}</h5>
                        <p class="mb-0">${crop.fertilizerSchedule}</p>
                        <a href="fertilizer-calculator.html" class="btn btn-sm btn-outline-success mt-2">${t('crop.calcLink')}</a>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="content-card">
                        <h5 class="fw-bold">🦠 ${t('crop.commonDiseases')}</h5>
                        <ul class="mb-0">${(crop.diseases || []).map(d => `<li>${d}</li>`).join('') || '<li>None listed</li>'}</ul>
                    </div>
                </div>
            </div>
        `;
    } catch {
        container.innerHTML = '<p class="text-danger">Crop guide not found.</p>';
    }
}

/* ---- Disease Detection ---- */
function handleDiseaseForm() {
    if (getPageName() !== 'disease') return;
    const form = document.getElementById('diseaseForm');
    const predictionCard = document.getElementById('predictionCard');
    const preview = document.getElementById('imagePreview');
    if (!form || !predictionCard) return;

    const fileInput = document.getElementById('imageUpload');
    if (fileInput && preview) {
        fileInput.addEventListener('change', () => {
            const file = fileInput.files[0];
            if (file) {
                preview.src = URL.createObjectURL(file);
                preview.classList.remove('d-none');
            }
        });
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const file = fileInput?.files[0];
        if (!file) return;

        predictionCard.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-success"></div><p class="mt-2 text-muted">' + t('disease.analyzing') + '</p></div>';

        try {
            const formData = new FormData();
            formData.append('image', file);
            const response = await fetch(apiUrl('/disease/detect'), { method: 'POST', body: formData });
            if (!response.ok) throw new Error('Detection failed');
            const result = await response.json();

            predictionCard.innerHTML = `
                <span class="prediction-badge">AI Placeholder — ${result.confidence}% confidence</span>
                <h4 class="fw-bold text-success">${result.diseaseName}</h4>
                <p class="text-muted">${result.description}</p>
                ${result.symptoms?.length ? `<h6 class="fw-bold mt-3">${t('disease.symptoms')}</h6><ul>${result.symptoms.map(s => `<li>${s}</li>`).join('')}</ul>` : ''}
                <h6 class="fw-bold mt-3">${t('disease.recFert')}</h6>
                <ul>${result.recommendedFertilizers.map(f => `<li>${f}</li>`).join('')}</ul>
                <h6 class="fw-bold">${t('disease.recPest')}</h6>
                <ul>${result.recommendedPesticides.map(p => `<li>${p}</li>`).join('')}</ul>
                <h6 class="fw-bold">${t('disease.prevention')}</h6>
                <ul>${result.preventionTips.map(tip => `<li>${tip}</li>`).join('')}</ul>
                <div class="d-flex gap-2 flex-wrap mt-3">
                    <a href="expert.html" class="btn btn-outline-success">${t('disease.requestExpert')}</a>
                    <a href="products.html" class="btn btn-success">${t('disease.searchProducts')}</a>
                </div>
            `;
        } catch {
            predictionCard.innerHTML = '<p class="text-danger">Analysis failed. Ensure the backend is running.</p>';
        }
    });
}

/* ---- Crop Recommendation ---- */
function handleCropRecForm() {
    if (getPageName() !== 'crop-recommend') return;
    const form = document.getElementById('cropRecForm');
    const resultCard = document.getElementById('cropRecResult');
    if (!form || !resultCard) return;

    const estimateBtn = document.getElementById('estimateWaterBtn');
    const estimateInfo = document.getElementById('waterEstimateInfo');
    if (estimateBtn && estimateInfo) {
        estimateBtn.addEventListener('click', async () => {
            const location = document.getElementById('recLocation').value.trim();
            if (!location) {
                estimateInfo.innerHTML = '<span class="text-danger">Enter a farm location first.</span>';
                return;
            }
            estimateInfo.innerHTML = '<span class="text-muted">Estimating water availability from weather data...</span>';
            try {
                const data = await fetchJson(`/weather/estimate?location=${encodeURIComponent(location)}`);
                const waterSelect = document.getElementById('recWater');
                if (waterSelect) waterSelect.value = data.estimatedWaterAvailability;
                estimateInfo.innerHTML = `
                    <div class="alert alert-success py-2 px-3 mb-0">
                        <strong>Estimated Water Availability: ${data.estimatedWaterAvailability}</strong> (prefilled above — you can still change it manually)<br>
                        Resolved location: ${data.resolvedName || location}<br>
                        Recent rainfall (last ${data.recentRainfallMm !== null ? 'few' : ''} days): ${data.recentRainfallMm} mm &nbsp;|&nbsp; Forecast rainfall: ${data.forecastRainfallMm} mm<br>
                        <span class="text-muted">${data.disclaimer}</span>
                    </div>
                `;
            } catch (err) {
                estimateInfo.innerHTML = '<span class="text-danger">Could not estimate water availability for this location. Please select it manually below, or try adding district/state to the location.</span>';
            }
        });
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const payload = {
            soilType: document.getElementById('recSoilType').value,
            season: document.getElementById('recSeason').value,
            waterAvailability: document.getElementById('recWater').value,
            farmLocation: document.getElementById('recLocation').value || null,
            previousCrop: document.getElementById('recPrevCrop').value || null,
            irrigationSource: document.getElementById('recIrrigation').value || null,
            ph: document.getElementById('recPh').value ? parseFloat(document.getElementById('recPh').value) : null,
            nitrogen: document.getElementById('recN').value ? parseFloat(document.getElementById('recN').value) : null,
            phosphorus: document.getElementById('recP').value ? parseFloat(document.getElementById('recP').value) : null,
            potassium: document.getElementById('recK').value ? parseFloat(document.getElementById('recK').value) : null
        };

        resultCard.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-success"></div><p class="mt-2 text-muted">Calculating suitability...</p></div>';

        try {
            const data = await postJson('/recommendations/crop', payload);
            const recs = data.recommendations || [];

            if (!recs.length) {
                resultCard.innerHTML = `
                    <h4 class="fw-bold">Recommended Crops</h4>
                    <p class="text-danger mb-0">No matching crops found. Try different inputs, or add more crops via the admin panel.</p>
                `;
                return;
            }

            resultCard.innerHTML = `
                <h4 class="fw-bold">Recommended Crops</h4>
                <p class="small text-muted">${data.disclaimer}</p>
                ${recs.map((r, idx) => `
                    <div class="content-card mb-3" style="border:1px solid #ddd;">
                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <h5 class="fw-bold mb-0">#${idx + 1} ${r.cropName} <span class="text-muted small">(${r.category || ''})</span></h5>
                            <span class="prediction-badge">Suitability Score: ${r.suitabilityScore}%</span>
                        </div>
                        <p class="mb-2 mt-2">${r.reason}</p>
                        ${r.matchedFactors && r.matchedFactors.length ? `<h6 class="fw-bold mt-2 mb-1">Matched Factors</h6><ul class="mb-2">${r.matchedFactors.map(f => `<li>${f}</li>`).join('')}</ul>` : ''}
                        ${r.notes && r.notes.length ? `<h6 class="fw-bold mt-2 mb-1">Notes</h6><ul class="mb-2 small text-muted">${r.notes.map(n => `<li>${n}</li>`).join('')}</ul>` : ''}
                        <div class="d-flex gap-3 small text-muted flex-wrap">
                            <span><strong>Water requirement:</strong> ${r.waterRequirement || 'N/A'}</span>
                            <span><strong>Expected duration:</strong> ${r.expectedDuration || 'N/A'}</span>
                        </div>
                        <a href="products.html?category=Fertilizers" class="btn btn-outline-success btn-sm mt-3">Browse Inputs for This Crop</a>
                        <a href="crop-calendar.html?crop=${encodeURIComponent(r.cropName)}" class="btn btn-outline-success btn-sm mt-3">📅 Create Crop Calendar</a>
                    </div>
                `).join('')}
            `;
        } catch (err) {
            resultCard.innerHTML = '<p class="text-danger">Could not get recommendations. Ensure the backend is running.</p>';
        }
    });
}

/* ---- Crop Calendar ---- */
function statusColorClasses(color) {
    switch (color) {
        case 'green': return { border: 'border-success', badge: 'bg-success' };
        case 'orange': return { border: 'border-warning', badge: 'bg-warning text-dark' };
        case 'red': return { border: 'border-danger', badge: 'bg-danger' };
        default: return { border: 'border-secondary', badge: 'bg-secondary' };
    }
}

function handleCropCalendarForm() {
    if (window.AGRIHUB_ADVANCED_CALENDAR) return;
    if (getPageName() !== 'crop-calendar') return;
    const form = document.getElementById('cropCalForm');
    const resultCard = document.getElementById('cropCalResult');
    if (!form || !resultCard) return;

    const params = new URLSearchParams(window.location.search);
    const cropFromQuery = params.get('crop');
    if (cropFromQuery) {
        const cropSelect = document.getElementById('calCrop');
        const options = Array.from(cropSelect.options).map(o => o.value);
        if (options.includes(cropFromQuery)) {
            cropSelect.value = cropFromQuery;
        }
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const payload = {
            crop: document.getElementById('calCrop').value,
            sowingDate: document.getElementById('calSowingDate').value,
            location: document.getElementById('calLocation').value || null,
            irrigationSource: document.getElementById('calIrrigation').value || null
        };

        resultCard.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-success"></div><p class="mt-2 text-muted">Generating calendar...</p></div>';

        try {
            const data = await postJson('/crop-calendar/generate', payload);

            const weatherLine = data.weatherAvailable
                ? `Weather forecast available up to: <strong>${data.forecastAvailableUntil}</strong>${data.resolvedLocation ? ` (location: ${data.resolvedLocation})` : ''}`
                : 'No weather advisories for this calendar — add a valid farm location to enable them.';

            resultCard.innerHTML = `
                <h4 class="fw-bold">${data.crop} Crop Calendar</h4>
                <p class="mb-1"><strong>Sowing Date:</strong> ${data.sowingDate}</p>
                <p class="mb-1"><strong>Expected Duration:</strong> ${data.durationLabel}</p>
                <p class="mb-1"><strong>Expected Harvest:</strong> ${data.harvestStart} – ${data.harvestEnd}</p>
                <p class="small text-muted mb-3">${weatherLine}</p>
                ${data.activities.map(a => {
                    const c = statusColorClasses(a.statusColor);
                    return `
                        <div class="content-card mb-3 ${c.border}" style="border-width:2px;border-style:solid;">
                            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                                <h6 class="fw-bold mb-0">${a.activityName} <span class="text-muted small">(${a.dayAfterSowingLabel})</span></h6>
                                <span class="badge ${c.badge}">${a.plannedDate}</span>
                            </div>
                            ${a.guidance ? `<p class="small mb-1 mt-2">${a.guidance}</p>` : ''}
                            ${a.weatherMessage ? `<p class="small mb-1">${a.weatherMessage}</p>` : ''}
                            ${a.suggestedDate ? `<p class="small mb-1"><strong>Suggested Rescheduled Date:</strong> ${a.suggestedDate}<br><span class="text-muted">${a.rescheduleReason || ''}</span></p>` : ''}
                            ${!a.suggestedDate && a.rescheduleReason ? `<p class="small text-muted mb-1">${a.rescheduleReason}</p>` : ''}
                        </div>
                    `;
                }).join('')}
                <p class="small text-muted mt-3">${data.disclaimer}</p>
            `;
        } catch (err) {
            resultCard.innerHTML = '<p class="text-danger">Could not generate the calendar. Check the crop, sowing date, and that the backend is running.</p>';
        }
    });
}

/* ---- Expert ---- */
function handleExpertForm() {
    if (getPageName() !== 'expert') return;
    const form = document.getElementById('expertForm');
    if (!form) return;

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const formData = new FormData(form);
        const payload = Object.fromEntries(formData.entries());
        const farmer = getSession('agrihubFarmer');
        if (farmer) {
            payload.farmerName = payload.farmerName || farmer.fullName;
            payload.email = payload.email || farmer.email;
            payload.phone = payload.phone || farmer.phone;
        }
        await postJson('/expert', payload);
        form.reset();
        showToast(t('toast.expertSubmitted'));
        loadExpertRequests();
    });
    loadExpertRequests();
}

async function loadExpertRequests() {
    const list = document.getElementById('expertRequestsList');
    if (!list) return;
    try {
        const requests = await fetchJson('/expert');
        const farmer = getSession('agrihubFarmer');
        const filtered = farmer?.email
            ? requests.filter(r => r.email === farmer.email)
            : requests;

        if (!filtered.length) {
            list.innerHTML = '<p class="empty-state">No expert requests yet.</p>';
            return;
        }
        list.innerHTML = filtered.map(request => `
            <div class="list-item-card mb-3">
                <div class="d-flex justify-content-between align-items-start">
                    <h6 class="fw-bold">${request.crop || 'General Query'}</h6>
                    <span class="order-status status-${request.status?.toLowerCase() || 'pending'}">${orderStatusLabel?.(request.status) || request.status}</span>
                </div>
                <p class="small text-muted mb-1">${request.question}</p>
                ${request.imageUrl ? `<img src="${request.imageUrl}" class="preview-img" alt="Uploaded">` : ''}
                <p class="small mb-0"><strong>${t('expert.expertReply')}:</strong> ${request.adminReply || t('expert.pending')}</p>
            </div>
        `).join('');
    } catch (error) {
        console.error(error);
    }
}

/* ---- Orders ---- */
async function loadOrdersPage() {
    if (getPageName() !== 'orders') return;
    renderCartSummary();

    const productList = document.getElementById('orderProducts');
    if (productList) {
        try {
            const products = await fetchJson('/products');
            productList.innerHTML = products.slice(0, 6).map(p => `
                <div class="col-md-6">
                    <div class="order-card d-flex justify-content-between align-items-center gap-2">
                        <div>
                            <h6 class="fw-bold mb-0">${p.name}</h6>
                            <small class="text-muted">${formatCurrency(p.price)}</small>
                        </div>
                        <button class="btn btn-sm btn-outline-success" onclick="addToCart('${p.id}')">Add</button>
                    </div>
                </div>
            `).join('');
        } catch (error) {
            console.error(error);
        }
    }

    const form = document.getElementById('orderForm');
    if (form) {
        const farmer = getSession('agrihubFarmer');
        if (farmer) {
            form.customerName.value = farmer.fullName || '';
            form.email.value = farmer.email || '';
            form.phone.value = farmer.phone || '';
            form.shippingAddress.value = farmer.address || farmer.village || '';
        }

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const cart = getCart();
            if (!cart.length) {
                showToast(t('toast.cartEmpty'), 'error');
                return;
            }
            const formData = new FormData(form);
            const payload = Object.fromEntries(formData.entries());
            const orderPayload = {
                customerName: payload.customerName,
                email: payload.email,
                phone: payload.phone,
                shippingAddress: payload.shippingAddress,
                paymentMethod: payload.paymentMethod || 'Cash on Delivery',
                totalAmount: cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
                items: cart.map(item => ({
                    productId: item.productId,
                    productName: item.name,
                    price: item.price,
                    quantity: item.quantity
                }))
            };
            await postJson('/orders', orderPayload);
            saveCart([]);
            renderCartSummary();
            form.reset();
            showToast(t('toast.orderPlaced'));
            loadOrderHistory();
        });
    }

    loadOrderHistory();
}

async function loadOrderHistory() {
    const list = document.getElementById('orderHistory');
    if (!list) return;
    try {
        const orders = await fetchJson('/orders');
        const farmer = getSession('agrihubFarmer');
        const filtered = farmer?.email
            ? orders.filter(o => o.email === farmer.email)
            : orders;

        if (!filtered.length) {
            list.innerHTML = '<p class="empty-state">No orders yet.</p>';
            return;
        }

        list.innerHTML = filtered.map(order => `
            <div class="list-item-card mb-3">
                <div class="d-flex flex-wrap justify-content-between gap-2 mb-2">
                    <strong>Order #${order.id?.substring(0, 8)}</strong>
                    <span class="order-status status-${order.status?.toLowerCase()}">${orderStatusLabel?.(order.status) || order.status}</span>
                </div>
                <p class="small mb-1">${order.items?.map(i => `${i.productName} × ${i.quantity}`).join(', ')}</p>
                <p class="small mb-1"><strong>${t('orders.total')}:</strong> ${formatCurrency(order.totalAmount)}</p>
                <p class="small mb-0"><strong>${t('orders.tracking')}:</strong> ${order.trackingNumber || 'N/A'}</p>
            </div>
        `).join('');
    } catch (error) {
        console.error(error);
    }
}

/* ---- Profile ---- */
async function loadProfilePage() {
    if (getPageName() !== 'profile') return;
    const farmer = getSession('agrihubFarmer');
    const form = document.getElementById('profileForm');
    const guest = document.getElementById('profileGuest');

    if (!farmer) {
        if (guest) guest.classList.remove('d-none');
        if (form) form.classList.add('d-none');
        return;
    }

    if (guest) guest.classList.add('d-none');
    if (!form) return;
    form.classList.remove('d-none');

    const setField = (name, value) => {
        const el = form.querySelector(`[name="${name}"]`);
        if (el) el.value = value || '';
    };

    try {
        const data = await fetchJson(`/farmers/${farmer.id}`);
        setField('fullName', data.fullName);
        setField('email', data.email);
        setField('phone', data.phone);
        setField('village', data.village);
        setField('farmSize', data.farmSize);
        setField('crops', (data.crops || []).join(', '));
        setField('address', data.address);
    } catch {
        setField('fullName', farmer.fullName);
        setField('email', farmer.email);
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(form);
        const payload = {
            fullName: formData.get('fullName'),
            email: formData.get('email'),
            phone: formData.get('phone'),
            village: formData.get('village'),
            farmSize: formData.get('farmSize'),
            address: formData.get('address'),
            crops: formData.get('crops').split(',').map(c => c.trim()).filter(Boolean)
        };
        const updated = await putJson(`/farmers/${farmer.id}`, payload);
        setSession('agrihubFarmer', updated);
        showToast('Profile updated');
        renderSiteHeader();
    });
}

/* ---- Login / Register ---- */
function handleLogin() {
    if (getPageName() !== 'login') return;
    const form = document.getElementById('loginForm');
    if (!form) return;
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(form));
        try {
            const farmer = await fetchJson('/farmers/login', { method: 'POST', body: JSON.stringify(payload) });
            setSession('agrihubFarmer', farmer);
            showToast('Welcome back!');
            window.location.href = 'index.html';
        } catch {
            showToast('Invalid credentials', 'error');
        }
    });
}

function handleRegister() {
    if (getPageName() !== 'register') return;
    const form = document.getElementById('registerForm');
    if (!form) return;
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(form));
        payload.crops = payload.crops.split(',').map(c => c.trim()).filter(Boolean);
        try {
            const farmer = await postJson('/farmers', payload);
            setSession('agrihubFarmer', farmer);
            showToast('Registration successful!');
            window.location.href = 'profile.html';
        } catch {
            showToast('Registration failed', 'error');
        }
    });
}

function handleAdminLogin() {
    if (getPageName() !== 'admin-login') return;
    const form = document.getElementById('adminLoginForm');
    if (!form) return;
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(form));
        try {
            const user = await fetchJson('/users/login', { method: 'POST', body: JSON.stringify(payload) });
            if (user.role !== 'admin') {
                showToast('Admin access only', 'error');
                return;
            }
            setSession('agrihubAdmin', user);
            window.location.href = 'admin.html';
        } catch {
            showToast('Invalid admin credentials', 'error');
        }
    });
}

function handleAdminRegister() {
    if (getPageName() !== 'admin-register') return;
    const form = document.getElementById('adminRegisterForm');
    if (!form) return;
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const payload = Object.fromEntries(new FormData(form));
        payload.role = "admin";
        try {
            const user = await postJson('/users/register', payload);
            setSession('agrihubAdmin', user);
            showToast('Admin registration successful!');
            window.location.href = 'admin.html';
        } catch (error) {
            showToast(error.message || 'Registration failed', 'error');
        }
    });
}

function handleContactForm() {
    const form = document.getElementById('contactForm');
    if (!form) return;
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        form.reset();
        showToast(t('toast.messageSent'));
    });
}

document.addEventListener('DOMContentLoaded', () => {
    loadHomePage();
    loadProducts();
    loadProductDetail();
    loadCropGuides();
    loadCropDetail();
    handleDiseaseForm();
    handleCropRecForm();
    handleCropCalendarForm();
    handleExpertForm();
    loadOrdersPage();
    loadProfilePage();
    handleLogin();
    handleRegister();
    handleAdminLogin();
    handleAdminRegister();
    handleContactForm();
});
