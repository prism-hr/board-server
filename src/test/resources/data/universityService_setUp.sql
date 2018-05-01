SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, name, handle, state, created_timestamp)
VALUES (1, 'UNIVERSITY', 'university', 'university', 'ACCEPTED', NOW());
