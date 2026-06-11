CREATE TABLE IF NOT EXISTS h_user (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    phone           VARCHAR(15)  NOT NULL UNIQUE,
    name            VARCHAR(50),
    tb_coins        INT          NOT NULL DEFAULT 0,
    cibil_score     INT,
    cibil_band      VARCHAR(20),
    db_match_flag   VARCHAR(20),
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS h_game_attempt (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id         VARCHAR(36),
    game_type       VARCHAR(20)  NOT NULL DEFAULT 'CREDIT_IQ',
    started_at      DATETIME(3)  NOT NULL,
    submitted_at    DATETIME(3),
    score           INT,
    time_taken_sec  DOUBLE,
    coins           INT,
    display_name    VARCHAR(50),
    verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    INDEX idx_attempt_coins (coins),
    INDEX idx_attempt_user (user_id),
    INDEX idx_attempt_game (game_type),
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES h_user(id)
);

CREATE TABLE IF NOT EXISTS h_lead_record (
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id           VARCHAR(36)  NOT NULL,
    game_attempt_id   VARCHAR(36)  NOT NULL,
    intent_category   VARCHAR(10),
    cibil_consent     BOOLEAN      NOT NULL DEFAULT FALSE,
    comms_consent     BOOLEAN      NOT NULL DEFAULT FALSE,
    consent_at        DATETIME(3),
    consent_text      TEXT,
    created_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_lead_user (user_id),
    CONSTRAINT fk_lead_user    FOREIGN KEY (user_id)          REFERENCES h_user(id),
    CONSTRAINT fk_lead_attempt FOREIGN KEY (game_attempt_id)  REFERENCES h_game_attempt(id)
);

CREATE TABLE IF NOT EXISTS h_device_info (
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    game_attempt_id   VARCHAR(36)  NOT NULL,
    device_id         VARCHAR(64),
    device_model      VARCHAR(100),
    user_lat          DOUBLE,
    user_lng          DOUBLE,
    city              VARCHAR(100),
    CONSTRAINT fk_device_attempt FOREIGN KEY (game_attempt_id) REFERENCES h_game_attempt(id)
);

CREATE TABLE IF NOT EXISTS h_city_device_score (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    city            VARCHAR(100) NOT NULL,
    device_model    VARCHAR(100) NOT NULL,
    score           INT          NOT NULL DEFAULT 5,
    UNIQUE INDEX idx_city_device (city, device_model)
);

CREATE TABLE IF NOT EXISTS h_otp_code (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    phone       VARCHAR(15)  NOT NULL,
    code        VARCHAR(10)  NOT NULL,
    expires_at  DATETIME(3)  NOT NULL,
    consumed    BOOLEAN      NOT NULL DEFAULT FALSE,
    INDEX idx_otp_phone (phone)
);
