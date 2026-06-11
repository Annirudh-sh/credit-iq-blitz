INSERT IGNORE INTO h_user (id, phone, name, tb_coins, created_at)
VALUES
    ('seed-user-priya',  '9000000001', 'Priya',  250, NOW()),
    ('seed-user-arjun',  '9000000002', 'Arjun',  250, NOW()),
    ('seed-user-neha',   '9000000003', 'Neha',   200, NOW()),
    ('seed-user-sam',    '9000000004', 'Sam',     200, NOW()),
    ('seed-user-rohit',  '9000000005', 'Rohit',  360, NOW()),
    ('seed-user-virat',  '9000000006', 'Virat',  340, NOW()),
    ('seed-user-dhoni',  '9000000007', 'Dhoni',  280, NOW()),
    ('seed-user-hardik', '9000000008', 'Hardik', 240, NOW());

INSERT IGNORE INTO h_game_attempt (id, user_id, game_type, started_at, submitted_at, score, time_taken_sec, coins, display_name, verified)
VALUES
    ('seed-att-priya', 'seed-user-priya', 'CREDIT_IQ', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR + INTERVAL 38 SECOND, 5, 38.2,  250, 'Priya', TRUE),
    ('seed-att-arjun', 'seed-user-arjun', 'CREDIT_IQ', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR + INTERVAL 45 SECOND, 5, 45.1,  250, 'Arjun', TRUE),
    ('seed-att-neha',  'seed-user-neha',  'CREDIT_IQ', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR + INTERVAL 52 SECOND, 4, 52.7,  200, 'Neha',  TRUE),
    ('seed-att-sam',   'seed-user-sam',   'CREDIT_IQ', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR + INTERVAL 60 SECOND, 4, 60.3,  200, 'Sam',   TRUE);

-- Cricket seed attempts
INSERT IGNORE INTO h_game_attempt (id, user_id, game_type, started_at, submitted_at, score, time_taken_sec, coins, display_name, verified)
VALUES
    ('seed-cri-rohit',  'seed-user-rohit',  'CRICKET', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR + INTERVAL 25 SECOND, 36, 25.4, 360, 'Rohit',  TRUE),
    ('seed-cri-virat',  'seed-user-virat',  'CRICKET', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR + INTERVAL 28 SECOND, 34, 28.1, 340, 'Virat',  TRUE),
    ('seed-cri-dhoni',  'seed-user-dhoni',  'CRICKET', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR + INTERVAL 30 SECOND, 28, 30.5, 280, 'Dhoni',  TRUE),
    ('seed-cri-hardik', 'seed-user-hardik', 'CRICKET', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR + INTERVAL 32 SECOND, 24, 32.0, 240, 'Hardik', TRUE);

INSERT IGNORE INTO h_lead_record (id, user_id, game_attempt_id, intent_category, cibil_consent, comms_consent, created_at)
VALUES
    ('seed-lead-priya',  'seed-user-priya',  'seed-att-priya',  'HOT',  TRUE, TRUE, NOW()),
    ('seed-lead-arjun',  'seed-user-arjun',  'seed-att-arjun',  'HOT',  TRUE, TRUE, NOW()),
    ('seed-lead-neha',   'seed-user-neha',   'seed-att-neha',   'WARM', TRUE, TRUE, NOW()),
    ('seed-lead-sam',    'seed-user-sam',     'seed-att-sam',    'WARM', TRUE, TRUE, NOW()),
    ('seed-lead-rohit',  'seed-user-rohit',  'seed-cri-rohit',  'HOT',  TRUE, TRUE, NOW()),
    ('seed-lead-virat',  'seed-user-virat',  'seed-cri-virat',  'HOT',  TRUE, TRUE, NOW()),
    ('seed-lead-dhoni',  'seed-user-dhoni',  'seed-cri-dhoni',  'WARM', TRUE, TRUE, NOW()),
    ('seed-lead-hardik', 'seed-user-hardik', 'seed-cri-hardik', 'WARM', TRUE, TRUE, NOW());
