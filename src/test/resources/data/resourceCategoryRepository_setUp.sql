SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource_category;
DELETE FROM resource;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, name, created_timestamp)
VALUES (1, 'DEPARTMENT', 'department', NOW());

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES(1, 'MEMBER', 'category1', NOW()),
  (1, 'MEMBER', 'category2', NOW()),
  (1, 'POST', 'category3', NOW());
