SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM resource_operation;
DELETE FROM resource_category;
DELETE FROM user;

SET FOREIGN_KEY_CHECKS = 1;
