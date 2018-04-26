SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, name, handle, created_timestamp)
VALUES (1, 'DEPARTMENT', 'department', 'university/department', NOW()),
  (2, 'DEPARTMENT', 'department 2', 'university/department-2', NOW()),
  (3, 'DEPARTMENT', 'other department', 'university/other-department', NOW());
