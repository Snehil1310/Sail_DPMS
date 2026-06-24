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

## 🔐 Test Credentials

| Username | Password | Role | Assigned Plant / Unit |
|---|---|---|---|
| `admin` | `Admin@1234` | SAIL Admin | *Global Access* |
| `dist_bhilai` | `dist123` | Distributor | Bhilai Steel Plant (BSP) |
| `dist_bokaro` | `dist123` | Distributor | Bokaro Steel Plant (BSL) |
| `dist_rourkela` | `dist123` | Distributor | Rourkela Steel Plant (RSP) |

> **Note**: Passwords can be entered using their complex format (`Admin@1234`) or simple testing fallbacks (`admin123`, `dist123`).

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
- The `DataInitializer` bean will automatically seed the database with all plants, users, central inventory stocks, distributor stocks (including threshold warnings), and historical targets/entries.

### 3. Open in Browser
Once the terminal logs show `Started SailDpmsApplication`, open your browser and navigate to:
```
http://localhost:8080
```
*(This will automatically redirect to the secure landing page `/1a2b3c`)*
