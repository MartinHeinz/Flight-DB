ALTER TABLE loyalty_cards ALTER COLUMN discount SET NOT NULL;

ALTER TABLE passengers ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE passengers ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE passengers ALTER COLUMN email SET NOT NULL;
ALTER TABLE passengers ADD CONSTRAINT email_unique UNIQUE(email);
ALTER TABLE passengers ADD CONSTRAINT email_check CHECK(email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$');

ALTER TABLE tickets ALTER COLUMN price SET NOT NULL;

ALTER TABLE flights ALTER COLUMN take_off_time SET NOT NULL;
ALTER TABLE flights ADD CONSTRAINT time CHECK(take_off_time < landing_time);
ALTER TABLE flights ADD CONSTRAINT time_check CHECK(extract(hour from take_off_time) - extract(hour from landing_time) < 24);
ALTER TABLE flights ADD CONSTRAINT pilot_date_unique UNIQUE(date, pilot_id);

ALTER TABLE pilots ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE pilots ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE destinations ALTER COLUMN latitude SET NOT NULL;
ALTER TABLE destinations ALTER COLUMN longitude SET NOT NULL;
ALTER TABLE destinations ALTER COLUMN name SET NOT NULL;

ALTER TABLE planes ALTER COLUMN seat_num SET NOT NULL;
ALTER TABLE planes ALTER COLUMN age SET NOT NULL;
ALTER TABLE planes ALTER COLUMN distance_flown SET NOT NULL;