CREATE INDEX dest_index ON destinations (name);


EXPLAIN ANALYZE SELECT * FROM destinations WHERE name = 'London'; -- pre 2000 rows
"Seq Scan on destinations  (cost=0.00..41.99 rows=1 width=30) (actual time=0.009..0.180 rows=1 loops=1)"
"  Filter: ((name)::text = 'London'::text)"
"  Rows Removed by Filter: 1998"
"Planning time: 0.906 ms"
"Execution time: 0.195 ms"


--s indexom na name
"Index Scan using dest_index on destinations  (cost=0.28..8.29 rows=1 width=30) (actual time=0.028..0.028 rows=1 loops=1)"
"  Index Cond: ((name)::text = 'London'::text)"
"Planning time: 0.677 ms"
"Execution time: 0.042 ms"


EXPLAIN ANALYZE SELECT * FROM flights WHERE pilot_id = '17' AND date = '2016-08-29';
-- index je automaticky vytvoreny kvoli cudziemu klucu (pilot_id, date)
"Index Scan using flights_pilot_id_date_key on flights  (cost=0.28..8.30 rows=1 width=40) (actual time=0.009..0.010 rows=1 loops=1)"
"  Index Cond: ((pilot_id = 17) AND (date = '2016-08-29'::date))"
"Planning time: 1.298 ms"
"Execution time: 0.028 ms"


CREATE INDEX plane_date_index ON flights (plane_id, date);
EXPLAIN ANALYZE SELECT * FROM flights WHERE plane_id = '8' AND date = '2016-10-26';
-- 1037 rows vo flights
"Seq Scan on flights  (cost=0.00..29.56 rows=1 width=40) (actual time=0.021..0.115 rows=1 loops=1)"
"  Filter: ((plane_id = 8) AND (date = '2016-10-26'::date))"
"  Rows Removed by Filter: 1036"
"Planning time: 0.094 ms"
"Execution time: 0.134 ms"


"Index Scan using plane_date_index on flights  (cost=0.28..8.30 rows=1 width=40) (actual time=0.010..0.010 rows=1 loops=1)"
"  Index Cond: ((plane_id = 8) AND (date = '2016-10-26'::date))"
"Planning time: 0.085 ms"
"Execution time: 0.025 ms"

