/**
 * Central API configuration for AgriHub.
 *
 * Recommended Cloudflare setup:
 * expose the Spring Boot app on port 8080 with cloudflared. Spring serves the
 * frontend and API from the same public URL, so no hardcoded tunnel URL is needed.
 *
 * For a separate frontend server, set window.AGRIHUB_API_BASE_URL before this
 * file loads, or run this in DevTools:
 * localStorage.setItem('agrihubApiBaseUrl', 'https://your-backend-url')
 */
const API_BASE_URL = '';

function getApiBaseUrl() {
    const configuredBaseUrl = window.AGRIHUB_API_BASE_URL
        || localStorage.getItem('agrihubApiBaseUrl')
        || API_BASE_URL;

    if (configuredBaseUrl) {
        return configuredBaseUrl.replace(/\/$/, '');
    }

    if (window.location.protocol === 'file:') {
        return 'http://localhost:8080';
    }

    const isLocalStaticServer = ['localhost', '127.0.0.1'].includes(window.location.hostname)
        && window.location.port
        && window.location.port !== '8080';

    if (isLocalStaticServer) {
        return 'http://localhost:8080';
    }

    return window.location.origin;
}

function apiUrl(path) {
    const normalized = path.startsWith('/') ? path : `/${path}`;
    return `${getApiBaseUrl()}${normalized}`;
}

function getSession(key) {
    try {
        return JSON.parse(localStorage.getItem(key));
    } catch {
        return null;
    }
}

function setSession(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
}

function clearSession(key) {
    localStorage.removeItem(key);
}

function formatCurrency(amount) {
    return `INR ${Number(amount).toLocaleString('en-IN')}`;
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `agri-toast agri-toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add('show'));
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 2800);
}
