SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, name, handle, state, created_timestamp)
VALUES (1, 'UNIVERSITY', 'university', 'university', 'ACCEPTED', NOW());

UPDATE resource
SET parent_id = id
WHERE scope = 'UNIVERSITY';

INSERT INTO resource (id, parent_id, scope, name, handle, state, created_timestamp)
VALUES (2, 1, 'DEPARTMENT', 'department', 'university/department', 'DRAFT', NOW()),
  (3, 1, 'DEPARTMENT', 'department 2', 'university/department-2', 'DRAFT', NOW());

INSERT INTO resource (id, parent_id, scope, name, handle, state, created_timestamp)
VALUES (4, 2, 'BOARD', 'board', 'university/department/board', 'ACCEPTED', NOW()),
  (5, 2, 'BOARD', 'board 2', 'university/department/board-2', 'ACCEPTED', NOW());
