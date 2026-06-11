INSERT IGNORE INTO h_user (id, phone, name, tb_coins, created_at)
VALUES
    ('seed-user-priya', '9000000001', 'Priya', 250, NOW()),
    ('seed-user-arjun', '9000000002', 'Arjun', 250, NOW()),
    ('seed-user-neha',  '9000000003', 'Neha',  200, NOW()),
    ('seed-user-sam',   '9000000004', 'Sam',   200, NOW());

INSERT IGNORE INTO h_quiz_attempt (id, user_id, started_at, submitted_at, correct_count, time_taken_sec, coins, display_name, verified)
VALUES
    ('seed-att-priya', 'seed-user-priya', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR + INTERVAL 38 SECOND, 5, 38.2,  250, 'Priya', TRUE),
    ('seed-att-arjun', 'seed-user-arjun', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR + INTERVAL 45 SECOND, 5, 45.1,  250, 'Arjun', TRUE),
    ('seed-att-neha',  'seed-user-neha',  NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR + INTERVAL 52 SECOND, 4, 52.7,  200, 'Neha',  TRUE),
    ('seed-att-sam',   'seed-user-sam',   NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR + INTERVAL 60 SECOND, 4, 60.3,  200, 'Sam',   TRUE);

INSERT IGNORE INTO h_lead_record (id, user_id, quiz_attempt_id, intent_category, cibil_consent, comms_consent, created_at)
VALUES
    ('seed-lead-priya', 'seed-user-priya', 'seed-att-priya', 'HOT',  TRUE, TRUE, NOW()),
    ('seed-lead-arjun', 'seed-user-arjun', 'seed-att-arjun', 'HOT',  TRUE, TRUE, NOW()),
    ('seed-lead-neha',  'seed-user-neha',  'seed-att-neha',  'WARM', TRUE, TRUE, NOW()),
    ('seed-lead-sam',   'seed-user-sam',   'seed-att-sam',   'WARM', TRUE, TRUE, NOW());
