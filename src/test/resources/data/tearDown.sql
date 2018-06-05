SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM activity;
DELETE FROM activity_event;
DELETE FROM activity_role;
DELETE FROM activity_user;
DELETE FROM document;
DELETE FROM location;
DELETE FROM organization;
DELETE FROM resource;
DELETE FROM resource_category;
DELETE FROM resource_event;
DELETE FROM resource_event_search;
DELETE FROM resource_operation;
DELETE FROM resource_relation;
DELETE FROM resource_search;
DELETE FROM resource_task;
DELETE FROM test_email;
DELETE FROM user;
DELETE FROM user_notification_suppression;
DELETE FROM user_role;
DELETE FROM user_search;

SET FOREIGN_KEY_CHECKS = 1;
