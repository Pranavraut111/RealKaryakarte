-- =============================================================================
-- Ganpati Mandal — PostgreSQL Setup Script
-- Run this ONCE in pgAdmin's query tool (connected to the default 'postgres' db)
-- =============================================================================

-- Step 1: Create the database
CREATE DATABASE ganpati_mandal;

-- Step 2: Create the application user
-- (Change the password if needed — must match application.properties)
CREATE USER mandal_app WITH PASSWORD 'changeme';
GRANT ALL PRIVILEGES ON DATABASE ganpati_mandal TO mandal_app;

-- Step 3: Connect to the new database and grant schema permissions
\c ganpati_mandal
GRANT ALL ON SCHEMA public TO mandal_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO mandal_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO mandal_app;

-- =============================================================================
-- NOW: Run the V1__init_schema.sql file in pgAdmin while connected to
-- the ganpati_mandal database. That file creates all tables and seed data.
-- =============================================================================
