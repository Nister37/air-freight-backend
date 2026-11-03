-- Local profile only. Sample flights cover today and yesterday in UTC.
-- Re-running adds missing rows and preserves existing flight/policy changes.
-- These snapshots bypass domain events: no FLM/CPM history is fabricated.
SET TIME ZONE 'UTC';

INSERT INTO flightoperations.fo_policy (id, version, flight_retention_days, actual_time_tolerance_minutes)
SELECT 1, 0, 7, 5
WHERE NOT EXISTS (SELECT 1 FROM flightoperations.fo_policy WHERE id = 1);

INSERT INTO loadplanning.lp_policy (id, version, offloading_constraint, loading_window_opening_minutes,
                       loading_window_closing_minutes, maximum_lateral_weight_difference_percent)
SELECT 1, 0, 'HARD', 720, 20, 10
WHERE NOT EXISTS (SELECT 1 FROM loadplanning.lp_policy WHERE id = 1);

-- Today's flights keep the default screen useful. Inserted in reverse departure order so query ordering is visible.
INSERT INTO flightoperations.fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9011/' || CAST(CURRENT_DATE AS DATE) || '/BRU', 0, 'HJ9011',
       CAST(CURRENT_DATE AS DATE), 'BRU', 'FRA', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '15' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '16' HOUR + INTERVAL '10' MINUTE, NULL, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM flightoperations.fo_flight WHERE id =
    'HJ9011/' || CAST(CURRENT_DATE AS DATE) || '/BRU');

INSERT INTO flightoperations.fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9010/' || CAST(CURRENT_DATE AS DATE) || '/BRU', 0, 'HJ9010',
       CAST(CURRENT_DATE AS DATE), 'BRU', 'LHR', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '9' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '10' HOUR + INTERVAL '20' MINUTE, NULL, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM flightoperations.fo_flight WHERE id =
    'HJ9010/' || CAST(CURRENT_DATE AS DATE) || '/BRU');

-- Scheduled flight without a final load: delay editing and departure refusal.
INSERT INTO flightoperations.fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9001/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU', 0, 'HJ9001',
       CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 'BRU', 'ATH', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '10' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '12' HOUR, NULL, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM flightoperations.fo_flight WHERE id =
    'HJ9001/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU');

-- Ready flight: the final-load flag is a fixture, not a real CPM delivery.
INSERT INTO flightoperations.fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9002/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU', 0, 'HJ9002',
       CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 'BRU', 'CDG', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '13' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '14' HOUR, NULL, NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM flightoperations.fo_flight WHERE id =
    'HJ9002/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU');

-- Landed flight: read-only lifecycle state for conflict checks.
INSERT INTO flightoperations.fo_flight (id, version, flight_number, scheduled_departure_date, departure_airport,
                       arrival_airport, aircraft_registration, scheduled_departure, scheduled_arrival,
                       actual_departure, actual_arrival, final_load_received)
SELECT 'HJ9003/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU', 0, 'HJ9003',
       CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 'BRU', 'AMS', 'SXBAA',
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '6' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '8' HOUR,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '6' HOUR + INTERVAL '2' MINUTE,
       CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '1' DAY + INTERVAL '8' HOUR + INTERVAL '3' MINUTE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM flightoperations.fo_flight WHERE id =
    'HJ9003/' || CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE) || '/BRU');


