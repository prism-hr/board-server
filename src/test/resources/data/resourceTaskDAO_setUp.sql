SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_category;
DELETE FROM user;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, name, state, created_timestamp)
VALUES (1, 'DEPARTMENT', 'department', 'DRAFT', NOW());

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', NOW());
