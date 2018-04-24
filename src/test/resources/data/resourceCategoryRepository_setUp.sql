SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resource_category;
TRUNCATE TABLE resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (scope, name, created_timestamp)
VALUES ('DEPARTMENT', 'department', NOW());

SET @resourceId = (
  SELECT LAST_INSERT_ID());

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES(@resourceId, 'MEMBER', 'category1', NOW()),
  (@resourceId, 'MEMBER', 'category2', NOW()),
  (@resourceId, 'POST', 'category3', NOW());
