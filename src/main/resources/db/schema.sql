-- ===========================================================
-- EcoCycle Manager - Schema SQLite
-- Sistema de Gestao de Logistica Reversa (ODS 12)
-- ===========================================================

CREATE TABLE IF NOT EXISTS assets (
    id              TEXT PRIMARY KEY,
    category        TEXT NOT NULL,
    model           TEXT,
    acquired_at     DATE NOT NULL,
    lifespan_years  INTEGER NOT NULL,
    status          TEXT DEFAULT 'active'   -- active | expiring | disposed
);

CREATE TABLE IF NOT EXISTS destinations (
    id   TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT                              -- cooperativa | fabricante | reciclador
);

CREATE TABLE IF NOT EXISTS disposals (
    id              TEXT PRIMARY KEY,
    asset_id        TEXT REFERENCES assets(id),
    disposed_at     DATE NOT NULL,
    disk_sanitized  INTEGER NOT NULL,      -- 0 ou 1
    battery_removed INTEGER NOT NULL,
    destination_id  TEXT REFERENCES destinations(id)
);

CREATE INDEX IF NOT EXISTS idx_assets_status ON assets(status);
CREATE INDEX IF NOT EXISTS idx_assets_category ON assets(category);
