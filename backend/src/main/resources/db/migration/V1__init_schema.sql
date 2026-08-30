-- =============================================================================
-- Ganpati Mandal Management System — Initial Schema
-- PostgreSQL 15+
-- =============================================================================

-- ─── Users ───────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    phone       VARCHAR(15)  UNIQUE NOT NULL,
    email       VARCHAR(150),
    role        VARCHAR(20)  NOT NULL DEFAULT 'MEMBER'
                CHECK (role IN ('ADMIN', 'KARYAKARTA', 'MEMBER')),
    language_pref VARCHAR(5) DEFAULT 'en',
    photo_url   TEXT,
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT now(),
    updated_at  TIMESTAMP    DEFAULT now()
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role  ON users(role);

-- ─── Receipt Number Sequence ─────────────────────────────────────────────────
-- Format: GM-2026-00001, GM-2026-00002, etc.
CREATE SEQUENCE receipt_no_seq START WITH 1 INCREMENT BY 1;

-- ─── Contributions (Vargani) ─────────────────────────────────────────────────
CREATE TABLE contributions (
    id                BIGSERIAL PRIMARY KEY,
    member_id         BIGINT       REFERENCES users(id),
    member_name       VARCHAR(150),
    amount            NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    payment_method    VARCHAR(20)
                      CHECK (payment_method IN ('CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE')),
    collected_by      BIGINT       REFERENCES users(id),
    receipt_no        VARCHAR(30)  UNIQUE NOT NULL,
    receipt_pdf_url   TEXT,
    note              TEXT,
    contribution_date DATE         NOT NULL,
    created_at        TIMESTAMP    DEFAULT now(),
    updated_at        TIMESTAMP    DEFAULT now(),
    created_by        BIGINT       REFERENCES users(id)
);

CREATE INDEX idx_contributions_member   ON contributions(member_id);
CREATE INDEX idx_contributions_date     ON contributions(contribution_date);
CREATE INDEX idx_contributions_method   ON contributions(payment_method);
CREATE INDEX idx_contributions_collector ON contributions(collected_by);

-- ─── Expense Categories ──────────────────────────────────────────────────────
CREATE TABLE expense_categories (
    id      BIGSERIAL PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL,
    name_mr VARCHAR(100)
);

-- ─── Expenses ────────────────────────────────────────────────────────────────
CREATE TABLE expenses (
    id                BIGSERIAL PRIMARY KEY,
    item_name         VARCHAR(200) NOT NULL,
    category_id       BIGINT       REFERENCES expense_categories(id),
    amount            NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    purchased_by      BIGINT       REFERENCES users(id),
    vendor_name       VARCHAR(150),
    item_photo_url    TEXT,
    receipt_photo_url TEXT,
    approval_status   VARCHAR(20)  DEFAULT 'APPROVED'
                      CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by       BIGINT       REFERENCES users(id),
    expense_date      DATE         NOT NULL,
    created_at        TIMESTAMP    DEFAULT now(),
    updated_at        TIMESTAMP    DEFAULT now(),
    created_by        BIGINT       REFERENCES users(id)
);

CREATE INDEX idx_expenses_category  ON expenses(category_id);
CREATE INDEX idx_expenses_purchaser ON expenses(purchased_by);
CREATE INDEX idx_expenses_date      ON expenses(expense_date);
CREATE INDEX idx_expenses_status    ON expenses(approval_status);

-- ─── Notices ─────────────────────────────────────────────────────────────────
CREATE TABLE notices (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    body       TEXT         NOT NULL,
    photo_url  TEXT,
    posted_by  BIGINT       REFERENCES users(id),
    is_pinned  BOOLEAN      DEFAULT FALSE,
    publish_at TIMESTAMP    DEFAULT now(),
    created_at TIMESTAMP    DEFAULT now()
);

CREATE INDEX idx_notices_pinned    ON notices(is_pinned);
CREATE INDEX idx_notices_published ON notices(publish_at);

-- ─── Broadcasts ──────────────────────────────────────────────────────────────
CREATE TABLE broadcasts (
    id              BIGSERIAL PRIMARY KEY,
    message_text    TEXT NOT NULL,
    channel         VARCHAR(10)
                    CHECK (channel IN ('SMS', 'WHATSAPP')),
    sent_by         BIGINT       REFERENCES users(id),
    recipient_group VARCHAR(20),
    sent_at         TIMESTAMP    DEFAULT now(),
    status          VARCHAR(20)  DEFAULT 'SENT'
);

-- ─── Mandal Settings ─────────────────────────────────────────────────────────
CREATE TABLE mandal_settings (
    id                        BIGSERIAL PRIMARY KEY,
    mandal_name               VARCHAR(200),
    event_year                INT,
    logo_url                  TEXT,
    suggested_vargani_amount  NUMERIC(10,2),
    expense_approval_required BOOLEAN DEFAULT FALSE,
    karyakarta_can_broadcast  BOOLEAN DEFAULT TRUE,
    sms_api_key               TEXT,
    whatsapp_api_key          TEXT
);

-- ─── OTP Storage (temporary, for phone verification) ─────────────────────────
CREATE TABLE otp_store (
    id         BIGSERIAL PRIMARY KEY,
    phone      VARCHAR(15) NOT NULL,
    otp_code   VARCHAR(6)  NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    verified   BOOLEAN     DEFAULT FALSE,
    created_at TIMESTAMP   DEFAULT now()
);

CREATE INDEX idx_otp_phone ON otp_store(phone);

-- =============================================================================
-- Seed Data
-- =============================================================================

-- Default expense categories
INSERT INTO expense_categories (name_en, name_mr) VALUES
    ('Decoration',      'सजावट'),
    ('Flowers',         'फुले'),
    ('Prasad',          'प्रसाद'),
    ('Lighting',        'प्रकाश व्यवस्था'),
    ('Sound System',    'ध्वनी यंत्रणा'),
    ('Pandal Setup',    'पंडाल उभारणी'),
    ('Idol / Murti',    'मूर्ती'),
    ('Puja Materials',  'पूजा साहित्य'),
    ('Food / Bhandara', 'भोजन / भंडारा'),
    ('Transport',       'वाहतूक'),
    ('Visarjan',        'विसर्जन'),
    ('Miscellaneous',   'इतर');

-- Default mandal settings (single row)
INSERT INTO mandal_settings (mandal_name, event_year, suggested_vargani_amount)
VALUES ('श्री गणेश मंडळ', 2026, 500.00);
