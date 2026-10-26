# AgriHub Dynamic Crop Weather Calendar — Implementation Notes

This project enhances the already-working crop calendar; it does **not** rebuild AgriHub or remove the existing activity schedule.

## 1) Files modified

Backend:
- `agrihub-backend/src/main/java/com/agrihub/model/CropCalendarActivity.java`
- `agrihub-backend/src/main/java/com/agrihub/model/CropCalendarRequest.java`
- `agrihub-backend/src/main/java/com/agrihub/model/CropCalendarResponse.java`
- `agrihub-backend/src/main/java/com/agrihub/model/DailyWeather.java`
- `agrihub-backend/src/main/java/com/agrihub/service/CropCalendarService.java`
- `agrihub-backend/src/main/java/com/agrihub/service/WeatherService.java`
- `agrihub-backend/src/main/resources/static/crop-calendar.html`
- `agrihub-backend/src/main/resources/static/js/app.js` (only disables the legacy handler when the advanced page is loaded)

Standalone frontend mirror:
- `agrihub-frontend/crop-calendar.html`
- `agrihub-frontend/js/app.js`

## 2) Files created

Backend models / configuration:
- `model/CropWeatherSummary.java`
- `model/WeatherThresholdInfo.java`
- `model/WeatherAlert.java`
- `model/WaterStatus.java`
- `model/StageWaterStatus.java`
- `service/CropWeatherProfileRegistry.java`

Frontend:
- `static/css/crop-calendar.css`
- `static/js/crop-calendar.js`
- mirrored copies under `agrihub-frontend/css/` and `agrihub-frontend/js/`

## 3) Main features added

- State, District and Farm Location / Village inputs
- Crop + Short / Medium / Long duration profile
- Sowing date, season and irrigation source
- Crop summary and harvest window
- Crop-specific GDD base temperature
- Historical + forecast GDD accumulation
- Estimated crop stage from crop-specific configurable GDD thresholds
- Max temperature, min temperature, rainfall and accumulated GDD cards
- 7-day weather table with humidity, wind, daily GDD, stage and advisory
- Stage-based configurable weather thresholds
- High-temperature, low-temperature, rainfall, humidity, wind and irrigation advisories
- Open-Meteo ET0-based **Prototype Estimated Water Requirement**
- Water deficit = `max(ET0 - rainfall, 0)`
- Stage-based water summary table for stages with real past weather data
- Existing sowing / inspection / irrigation / fertilizer / weeding / pest monitoring / spraying / harvest calendar retained
- Activity cards now show crop stage, weather status, advisory and suggested reschedule date when available
- No future weather is fabricated outside Open-Meteo's returned forecast range

## 4) GDD formula

```text
Daily GDD = ((Tmax + Tmin) / 2) - Tbase
If Daily GDD < 0, Daily GDD = 0
Accumulated GDD = sum of Daily GDD from sowing date through the available weather timeline
```

The calculation runs in the Spring Boot backend.

## 5) Crop base-temperature configuration

Central file:

`com.agrihub.service.CropWeatherProfileRegistry`

Configured base temperatures:

| Crop | Base temperature used |
|---|---:|
| Groundnut / Peanut | 13.3 °C (56 °F) |
| Rice | 10.0 °C (50 °F) |
| Cotton | 15.6 °C (60 °F) |
| Maize / Corn | 10.0 °C (50 °F) |
| Tomato | 10.0 °C (50 °F) |
| Chilli / Pepper | 10.0 °C (50 °F) |

Reference basis used for these base temperatures:
- UF/IFAS Degree-Days reference table: `https://ask.ifas.ufl.edu/publication/AE428`
- Midwestern Regional Climate Center crop base-temperature table: `https://mrcc.purdue.edu/resources/growing-degree-day-description`
- Cotton DD60 extension references (base 60 °F)

The crop-stage GDD boundaries are **prototype configuration values**, not universal official standards.

## 6) Crop-stage threshold configuration

Also centralized in `CropWeatherProfileRegistry`.

Every crop has its own ordered stages. Each stage stores:
- start GDD
- maximum temperature threshold
- minimum temperature threshold
- rainfall threshold
- minimum humidity threshold
- maximum humidity threshold

These values are deliberately labelled **configurable prototype agronomic values**. They must be validated for variety, local region and agricultural-extension recommendations before field deployment.

## 7) Weather API parameters

Open-Meteo Forecast API:

```text
daily=temperature_2m_max,
      temperature_2m_min,
      precipitation_sum,
      wind_speed_10m_max,
      et0_fao_evapotranspiration
hourly=relative_humidity_2m
past_days=5
forecast_days=<configured forecast window, max 16>
timezone=auto
```

Open-Meteo Historical Archive API is used for older dates from sowing date to the recent-history overlap.

Geocoding continues to use the existing Open-Meteo geocoder. The updated resolver also retries simplified location forms if the full `Village, District, State` query is not found.

## 8) Water-deficit calculation

The project does **not** claim exact crop evapotranspiration.

For the prototype dashboard:

```text
Prototype Estimated Water Requirement = sum of Open-Meteo FAO reference ET0
Rainfall Received = sum of Open-Meteo precipitation
Estimated Water Deficit = max(Estimated Water Requirement - Rainfall Received, 0)
```

This is labelled clearly in the UI and API response. A validated crop coefficient (`Kc`) / ETc method can be added later after agronomic validation.

## 9) Exact Windows PowerShell commands to run

### Start MongoDB (Administrator PowerShell)

```powershell
Start-Process powershell -Verb RunAs
```

In the new Administrator window:

```powershell
net start MongoDB
Get-Service MongoDB
```

`Status` should be `Running`.

### Run AgriHub on port 8081

Extract the ZIP, then:

```powershell
cd "C:\Users\Vanitha\Downloads\AgriHub-Dynamic-Crop-Weather-Calendar-Complete\AgriHub\agrihub-backend"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Expected ending:

```text
Tomcat started on port 8081
Started AgrihubBackendApplication
```

## 10) URL to test

```text
http://localhost:8081/crop-calendar.html
```

Press `Ctrl + F5` after opening it to avoid cached JavaScript/CSS.

Example API test in PowerShell:

```powershell
$body = @{
    crop = "Cotton"
    cropVarietyDuration = "Medium Duration"
    sowingDate = "2026-09-10"
    season = "Kharif"
    state = "Andhra Pradesh"
    district = "NTR"
    location = "Kondapalli"
    irrigationSource = "Borewell"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8081/crop-calendar/generate" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

## 11) Response structure

The enhanced response contains:

```text
crop
cropVarietyDuration
season
resolvedLocation
sowingDate
durationLabel
harvestStart / harvestEnd
daysAfterSowing
currentStage
baseTemperature
accumulatedGdd
currentWeatherSummary
  maxTemp
  minTemp
  rainfall
  humidity
  windSpeed
dailyForecast[]
  date
  temperatureMaxC
  temperatureMinC
  precipitationMm
  humidityMeanPct
  windSpeedMaxKmh
  dailyGdd
  cumulativeGdd
  cropStage
  advisory
currentStageThresholds
weatherAlerts[]
waterStatus
stageWaterTable[]
activities[]
```

Actual values depend on the location, date and live Open-Meteo response. The code does not insert sample weather values into the live result.

## 12) What was tested in the build workspace

Passed:
- JavaScript syntax check with `node --check` for `crop-calendar.js` and the modified `app.js`
- Java source syntax/type check of the modified calendar/weather classes using minimal local framework stubs
- Backend calendar service smoke test using deterministic mock weather data: GDD, crop stage, alerts, water status and activities were generated without runtime errors

Environment limitation:
- A full Maven build could not be completed in this sandbox because the Maven wrapper attempted to download Maven from Maven Central and outbound download was unavailable. Run the normal `mvnw.cmd` command on the project machine, where this project was already running previously.
- Live Open-Meteo calls were not executed from the sandbox. They must be tested on the local machine with internet access.

## 13) Items that still require agronomic validation

Before presenting any values as official field recommendations, validate:
- crop-stage GDD thresholds per crop/variety and region
- stage-specific max/min temperature thresholds
- stage-specific rainfall and humidity thresholds
- existing crop activity timing templates
- interpretation of ET0 as a prototype water-requirement indicator
- duration-profile adjustments (Short / Medium / Long) for specific varieties

The UI intentionally labels these as **Estimated**, **Prototype**, **Forecast**, or **Advisory** rather than official standards.
