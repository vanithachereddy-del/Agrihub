# AgriHub — Smart Agriculture Platform

Final Year B.Tech project connecting **farmers** with **fertilizer & pesticide shop owners** through a modern web platform.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | HTML5, CSS3, JavaScript, Bootstrap 5 |
| Backend | Spring Boot 3 (Java 21) |
| Database | MongoDB |
| Architecture | MVC — Controller → Service → Repository → Model |
| API | REST JSON |

## Features

### Farmer
- Home page with search, featured products, crop guide, schemes & contact
- Product catalog with search, filters, details, stock & cart
- Crop guide with growth stages, schedules & diseases
- AI disease detection (**placeholder** — ready for real ML model)
- Ask Expert with image support
- Orders: cart, checkout, history & tracking
- Profile: name, phone, village, crops, farm size

### Admin (Shop Owner)
- Dashboard with stats, charts & low-stock alerts
- Product CRUD & stock management
- Order accept / reject / delivered
- Expert request replies
- Registered farmers list

## Project Structure

```
AgriHub/
├── agrihub-backend/          # Spring Boot application
│   └── src/main/
│       ├── java/com/agrihub/
│       │   ├── controller/   # REST controllers
│       │   ├── service/      # Business logic
│       │   ├── repository/   # MongoDB repositories
│       │   ├── model/        # Document entities
│       │   └── config/       # CORS, data seeder
│       └── resources/
│           ├── application.properties
│           └── static/       # Frontend (HTML, CSS, JS)
└── agrihub-frontend/         # Standalone frontend copy (optional dev)
```

## MongoDB Collections

- `products` — fertilizers, pesticides, seeds, irrigation
- `farmers` — registered farmers
- `orders` — customer orders with tracking
- `expertRequests` — farmer questions & admin replies
- `crops` — crop guides
- `users` — admin / shop owner accounts

## REST API Endpoints

| Resource | Endpoints |
|----------|-----------|
| Products | `GET/POST /products`, `GET/PUT/DELETE /products/{id}` |
| Farmers | `GET/POST /farmers`, `GET/PUT /farmers/{id}`, `POST /farmers/login` |
| Orders | `GET/POST /orders`, `GET/PUT /orders/{id}` |
| Expert | `GET/POST /expert`, `PUT /expert/{id}` |
| Crops | `GET /crops`, `GET /crops/{id}` |
| Disease | `POST /disease/detect` (multipart image) |
| Dashboard | `GET /dashboard` |
| Users | `POST /users/login` (admin) |

## Prerequisites

- Java 21+
- Maven 3.8+
- MongoDB running on `localhost:27017`

## How to Run

1. **Start MongoDB**
   ```bash
   mongod
   ```

2. **Run the backend** (serves frontend at http://localhost:8080)
   ```bash
   cd agrihub-backend
   ./mvnw spring-boot:run
   ```
   On Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. **Open the app**
   - Farmer site: http://localhost:8080/index.html
   - Admin login: http://localhost:8080/admin-login.html

## Cloudflare Tunnel Public Link

Use one Cloudflare Tunnel for the Spring Boot backend on port `8080`. The backend
also serves the frontend from `src/main/resources/static`, so the same
`trycloudflare.com` public URL opens the frontend and handles API requests.

On Windows, from the `AgriHub` folder:

```bat
start-backend.bat
start-cloudflared-tunnel.bat
```

Or start both in separate command windows:

```bat
start-agrihub-public.bat
```

PowerShell users can run this version to show the Cloudflare link in the same
terminal:

```powershell
.\start-agrihub-public.ps1
```

Copy the `https://...trycloudflare.com` URL printed by cloudflared and open:

```text
https://your-cloudflare-url.trycloudflare.com/index.html
https://your-cloudflare-url.trycloudflare.com/admin-login.html
https://your-cloudflare-url.trycloudflare.com/products
```

Do not tunnel only `agrihub-frontend` on port `5500` if you want one public link
to access both frontend and backend. Tunnel `http://localhost:8080` instead.

## Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@agrihub.com | admin123 |
| Farmer | ramesh@farmer.com | farmer123 |

## AI Disease Detection (Placeholder)

The `DiseaseDetectionService` returns mock predictions based on image hash. To integrate a real model:

1. Replace logic in `com.agrihub.service.DiseaseDetectionService`
2. Call your Python/TensorFlow service or load a `.tflite` / ONNX model
3. Keep the same `DiseasePrediction` response structure

## Optional: Standalone Frontend

Run `agrihub-frontend` with any static server (API calls go to port 8080):

```bash
cd agrihub-frontend
npx serve .
```

---

**AgriHub** — Built for modern agriculture. Final Year B.Tech Engineering Project.

## Dynamic Crop Weather Calendar (Enhanced)

Open:

```text
http://localhost:8080/crop-calendar.html
```

The enhanced calendar keeps the original activity schedule and adds real Open-Meteo weather, historical/forecast GDD, estimated crop stage, weather alerts, ET0-based prototype water status, stage water summaries and weather-aware activity cards.

See `DYNAMIC_CROP_WEATHER_CALENDAR_IMPLEMENTATION.md` for the exact design, calculations, files changed, run commands and validation notes.
