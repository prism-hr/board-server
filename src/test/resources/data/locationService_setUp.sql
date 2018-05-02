SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM location;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO location (id, name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES (1, 'london', 'gb', 'google', 1, 1, NOW());
