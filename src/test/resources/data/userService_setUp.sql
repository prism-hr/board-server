SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM user;
DELETE FROM organization;
DELETE FROM location;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', NOW());

INSERT INTO organization(id, name, logo, created_timestamp)
VALUES (1, 'organization name', 'organization logo', NOW());

INSERT INTO location(id, name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES(1, 'london', 'gb', 'google', 1, 1, NOW());
