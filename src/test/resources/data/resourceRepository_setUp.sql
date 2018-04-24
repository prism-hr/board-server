SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (scope, name, handle, created_timestamp)
VALUES ('DEPARTMENT', 'department', 'university/department', NOW()),
  ('DEPARTMENT', 'department 2', 'university/department-2', NOW()),
  ('DEPARTMENT', 'other department', 'university/other-department', NOW());
