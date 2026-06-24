# SAIL DPMS — Distributor Performance Management System

A full-stack enterprise application designed for **Steel Authority of India Limited (SAIL)** to track, manage, and optimize distributor performance, inventory, and order-to-ledger workflows across its nationwide steel distribution network.

---

## 🌟 Key Features & Functional Modules

### 1. Live Inventory & Threshold Notifications
- **Central & Distributor Inventory**: Tracks material quantities (in Metric Tonnes) across different steel products (e.g., TMT Bars, HR Coils, Plates, Rails, etc.) for both manufacturing plants and individual distributors.
- **Low-Stock Threshold Alerting**: The Spring Boot service layer implements low-stock checking logic. If a distributor's stock level for a product falls below their configured threshold, a prominent warning alert is dynamically displayed on their dashboard.

### 2. Dynamic Order Placement & Ledger System
- **Real-Time Order Placement**: Distributors can place material purchase orders dynamically. The form fetches available product categories and prices per MT directly from their assigned SAIL manufacturing plant.
- **Double-Entry Ledger Tracking**: Placing an order creates a pending transaction. Once approved, the system updates the distributor's ledger with double-entry accounting records (`MATERIAL_SENT`, `PAYMENT_RECEIVED`) and computes real-time outstanding balances.
- **Admin Order Workflows**: Administrators can review pending orders in real time, with the ability to "Approve" (which dispatches stock and updates the ledger) or "Reject" them.

### 3. Simulated Payment Processing
- **Secure Mock Payment Gateway**: Distributors can pay for approved orders using a credit/debit card, net banking, or UPI simulation. 
- **Receipt Generation**: Submitting a payment transitions the order status in the MySQL database to `PAID`, deducts the distributor's outstanding balance, and generates a formal transaction receipt with a unique reference number.

### 4. Box-Structured Modern UI
- Clean, premium, box-structured dashboard layout using CSS Grid and Flexbox.
- Inline alert animations and notifications (no browser popup interrupts).
- Delayed redirection (2.5 seconds) on login success to simulate enterprise authentication checks.

---

## 🛠️ Tech Stack

- **Backend**: Java 17+ / Spring Boot 3.2.5
- **Database**: MySQL 8.x (Local Connection)
- **Frontend**: HTML5, CSS3 (Vanilla Grid/Flexbox), JavaScript (Vanilla ES6)
- **Security**: Spring Security with BCrypt Password Hashing (plus plain-text fallback options for testing)

---

## 🌐 Secure Routing Endpoints

The application uses custom, secure routes mapped in the Spring Controller layer to forward requests to the static frontend:

| URL Path | Type | Destination Page | Description |
|---|---|---|---|
| `/` | Redirect | `/1a2b3c` | Automatically redirects to the landing page |
| `/1a2b3c` | Forward | `index.html` | Public landing page displaying SAIL stats & carousel |
| `/2b3c4d` | Forward | `signin.html` | Secure portal sign-in page |
| `/d4e5f6` | Forward | `admin.html` | Secured SAIL Admin Dashboard |
| `/7a8b9c` | Forward | `distributor.html` | Secured Distributor Dashboard |

---

## 🔐 Login Credentials

| Username | Password | Role | Distributor Name | Assigned Plant |
|---|---|---|---|---|
| `admin` | `Admin@1234` | SAIL Admin | — | *Global Access* |
| `dist_1` | `Rahul@1234` | Distributor | Rajesh Kumar Sharma | Bhilai Steel Plant (BSP) |
| `dist_2` | `Anita@5678` | Distributor | Anita Devi Singh | Bokaro Steel Plant (BSL) |
| `dist_3` | `Manoj@9012` | Distributor | Manoj Kumar Patel | Rourkela Steel Plant (RSP) |

> **Note**: Each distributor has a unique password. BCrypt-hashed passwords are stored in the database; plain-text fallbacks are available for development convenience.

---

## 📊 Seeded Production Data

The `DataInitializer` automatically populates the database on first startup with realistic SAIL production data:

| Data Type | Count | Details |
|---|---|---|
| SAIL Plants | 5 | BSP, BSL, RSP, DSP, ISP with capacity and product descriptions |
| Central Inventory | 23 items | Products with BIS/IS grade specifications (Fe-500D, IS:2062, API 5L X-65, CRGO) |
| Distributor Inventory | 7 items | Including 2 below-threshold alerts for low-stock notifications |
| Sales Targets | 9 | FY 2025-26 quarterly targets across all 3 distributors |
| Sales Entries | 12+ | Monthly records referencing real projects (PMAY, Raipur Metro, GAIL Pipeline) |
| Orders | 9 | Full lifecycle: PENDING → APPROVED → PAID, plus REJECTED |
| Payments | 4 | With SAIL transaction references (SAIL-TXN-YYYYMMDD-XX###) |
| Ledger Entries | 10 | Double-entry records with running balances, dispatch details, and rail rake numbers |

---

## 🚀 Local Setup & Run Instructions

### 1. Configure the Database
1. Make sure MySQL is running on `localhost:3306`.
2. Open `src/main/resources/application.properties` and update the database credentials to match your local setup:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/sail_dpms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

### 2. Run the Application
From the root project directory, execute the following Maven command:
```bash
mvn spring-boot:run
```
- The application is configured with `spring.jpa.hibernate.ddl-auto=create`, which will automatically drop/recreate the tables and relationships on startup.
- The `DataInitializer` bean will automatically seed the database with all plants, users, inventories, orders, payments, ledger entries, and sales history.

### 3. Open in Browser
Once the terminal logs show `Started SailDpmsApplication`, open your browser and navigate to:
```
http://localhost:8080
```
*(This will automatically redirect to the secure landing page `/1a2b3c`)*

---

## 📁 Project Structure

```
sail-dpms/
├── src/main/java/com/sail/dpms/
│   ├── SailDpmsApplication.java          # Spring Boot entry point
│   ├── config/
│   │   ├── DataInitializer.java          # Database seeder with realistic data
│   │   └── SecurityConfig.java           # Spring Security + CORS config
│   ├── controller/
│   │   ├── AuthController.java           # Login API (/api/auth/login)
│   │   ├── RouteController.java          # Secure URL forwarding
│   │   ├── AdminController.java          # Admin dashboard APIs
│   │   ├── DistributorController.java    # Distributor APIs
│   │   ├── InventoryController.java      # Inventory & threshold APIs
│   │   ├── OrderController.java          # Order placement & approval APIs
│   │   ├── PaymentController.java        # Payment processing APIs
│   │   └── LedgerController.java         # Ledger & balance APIs
│   ├── entity/                           # JPA Entities (9 tables)
│   └── repository/                       # Spring Data JPA Repositories
├── src/main/resources/
│   ├── application.properties            # DB config & Hibernate settings
│   └── static/
│       ├── index.html                    # Landing page with carousel
│       ├── signin.html                   # Login page
│       ├── admin.html                    # Admin dashboard
│       ├── distributor.html              # Distributor dashboard
│       ├── css/style.css                 # Global styles
│       └── js/app.js                     # Frontend logic & API calls
└── pom.xml                               # Maven dependencies
```

