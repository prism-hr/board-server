SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM resource_operation;
DELETE FROM resource_category;
DELETE FROM user;

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

INSERT INTO user (id, uuid, given_name, surname, email, email_display, password, password_hash,
                  document_image_request_state, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', SHA2('password', 256), 'SHA256',
        'DISPLAY_FIRST', NOW());
