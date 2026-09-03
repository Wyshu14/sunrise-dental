-- =====================================================================
-- Seed data for Sunrise Dental Clinic.
-- Run after schema.sql: mysql -u root -p sunrise_dental < data.sql
--
-- Default login accounts (documented here AND in README.md):
--   Administrator : username = admin        password = Admin@123
--   Receptionist  : username = reception1   password = Reception@123
-- These password hashes were generated with the project's own
-- PasswordUtil (SHA-256 + per-user salt) so they work out of the box -
-- change them after first login in a real deployment.
-- =====================================================================

INSERT INTO users (username, password_hash, password_salt, role, staff_name) VALUES
('admin',      '8RvAeYp22oV1oaQiXsyoJOkOyg/Lav9GDVOyA0RZ7Iw=', 'rzUQZvdNIk/CWTZeA1niHg==', 'ADMINISTRATOR', 'Clinic Administrator'),
('reception1', 'eJ9olMHI1VRtuov/PsBFRacFFMGa9ETlu9fZLxrmvVU=', 'aYiOSMPnI8T/hIZoMHNJjA==', 'RECEPTIONIST',  'Nadeesha Perera');

INSERT INTO dentists (name, specialization) VALUES
('Dr. S. Fernando', 'General Dentistry'),
('Dr. R. Jayawardena', 'Orthodontics'),
('Dr. M. Silva', 'Oral Surgery');

INSERT INTO treatment_types (treatment_name, base_cost) VALUES
('Dental Check-up', 1000.00),
('Scaling & Polishing', 2500.00),
('Tooth Extraction', 4000.00),
('Root Canal Treatment', 15000.00),
('Dental Filling', 3500.00),
('Braces Consultation', 2000.00);
