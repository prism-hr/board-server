SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO document(id, cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES(1, 'cloudinary id', 'cloudinary url', 'file name', NOW());

INSERT INTO resource(id, scope, name, document_logo_id, state, created_timestamp)
VALUES(1, 'UNIVERSITY', 'university', 1, 'ACCEPTED', NOW());

UPDATE resource
SET parent_id = 1
WHERE id = 1;

INSERT INTO resource(id, parent_id, scope, name, document_logo_id, state, created_timestamp)
VALUES(2, 1, 'DEPARTMENT', 'department', 1, 'DRAFT', NOW());

INSERT INTO resource(id, parent_id, scope, name, state, created_timestamp)
VALUES(3, 2, 'BOARD', 'board', 'ACCEPTED', NOW());

INSERT INTO user(id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', NOW());

INSERT INTO user_role(id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES(1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', NOW());

INSERT INTO resource_relation(resource1_id, resource2_id, created_timestamp)
VALUES(1, 1, NOW()),
  (1, 2, NOW()),
  (2, 2, NOW()),
  (1, 3, NOW()),
  (2, 3, NOW()),
  (3, 3, NOW());

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES (2, 'MEMBER', 'UNDERGRADUATE_STUDENT', NOW()),
  (2, 'MEMBER', 'MASTER_STUDENT', NOW());
