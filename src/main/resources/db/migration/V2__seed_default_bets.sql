INSERT INTO bets (id, user_id, event_id, event_market_id, selected_winner_id, amount, status, created_at, settled_at, version)
VALUES
    ('1a2b3c4d-5e6f-4701-8a01-111111111111', 'user-1', 'match-42', 'winner-market', 'team-a', 10.0000, 'OPEN', '2026-09-28T08:00:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-222222222222', 'user-2', 'match-42', 'winner-market', 'team-b', 5.0000, 'OPEN', '2026-09-28T08:05:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-333333333333', 'user-3', 'match-42', 'winner-market', 'team-a', 7.5000, 'OPEN', '2026-09-28T08:10:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-444444444444', 'user-4', 'match-43', 'winner-market', 'team-c', 12.0000, 'OPEN', '2026-09-28T08:15:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-555555555555', 'user-5', 'match-43', 'winner-market', 'team-d', 3.2500, 'OPEN', '2026-09-28T08:20:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-666666666666', 'user-6', 'match-44', 'winner-market', 'team-e', 25.0000, 'OPEN', '2026-09-28T08:25:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-777777777777', 'user-7', 'match-44', 'winner-market', 'team-f', 8.0000, 'OPEN', '2026-09-28T08:30:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-888888888888', 'user-8', 'match-45', 'winner-market', 'team-g', 15.0000, 'OPEN', '2026-09-28T08:35:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-999999999999', 'user-9', 'match-45', 'winner-market', 'team-h', 2.5000, 'OPEN', '2026-09-28T08:40:00Z', NULL, 0),
    ('1a2b3c4d-5e6f-4701-8a01-aaaaaaaaaaaa', 'user-10', 'match-46', 'winner-market', 'team-i', 20.0000, 'OPEN', '2026-09-28T08:45:00Z', NULL, 0);
