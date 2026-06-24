-- ============================================================
--  SAIL Distributor Performance Management System
--  Database Schema & Seed Data
--  Run this script in MySQL before starting the application.
-- ============================================================

CREATE DATABASE IF NOT EXISTS sail_dpms
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sail_dpms;

-- -----------------------------------------------------------
--  1. Users (both Admins and Distributors)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL COMMENT 'ADMIN or DISTRIBUTOR',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  2. SAIL Manufacturing Units / Plants
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sail_units (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(100) NOT NULL,
    short_code  VARCHAR(10)  NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  3. Distributors (linked to a user account and a SAIL unit)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS distributors (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    unit_id       BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    region        VARCHAR(50),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES sail_units(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  4. Sales Targets (assigned by Admin)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_targets (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    distributor_id  BIGINT        NOT NULL,
    target_volume   DECIMAL(15,2) NOT NULL,
    fiscal_year     VARCHAR(10)   NOT NULL,
    quarter         VARCHAR(5),
    assigned_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  5. Sales Entries (submitted by Distributor)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_entries (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    distributor_id   BIGINT        NOT NULL,
    sales_volume     DECIMAL(15,2) NOT NULL,
    product_category VARCHAR(50),
    month            VARCHAR(20),
    fiscal_year      VARCHAR(10)   NOT NULL,
    dispatch_date    DATE,
    payment_date     DATE,
    remarks          TEXT,
    submitted_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  6. Inventory (stock tracking per distributor per product)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    unit_id          BIGINT,
    distributor_id   BIGINT        NOT NULL,
    product_category VARCHAR(50)   NOT NULL,
    quantity         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    threshold        DECIMAL(15,2) NOT NULL DEFAULT 50.00,
    price_per_mt     DECIMAL(15,2) NOT NULL DEFAULT 45000.00,
    updated_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES sail_units(id) ON DELETE SET NULL,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  7. Orders (purchase orders placed by distributors)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    distributor_id   BIGINT        NOT NULL,
    unit_id          BIGINT        NOT NULL,
    product_category VARCHAR(50)   NOT NULL,
    quantity         DECIMAL(15,2) NOT NULL,
    price_per_mt     DECIMAL(15,2) NOT NULL,
    total_price      DECIMAL(15,2) NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, PAID',
    reject_reason    TEXT,
    placed_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    approved_at      TIMESTAMP     NULL,
    paid_at          TIMESTAMP     NULL,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES sail_units(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  8. Payments (mock payment records)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    distributor_id  BIGINT        NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    payment_method  VARCHAR(30)   NOT NULL COMMENT 'CREDIT_CARD, DEBIT_CARD, NET_BANKING, UPI',
    transaction_ref VARCHAR(50)   NOT NULL UNIQUE,
    card_last_four  VARCHAR(4),
    paid_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  9. Ledger Entries (double-entry accounting for material & payments)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ledger_entries (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    distributor_id   BIGINT        NOT NULL,
    order_id         BIGINT,
    entry_type       VARCHAR(30)   NOT NULL COMMENT 'MATERIAL_SENT, PAYMENT_RECEIVED',
    product_category VARCHAR(50),
    quantity_mt      DECIMAL(15,2),
    debit            DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    credit           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    balance          DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    transaction_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks          TEXT,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------------------
--  Seed Data — SAIL Plants
--  (User accounts with BCrypt passwords are seeded
--   by the Spring Boot DataInitializer on first run.)
-- -----------------------------------------------------------
INSERT INTO sail_units (name, location, short_code, description) VALUES
    ('Bhilai Steel Plant',    'Bhilai, Chhattisgarh',  'BSP', 'India''s first and most productive integrated steel plant, known for producing world-class rails, heavy structurals, and plates.'),
    ('Bokaro Steel Plant',    'Bokaro, Jharkhand',     'BSL', 'One of the largest steel plants in India, specializing in hot & cold rolled coils, sheets, and galvanised products.'),
    ('Rourkela Steel Plant',  'Rourkela, Odisha',      'RSP', 'The first public sector steel plant featuring a dedicated Silicon Steel Mill and a state-of-the-art Plate Mill.'),
    ('Durgapur Steel Plant',  'Durgapur, West Bengal',  'DSP', 'A key producer of structural steel, skelp, and specialised wheels & axles for Indian Railways.'),
    ('IISCO Steel Plant',     'Burnpur, West Bengal',   'ISP', 'One of the oldest steel plants in India, recently modernised with cutting-edge technology and expanded capacity.')
ON DUPLICATE KEY UPDATE name = VALUES(name);
