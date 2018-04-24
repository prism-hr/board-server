SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource(scope, name, created_timestamp)
VALUES('UNIVERSITY', 'university', NOW());

INSERT INTO resource(parent_id, scope, name, created_timestamp)
VALUES(LAST_INSERT_ID(), 'DEPARTMENT', 'department', NOW());
