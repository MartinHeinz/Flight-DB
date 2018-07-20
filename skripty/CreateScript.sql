CREATE TABLE passengers
(
id SERIAL PRIMARY KEY,
first_name VARCHAR(50) NOT NULL,
last_name VARCHAR(50) NOT NULL,
email VARCHAR(100) CHECK(email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$') NOT NULL UNIQUE
);

CREATE TABLE loyalty_cards
(
passenger_id INTEGER REFERENCES passengers(id),
discount NUMERIC(3,2) NOT NULL,
PRIMARY KEY(passenger_id)
);

CREATE TABLE destinations
(
id SERIAL PRIMARY KEY,
location POINT NOT NULL,
name VARCHAR(100) NOT NULL
);

CREATE TABLE pilots
(
id SERIAL PRIMARY KEY,
first_name VARCHAR(50) NOT NULL,
last_name VARCHAR(50) NOT NULL
);

CREATE TABLE planes
(
id SERIAL PRIMARY KEY,
seat_num INTEGER NOT NULL, -- check from number of tickets bought
code_name VARCHAR(100),
age INTEGER NOT NULL DEFAULT 0,
distance_flown INTEGER NOT NULL DEFAULT 0,
last_check_date DATE DEFAULT now(),
condition INTEGER DEFAULT 0 CHECK(condition >= 0)
);

CREATE TABLE flights
(
id SERIAL PRIMARY KEY,
take_off_time TIMESTAMP NOT NULL CHECK(take_off_time < landing_time),
landing_time TIMESTAMP CHECK(take_off_time < landing_time),
date DATE,
from_id INTEGER REFERENCES destinations(id) NOT NULL CHECK(from_id != to_id),
to_id INTEGER REFERENCES destinations(id) NOT NULL CHECK(from_id != to_id),
pilot_id INTEGER REFERENCES pilots(id) NOT NULL,
UNIQUE(pilot_id, date),
CHECK(extract(hour from take_off_time) - extract(hour from landing_time) < 12),
plane_id INTEGER REFERENCES planes(id)

);

CREATE TABLE tickets
(
id SERIAL PRIMARY KEY,
passenger_id INTEGER REFERENCES passengers(id),
flight_id INTEGER REFERENCES flights(id),
price NUMERIC(6,2) NOT NULL,
travel_class VARCHAR(50)
);


INSERT INTO passengers (first_name, last_name, email)
SELECT
	substring(md5(random()::text) from 1 for 10) AS first_name,
	substring(md5(random()::text) from 1 for 10) AS last_name,
	concat(substring(md5(random()::text) from 1 for 10), '@gmail.com') AS email
FROM
	generate_series(1,100) AS s;

INSERT INTO loyalty_cards (passenger_id, discount)
SELECT
	passengers.id,
	round(cast(random() AS numeric), 2) AS discount
FROM
	passengers
WHERE
	random() < 0.3;


INSERT INTO pilots (first_name, last_name)
SELECT
	substring(md5(random()::text) from 1 for 10) AS first_name,
	substring(md5(random()::text) from 1 for 10) AS last_name
FROM
	generate_series(1,25) AS s;

INSERT INTO destinations (location, name)
SELECT
	point(random()*90, random()*180) AS location,
	substring(md5(random()::text) from 1 for 10) AS name
FROM
	generate_series(1,2000) AS s;

INSERT INTO planes (seat_num, code_name, age, distance_flown, last_check_date, condition)
SELECT
	floor(random() * (500 - 50) + 50) AS seat_num,
	substring(md5(random()::text) from 1 for 10) AS code_name,
	floor(random() * (15 - 2) + 2) AS age,
	floor(random() * (20000 - 500) + 500) AS distance_flown,
	timestamp '2015-01-01 20:00:00' + random() * (now() - timestamp '2015-01-01 20:00:00') AS last_check_date,
	floor(random() * 15) AS condition
FROM
	generate_series(1, 10);