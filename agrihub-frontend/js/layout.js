const NAV_ITEMS = [
    { page: 'home', labelKey: 'nav.home', href: 'index.html', icon: '🏠' },
    { page: 'products', labelKey: 'nav.products', href: 'products.html', icon: '🛒' },
    { page: 'crop-guide', labelKey: 'nav.cropGuide', href: 'crop-guide.html', icon: '🌱' },
    { page: 'crop-recommend', labelKey: 'nav.cropRecommend', href: 'crop-recommendation.html', icon: '🧭' },
    { page: 'crop-calendar', labelKey: 'nav.cropCalendar', href: 'crop-calendar.html', icon: '📅' },
    { page: 'disease', labelKey: 'nav.disease', href: 'disease.html', icon: '🔬' },
    { page: 'expert', labelKey: 'nav.expert', href: 'expert.html', icon: '👨‍🌾' },
    { page: 'orders', labelKey: 'nav.orders', href: 'orders.html', icon: '📦' },
    { page: 'profile', labelKey: 'nav.profile', href: 'profile.html', icon: '👤' }
];

function renderSiteHeader() {
    const header = document.getElementById('site-header');
    if (!header) return;

    const currentPage = document.body.dataset.page || 'home';
    const farmer = getSession('agrihubFarmer');
    const admin = getSession('agrihubAdmin');
    const navLinks = NAV_ITEMS.map(item => `
        <li class="nav-item">
            <a class="nav-link${currentPage === item.page ? ' active' : ''}" href="${item.href}">
                <span class="nav-icon" aria-hidden="true">${item.icon}</span><span data-i18n="${item.labelKey}">${t(item.labelKey)}</span>
            </a>
        </li>
    `).join('');

    const unread = getNotifications?.().filter(n => !n.read).length || 0;

    header.innerHTML = `
        <nav class="navbar navbar-expand-lg site-navbar">
            <div class="container">
                <a class="navbar-brand" href="index.html">
                    <span class="brand-icon" aria-hidden="true">🌾</span>
                    <span class="brand-text">AgriHub</span>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"
                        aria-controls="mainNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="mainNav">
                    <ul class="navbar-nav ms-auto align-items-lg-center gap-lg-1">${navLinks}</ul>
                    <div class="d-flex align-items-center gap-2 ms-lg-3 mt-3 mt-lg-0 flex-wrap">
                        <select id="langSelect" class="form-select form-select-sm lang-select" aria-label="${t('common.language')}">
                            <option value="en">English</option>
                            <option value="te">తెలుగు</option>
                            <option value="hi">हिन्दी</option>
                        </select>
                        <div class="dropdown notif-dropdown">
                            <button class="btn btn-sm btn-outline-success notif-btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-bell"></i>
                                <span id="notifBadge" class="notif-badge${unread ? '' : ' d-none'}">${unread}</span>
                            </button>
                            <div class="dropdown-menu dropdown-menu-end notif-menu">
                                <div class="notif-header">
                                    <strong data-i18n="common.notifications">${t('common.notifications')}</strong>
                                    <button type="button" class="btn btn-link btn-sm p-0" id="markNotifRead" data-i18n="common.markAllRead">${t('common.markAllRead')}</button>
                                </div>
                                <div id="notifList"></div>
                            </div>
                        </div>
                        ${farmer
                            ? `<div class="d-flex align-items-center gap-2">
                                <span class="nav-user-badge">${t('nav.hi')}, ${farmer.fullName?.split(' ')[0] || 'Farmer'}</span>
                                <button id="farmerLogout" class="btn btn-sm btn-outline-danger" data-i18n="admin.logout">${t('admin.logout')}</button>
                               </div>`
                            : admin
                                ? `<div class="d-flex align-items-center gap-2">
                                    <span class="nav-user-badge bg-primary text-white">${t('nav.hi')}, Admin</span>
                                    <button id="adminLogoutHeader" class="btn btn-sm btn-outline-danger" data-i18n="admin.logout">${t('admin.logout')}</button>
                                   </div>`
                                : `<a href="login.html" class="btn btn-sm btn-outline-success" data-i18n="auth.login">${t('auth.login')}</a>`}
                        ${admin
                            ? `<a href="admin.html" class="btn btn-sm btn-success">Admin Panel</a>`
                            : `<a href="admin-login.html" class="btn btn-sm btn-success" data-i18n="auth.adminLogin">${t('auth.adminLogin')}</a>`}
                    </div>
                </div>
            </div>
        </nav>
    `;

    const langSelect = document.getElementById('langSelect');
    if (langSelect) {
        langSelect.value = getLang();
        langSelect.addEventListener('change', () => setLang(langSelect.value));
    }
    const farmerLogout = document.getElementById('farmerLogout');
    if (farmerLogout) {
        farmerLogout.addEventListener('click', (e) => {
            e.preventDefault();
            clearSession('agrihubFarmer');
            showToast('Logged out successfully');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        });
    }
    const adminLogoutHeader = document.getElementById('adminLogoutHeader');
    if (adminLogoutHeader) {
        adminLogoutHeader.addEventListener('click', (e) => {
            e.preventDefault();
            clearSession('agrihubAdmin');
            showToast('Logged out successfully');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        });
    }
    renderNotificationDropdown?.();
    updateNotificationBadge?.();
}

function renderSiteFooter() {
    const footer = document.getElementById('site-footer');
    if (!footer) return;

    footer.innerHTML = `
        <footer class="site-footer">
            <div class="container">
                <div class="row g-4 align-items-start">
                    <div class="col-lg-4">
                        <div class="footer-brand"><span aria-hidden="true">🌾</span> AgriHub</div>
                        <p class="footer-tagline" data-i18n="footer.tagline">${t('footer.tagline')}</p>
                        <div class="footer-social">
                            <span>📍 Rampur, India</span>
                            <span>📞 +91 98765 43210</span>
                            <span>✉️ support@agrihub.com</span>
                        </div>
                    </div>
                    <div class="col-6 col-lg-2">
                        <h6 class="footer-heading" data-i18n="footer.explore">${t('footer.explore')}</h6>
                        <ul class="footer-links">
                            <li><a href="products.html" data-i18n="nav.products">${t('nav.products')}</a></li>
                            <li><a href="crop-guide.html" data-i18n="nav.cropGuide">${t('nav.cropGuide')}</a></li>
                            <li><a href="disease.html" data-i18n="nav.disease">${t('nav.disease')}</a></li>
                        </ul>
                    </div>
                    <div class="col-6 col-lg-2">
                        <h6 class="footer-heading" data-i18n="footer.support">${t('footer.support')}</h6>
                        <ul class="footer-links">
                            <li><a href="expert.html" data-i18n="nav.expert">${t('nav.expert')}</a></li>
                            <li><a href="orders.html" data-i18n="nav.orders">${t('nav.orders')}</a></li>
                            <li><a href="profile.html" data-i18n="nav.profile">${t('nav.profile')}</a></li>
                        </ul>
                    </div>
                    <div class="col-lg-4">
                        <h6 class="footer-heading" data-i18n="footer.schemes">${t('footer.schemes')}</h6>
                        <ul class="footer-links">
                            <li><a href="#schemes">PM-KISAN</a></li>
                            <li><a href="#schemes">Soil Health Card</a></li>
                            <li><a href="#schemes">KCC — Kisan Credit Card</a></li>
                        </ul>
                    </div>
                </div>
                <div class="footer-bottom d-flex flex-wrap justify-content-between gap-2">
                    <span>&copy; ${new Date().getFullYear()} AgriHub. Final Year B.Tech Project.</span>
                    <span data-i18n="footer.builtWith">${t('footer.builtWith')}</span>
                </div>
            </div>
        </footer>
    `;
}

document.addEventListener('DOMContentLoaded', () => {
    renderSiteHeader();
    renderSiteFooter();
});

document.addEventListener('agrihub:langchange', () => {
    renderSiteHeader();
    renderSiteFooter();
    applyTranslations();
});
