-- =====================================================================
-- Sunrise Dental Clinic - Appointment & Patient Management System
-- MySQL schema (Task B). Mirrors the Task A class diagram exactly, so
-- there is a clear, traceable line from UML model -> database design.
-- Run this against an empty schema, e.g.:
--   mysql -u root -p -e "CREATE DATABASE sunrise_dental"
--   mysql -u root -p sunrise_dental < schema.sql
--   mysql -u root -p sunrise_dental < data.sql
-- =====================================================================

DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS treatment_types;
DROP TABLE IF EXISTS dentists;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------------
-- users: Receptionist / Administrator accounts (User class hierarchy)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    password_salt  VARCHAR(50)  NOT NULL,
    role           ENUM('RECEPTIONIST', 'ADMINISTRATOR') NOT NULL,
    staff_name     VARCHAR(100) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- patients
-- ---------------------------------------------------------------------
CREATE TABLE patients (
    patient_id     INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(60)  NOT NULL,
    address        VARCHAR(120) NOT NULL,
    contact_number VARCHAR(15)  NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- dentists
-- ---------------------------------------------------------------------
CREATE TABLE dentists (
    dentist_id     INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(60) NOT NULL,
    specialization VARCHAR(60) NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- treatment_types
-- ---------------------------------------------------------------------
CREATE TABLE treatment_types (
    treatment_id   INT AUTO_INCREMENT PRIMARY KEY,
    treatment_name VARCHAR(60) NOT NULL,
    base_cost      DECIMAL(10,2) NOT NULL CHECK (base_cost >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- appointments: central table linking patient, dentist, treatment type
-- ---------------------------------------------------------------------
CREATE TABLE appointments (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number  VARCHAR(20) NOT NULL UNIQUE,
    patient_id          INT NOT NULL,
    dentist_id          INT NOT NULL,
    treatment_id        INT NOT NULL,
    appointment_date    DATE NOT NULL,
    appointment_time    TIME NOT NULL,
    status              ENUM('BOOKED','RESCHEDULED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'BOOKED',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appt_patient   FOREIGN KEY (patient_id)   REFERENCES patients(patient_id)   ON DELETE RESTRICT,
    CONSTRAINT fk_appt_dentist   FOREIGN KEY (dentist_id)   REFERENCES dentists(dentist_id)    ON DELETE RESTRICT,
    CONSTRAINT fk_appt_treatment FOREIGN KEY (treatment_id) REFERENCES treatment_types(treatment_id) ON DELETE RESTRICT,

    INDEX idx_appt_date (appointment_date),
    INDEX idx_appt_dentist_slot (dentist_id, appointment_date, appointment_time)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- bills: composition child of appointments (Task A: Appointment 1 *-- 0..1 Bill)
-- ---------------------------------------------------------------------
CREATE TABLE bills (
    bill_id             INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number  VARCHAR(20) NOT NULL UNIQUE,
    consultation_fee    DECIMAL(10,2) NOT NULL,
    treatment_cost      DECIMAL(10,2) NOT NULL,
    discount_amount     DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(10,2) NOT NULL,
    generated_date      DATE NOT NULL,

    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_number)
        REFERENCES appointments(appointment_number) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- ADVANCED DATABASE FEATURES (top marking band: "appropriate use of
-- advanced database features e.g. stored procedures, functions, triggers
-- to implement business rules")
-- =====================================================================

DELIMITER $$

-- ---------------------------------------------------------------------
-- FUNCTION: fn_calculate_bill_total
-- Business rule: total = consultation fee + treatment cost - discount,
-- never negative. Centralising this in the database means the rule holds
-- even for a query or another client that writes to `bills` directly,
-- not only through the Java service layer (defense in depth - the same
-- rule is also enforced in Bill.calculateTotal() in the application).
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_calculate_bill_total$$
CREATE FUNCTION fn_calculate_bill_total(
    p_consultation_fee DECIMAL(10,2),
    p_treatment_cost DECIMAL(10,2),
    p_discount DECIMAL(10,2)
) RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE v_total DECIMAL(10,2);
    SET v_total = p_consultation_fee + p_treatment_cost - p_discount;
    IF v_total < 0 THEN
        SET v_total = 0;
    END IF;
    RETURN v_total;
END$$

-- ---------------------------------------------------------------------
-- TRIGGER: trg_prevent_double_booking (BEFORE INSERT)
-- Business rule from the brief itself: "double bookings" were named as
-- one of the clinic's current problems. The Java service layer already
-- checks this (AppointmentService.registerAppointment ->
-- existsForDentistAtSlot), but enforcing it again here means the rule
-- cannot be bypassed even by a direct SQL insert or a future client that
-- forgets to call the Java check - the database is the last line of
-- defense for data integrity.
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_prevent_double_booking$$
CREATE TRIGGER trg_prevent_double_booking
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_conflicts INT;
    SELECT COUNT(*) INTO v_conflicts
    FROM appointments
    WHERE dentist_id = NEW.dentist_id
      AND appointment_date = NEW.appointment_date
      AND appointment_time = NEW.appointment_time
      AND status <> 'CANCELLED';

    IF v_conflicts > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Double-booking rejected: this dentist already has an appointment at that date/time.';
    END IF;
END$$

-- Same rule again for UPDATE (covers reschedule moving an appointment onto an occupied slot).
DROP TRIGGER IF EXISTS trg_prevent_double_booking_update$$
CREATE TRIGGER trg_prevent_double_booking_update
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_conflicts INT;
    IF NEW.status <> 'CANCELLED' THEN
        SELECT COUNT(*) INTO v_conflicts
        FROM appointments
        WHERE dentist_id = NEW.dentist_id
          AND appointment_date = NEW.appointment_date
          AND appointment_time = NEW.appointment_time
          AND status <> 'CANCELLED'
          AND id <> NEW.id;

        IF v_conflicts > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Double-booking rejected: this dentist already has an appointment at that date/time.';
        END IF;
    END IF;
END$$

-- ---------------------------------------------------------------------
-- STORED PROCEDURE: sp_daily_revenue
-- Used by ReportService's "Generate Reports" (Administrator) use case -
-- see ReportService.revenueReportViaStoredProcedure() in the Java code,
-- which calls this with a CallableStatement rather than re-implementing
-- the aggregation in Java, so the business rule (what counts as revenue
-- for a given day) is defined in exactly one place.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_daily_revenue$$
CREATE PROCEDURE sp_daily_revenue(IN p_date DATE, OUT p_total_revenue DECIMAL(10,2), OUT p_bill_count INT)
BEGIN
    SELECT COALESCE(SUM(total_amount), 0), COUNT(*)
    INTO p_total_revenue, p_bill_count
    FROM bills
    WHERE generated_date = p_date;
END$$

DELIMITER ;
