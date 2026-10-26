window.AGRIHUB_ADVANCED_CALENDAR = true;

(function () {
    const esc = (value) => String(value ?? '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#039;');

    const num = (value, unit = '', fallback = 'N/A') => {
        if (value === null || value === undefined || Number.isNaN(Number(value))) return fallback;
        return `${Number(value).toFixed(1)}${unit}`;
    };

    const statusClass = (color) => ['green', 'orange', 'red', 'blue'].includes(color) ? color : 'blue';

    document.addEventListener('DOMContentLoaded', () => {
        if (document.body?.dataset?.page !== 'crop-calendar') return;
        const form = document.getElementById('cropCalForm');
        if (!form) return;

        const cropFromQuery = new URLSearchParams(window.location.search).get('crop');
        if (cropFromQuery) {
            const select = document.getElementById('calCrop');
            const matching = Array.from(select.options).find(o => o.value.toLowerCase() === cropFromQuery.toLowerCase());
            if (matching) select.value = matching.value;
        }

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            showLoading();

            const payload = {
                state: document.getElementById('calState').value || null,
                district: document.getElementById('calDistrict').value || null,
                location: document.getElementById('calLocation').value || null,
                crop: document.getElementById('calCrop').value,
                cropVarietyDuration: document.getElementById('calDuration').value,
                sowingDate: document.getElementById('calSowingDate').value,
                season: document.getElementById('calSeason').value,
                irrigationSource: document.getElementById('calIrrigation').value || null
            };

            try {
                const data = await postJson('/crop-calendar/generate', payload);
                renderDashboard(data);
            } catch (error) {
                const summary = document.getElementById('calendarSummaryCard');
                summary.innerHTML = `
                    <span class="section-kicker">ERROR</span>
                    <h4 class="fw-bold">Could not generate the calendar</h4>
                    <p class="text-danger mb-2">${esc(error?.message || 'Check the backend, crop, sowing date and location.')}</p>
                    <p class="small text-muted mb-0">The backend should be running and MongoDB should be connected. Weather sections also require internet access to Open-Meteo.</p>`;
            }
        });
    });

    function showLoading() {
        document.getElementById('cropWeatherResults').classList.add('d-none');
        document.getElementById('calendarSummaryCard').innerHTML = `
            <div class="loading-panel">
                <div class="spinner-border text-success" role="status"></div>
                <p class="mt-3 mb-0 text-muted">Building crop calendar and retrieving weather data...</p>
            </div>`;
    }

    function renderDashboard(data) {
        renderSummary(data);
        renderWeatherCards(data);
        renderStage(data);
        renderAlerts(data);
        renderForecast(data);
        renderThresholds(data);
        renderWaterStatus(data);
        renderStageWaterTable(data);
        renderActivities(data);
        document.getElementById('cropWeatherResults').classList.remove('d-none');
    }

    function renderSummary(data) {
        const location = data.resolvedLocation || data.queriedLocation || 'No weather location provided';
        const weatherNote = data.weatherAvailable
            ? `<span class="text-success fw-semibold">Weather connected</span>${data.forecastAvailableUntil ? ` · Forecast available until ${esc(data.forecastAvailableUntil)}` : ''}`
            : `<span class="text-warning fw-semibold">Weather unavailable</span> · Activity calendar still generated`;

        document.getElementById('calendarSummaryCard').innerHTML = `
            <div class="section-heading">
                <div>
                    <span class="section-kicker">CURRENT CROP SUMMARY</span>
                    <h3 class="fw-bold mb-1">${esc(data.crop)}</h3>
                </div>
                <span class="data-source-chip">${esc(data.season || 'Season not specified')}</span>
            </div>
            <p class="small text-muted mb-0">${weatherNote}</p>
            <div class="summary-grid">
                <div class="summary-item"><span>Location</span><strong>${esc(location)}</strong></div>
                <div class="summary-item"><span>Sowing Date</span><strong>${esc(data.sowingDate)}</strong></div>
                <div class="summary-item"><span>Duration Profile</span><strong>${esc(data.cropVarietyDuration || 'Medium Duration')}</strong></div>
                <div class="summary-item"><span>Expected Duration</span><strong>${esc(data.durationLabel)}</strong></div>
                <div class="summary-item"><span>Expected Harvest</span><strong>${esc(data.harvestStart)} – ${esc(data.harvestEnd)}</strong></div>
                <div class="summary-item"><span>GDD Base Temperature</span><strong>${num(data.baseTemperature, ' °C')}</strong></div>
            </div>
            <p class="small text-muted mt-3 mb-0">${esc(data.baseTemperatureSourceNote || '')}</p>`;
    }

    function renderWeatherCards(data) {
        const w = data.currentWeatherSummary || {};
        const cards = [
            ['🌡️', 'Max Temp', num(w.maxTemp, ' °C')],
            ['🌙', 'Min Temp', num(w.minTemp, ' °C')],
            ['🌧️', 'Rainfall', num(w.rainfall, ' mm')],
            ['🌱', 'Accumulated GDD', num(data.accumulatedGdd, ' GDD')]
        ];
        document.getElementById('weatherCards').innerHTML = cards.map(c => `
            <div class="weather-metric-card">
                <div class="metric-icon">${c[0]}</div>
                <div class="metric-label">${c[1]}</div>
                <div class="metric-value">${c[2]}</div>
            </div>`).join('');
    }

    function renderStage(data) {
        document.getElementById('cropStagePanel').innerHTML = `
            <span class="section-kicker">CURRENT CROP STAGE</span>
            <h4 class="fw-bold mb-0">Estimated Crop Stage</h4>
            <div class="stage-hero">
                <div class="stage-value"><span>Current Stage</span><strong>${esc(data.currentStage || 'N/A')}</strong></div>
                <div class="stage-value"><span>Days After Sowing</span><strong>${esc(data.daysAfterSowing ?? 0)} days</strong></div>
                <div class="stage-value"><span>Accumulated GDD</span><strong>${num(data.accumulatedGdd, ' GDD')}</strong></div>
            </div>
            <p class="small text-muted mt-3 mb-0">Stage is estimated from cumulative GDD and configurable crop-specific prototype stage thresholds. It is not an exact phenological observation.</p>`;
    }

    function renderAlerts(data) {
        const alerts = data.weatherAlerts || [];
        document.getElementById('weatherAlertsPanel').innerHTML = `
            <span class="section-kicker">WEATHER ALERTS</span>
            <h4 class="fw-bold mb-0">Stage-Aware Advisories</h4>
            <div class="alert-stack">
                ${alerts.map(a => `
                    <div class="weather-alert ${esc(a.severity || 'info')}">
                        <div class="d-flex justify-content-between gap-2">
                            <span class="alert-title">${esc(a.title)}</span>
                            ${a.date ? `<span class="alert-date">${esc(a.date)}</span>` : ''}
                        </div>
                        <div class="small mt-1">${esc(a.message)}</div>
                    </div>`).join('') || '<div class="api-warning">No weather alerts available.</div>'}
            </div>`;
    }

    function renderForecast(data) {
        const rows = data.dailyForecast || [];
        const tbody = document.getElementById('forecastTableBody');
        if (!rows.length) {
            tbody.innerHTML = `<tr><td colspan="9"><div class="api-warning">Forecast table unavailable. Check the farm location and internet connection.</div></td></tr>`;
            return;
        }
        tbody.innerHTML = rows.map(r => `
            <tr>
                <td class="fw-semibold">${esc(r.date)}</td>
                <td>${num(r.temperatureMaxC, ' °C')}</td>
                <td>${num(r.temperatureMinC, ' °C')}</td>
                <td>${num(r.precipitationMm, ' mm')}</td>
                <td>${num(r.humidityMeanPct, ' %')}</td>
                <td>${num(r.windSpeedMaxKmh, ' km/h')}</td>
                <td>${num(r.dailyGdd, '')}</td>
                <td>${esc(r.cropStage || 'N/A')}</td>
                <td class="advisory-cell">${esc(r.advisory || '')}</td>
            </tr>`).join('');
    }

    function renderThresholds(data) {
        const t = data.currentStageThresholds || {};
        document.getElementById('thresholdPanel').innerHTML = `
            <span class="section-kicker">STAGE-BASED THRESHOLDS</span>
            <h4 class="fw-bold mb-1">Weather Thresholds for Current Crop Stage</h4>
            <p class="small text-muted mb-0">Stage: <strong>${esc(t.stage || data.currentStage || 'N/A')}</strong></p>
            <div class="threshold-grid">
                <div class="threshold-box"><span>Max Temperature</span><strong>${num(t.maxTemperatureC, ' °C')}</strong></div>
                <div class="threshold-box"><span>Min Temperature</span><strong>${num(t.minTemperatureC, ' °C')}</strong></div>
                <div class="threshold-box"><span>Rainfall Threshold</span><strong>${num(t.rainfallThresholdMm, ' mm')}</strong></div>
                <div class="threshold-box"><span>Humidity Range</span><strong>${num(t.minHumidityPct, '%')} – ${num(t.maxHumidityPct, '%')}</strong></div>
            </div>
            <p class="small text-muted mt-3 mb-0">${esc(t.validationNote || 'Prototype values — agronomic validation required.')}</p>`;
    }

    function renderWaterStatus(data) {
        const w = data.waterStatus;
        if (!w) {
            document.getElementById('waterStatusPanel').innerHTML = `
                <span class="section-kicker">WATER STATUS</span>
                <h4 class="fw-bold">Prototype Estimated Water Requirement</h4>
                <div class="api-warning">Water status is unavailable until historical/forecast weather data is available for the selected location.</div>`;
            return;
        }
        document.getElementById('waterStatusPanel').innerHTML = `
            <span class="section-kicker">WATER STATUS</span>
            <h4 class="fw-bold mb-1">Prototype Estimated Water Requirement</h4>
            <p class="small text-muted mb-0">Period: ${esc(w.period || '')}</p>
            <div class="water-stat-grid">
                <div class="water-stat-box"><span>Rainfall Received</span><strong>${num(w.rainfallReceived, ' mm')}</strong></div>
                <div class="water-stat-box"><span>Estimated Water Demand</span><strong>${num(w.estimatedWaterDemand, ' mm')}</strong></div>
                <div class="water-stat-box"><span>Estimated Water Deficit</span><strong>${num(w.waterDeficit, ' mm')}</strong></div>
                <div class="water-stat-box"><span>Status</span><strong>${esc(w.status || 'N/A')}</strong></div>
            </div>
            <p class="small text-muted mt-3 mb-0">${esc(w.methodology || '')}</p>`;
    }

    function renderStageWaterTable(data) {
        const rows = data.stageWaterTable || [];
        const tbody = document.getElementById('stageWaterTableBody');
        if (!rows.length) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-muted">No stage-level historical weather data available yet.</td></tr>`;
            return;
        }
        tbody.innerHTML = rows.map(r => `
            <tr>
                <td class="fw-semibold">${esc(r.cropStage)}</td>
                <td>${esc(r.period)}</td>
                <td>${num(r.estimatedWaterDemand, ' mm')}</td>
                <td>${num(r.rainfallReceived, ' mm')}</td>
                <td>${num(r.waterDeficit, ' mm')}</td>
                <td>${esc(r.status || '')}</td>
            </tr>`).join('');
    }

    function renderActivities(data) {
        const activities = data.activities || [];
        document.getElementById('forecastUntilChip').textContent = data.forecastAvailableUntil
            ? `Forecast to ${data.forecastAvailableUntil}` : 'Forecast unavailable';
        document.getElementById('activityCalendar').innerHTML = activities.map(a => `
            <article class="activity-card ${statusClass(a.statusColor)}">
                <div class="activity-head">
                    <div>
                        <h6 class="fw-bold mb-1">${esc(a.activityName)}</h6>
                        <div class="text-muted small">${esc(a.dayAfterSowingLabel || '')}</div>
                    </div>
                    <span class="activity-date">${esc(a.plannedDate)}</span>
                </div>
                <div class="activity-meta">
                    <span>Stage: ${esc(a.cropStage || 'N/A')}</span>
                    <span>Status: ${esc(a.weatherStatus || 'N/A')}</span>
                </div>
                ${a.guidance ? `<div class="activity-advisory"><strong>Activity guidance:</strong> ${esc(a.guidance)}</div>` : ''}
                ${a.weatherMessage ? `<div class="activity-advisory"><strong>Weather advisory:</strong> ${esc(a.weatherMessage)}</div>` : ''}
                ${a.suggestedDate || a.rescheduleReason ? `
                    <div class="reschedule-box">
                        ${a.suggestedDate ? `<strong>Suggested Rescheduled Date:</strong> ${esc(a.suggestedDate)}<br>` : ''}
                        ${esc(a.rescheduleReason || '')}
                    </div>` : ''}
            </article>`).join('');
        document.getElementById('calendarDisclaimer').textContent = data.disclaimer || '';
    }
})();
