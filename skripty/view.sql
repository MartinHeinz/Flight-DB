CREATE VIEW pilot_stats
(pilot_id, month, year, flights_count)
AS
SELECT
pilot_id,
to_char(to_timestamp(to_char(date_part('month', date), '999'), 'MM'), 'Mon') AS month,
date_part('year', date) AS year,
count(pilot_id) AS flights_count
FROM
flights
GROUP BY pilot_id, month, year
ORDER BY pilot_id