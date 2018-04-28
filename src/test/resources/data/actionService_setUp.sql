SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;
DELETE FROM resource_task;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, name, state, created_timestamp)
VALUES (1, 'UNIVERSITY', 'university', 'ACCEPTED', NOW());