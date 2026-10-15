-- Local profile only. All sample flight dates are yesterday in UTC.
-- Re-running adds missing rows and preserves existing flight/policy changes.
-- These snapshots bypass domain events: no FLM/CPM history is fabricated.
SET TIME ZONE 'UTC';

INSERT INTO oc_policy (id, version, offloading_constraint, loading_window_opening_minutes,
                       loading_window_closing_minutes, maximum_lateral_weight_difference_percent,
                       flight_retention_days, actual_time_tolerance_minutes)
SELECT 1, 0, 'HARD', 720, 20, 10, 7, 5
WHERE NOT EXISTS (SELECT 1 FROM oc_policy WHERE id = 1);

-- Scheduled flight without a final load: delay editing and departure refusal.
INSERT INTO fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9001/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU', 0, 'HJ9001',
       CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 'BRU', 'ATH', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '10' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '12' HOUR, NULL, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM fo_flight WHERE id =
    'HJ9001/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU');

-- Ready flight: the final-load flag is a fixture, not a real CPM delivery.
INSERT INTO fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9002/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU', 0, 'HJ9002',
       CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 'BRU', 'CDG', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '13' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '14' HOUR, NULL, NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM fo_flight WHERE id =
    'HJ9002/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU');

-- Landed flight: read-only lifecycle state for conflict checks.
INSERT INTO fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9003/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU', 0, 'HJ9003',
       CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 'BRU', 'AMS', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '6' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '8' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '6' HOUR + INTERVAL '2' MINUTE,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '8' HOUR + INTERVAL '3' MINUTE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM fo_flight WHERE id =
    'HJ9003/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU');

-- Load Planning aircraft catalogue: the airline operates one type, the Boeing 747-400 freighter (main deck).
-- Rows nose to tail, nose and tail single positions, cargo door on the left of row R.
INSERT INTO lp_aircraft_type (type_code, row_slots, nose_slots, tail_slots, door_row)
SELECT 'B744F', 'C,D,E,F,G,H,J,K,L,M,P,R,S,T,U', 'A1,A2,B1', 'X', 'R'
    WHERE NOT EXISTS (SELECT 1 FROM lp_aircraft_type WHERE type_code = 'B744F');

INSERT INTO lp_aircraft (registration, type_code)
SELECT 'SXBAA', 'B744F'
    WHERE NOT EXISTS (SELECT 1 FROM lp_aircraft WHERE registration = 'SXBAA');
