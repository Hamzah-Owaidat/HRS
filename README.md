-- =============================================
-- Hotel Reservation System - SQL Server Schema
-- Converted from MySQL/phpMyAdmin dump
-- =============================================

USE hotel;
GO

-- --------------------------------------------------------
-- Table: roles
-- --------------------------------------------------------
CREATE TABLE roles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);
GO
Hello:"(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)"\\
-- --------------------------------------------------------
-- Table: guests
-- --------------------------------------------------------
CREATE TABLE guests (
    id INT IDENTITY(1,1) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone INT NOT NULL,
    nationality VARCHAR(100) NOT NULL
);
GO

-- --------------------------------------------------------
-- Table: room_types
-- --------------------------------------------------------
CREATE TABLE room_types (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    price_per_night INT NOT NULL
);
GO

-- --------------------------------------------------------
-- Table: rooms
-- --------------------------------------------------------
CREATE TABLE rooms (
    id INT IDENTITY(1,1) PRIMARY KEY,
    room_number INT NOT NULL,
    room_type_id INT NOT NULL,
    floor INT NOT NULL,
    status VARCHAR(100) NOT NULL,
    price_per_night DECIMAL(10,0) NOT NULL,
    CONSTRAINT fk_rooms_room_type FOREIGN KEY (room_type_id)
        REFERENCES room_types(id) ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- --------------------------------------------------------
-- Table: users
-- --------------------------------------------------------
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- --------------------------------------------------------
-- Table: reservations
-- --------------------------------------------------------
CREATE TABLE reservations (
    id INT IDENTITY(1,1) PRIMARY KEY,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guests_count INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,0) NOT NULL,
    CONSTRAINT fk_reservations_guest FOREIGN KEY (guest_id)
        REFERENCES guests(id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_reservations_room FOREIGN KEY (room_id)
        REFERENCES rooms(id) ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- --------------------------------------------------------
-- Table: payments
-- --------------------------------------------------------
CREATE TABLE payments (
    id INT IDENTITY(1,1) PRIMARY KEY,
    reservation_id INT NOT NULL,
    amount DECIMAL(10,0) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservations(id) ON DELETE CASCADE ON UPDATE CASCADE
);
GO

-- --------------------------------------------------------
-- Seed roles
-- --------------------------------------------------------
INSERT INTO roles (name) VALUES ('admin'), ('employee');
GO




ALTER TABLE guests ALTER COLUMN phone VARCHAR(20) NOT NULL;
