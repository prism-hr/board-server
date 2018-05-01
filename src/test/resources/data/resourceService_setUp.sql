SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource(id, scope, name, state, created_timestamp)
VALUES(1, 'UNIVERSITY', 'university', 'ACCEPTED', NOW());

UPDATE resource
SET parent_id = 1
WHERE id = 1;

INSERT INTO resource(id, parent_id, scope, name, state, created_timestamp)
VALUES(2, 1, 'DEPARTMENT', 'department', 'DRAFT', NOW());

INSERT INTO resource(id, parent_id, scope, name, state, created_timestamp)
VALUES(3, 2, 'BOARD', 'board', 'ACCEPTED', NOW());
