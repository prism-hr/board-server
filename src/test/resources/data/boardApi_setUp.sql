SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO document (id, cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES (1, 'cloudinary id', 'cloudinary url', 'file name', NOW());

INSERT INTO resource (id, scope, name, handle, document_logo_id, state, created_timestamp)
VALUES (1, 'UNIVERSITY', 'university', 'university', 1, 'ACCEPTED', NOW());

UPDATE resource
SET parent_id = 1
WHERE id = 1;

INSERT INTO resource (id, parent_id, scope, name, handle, document_logo_id, state, created_timestamp)
VALUES (2, 1, 'DEPARTMENT', 'department', 'university/department', 1, 'DRAFT', NOW());

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
VALUES (1, 1, NOW()),
  (1, 2, NOW()),
  (2, 2, NOW());

INSERT INTO user (id, uuid, given_name, surname, email, email_display, password, password_hash,
                  document_image_request_state, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', SHA2('password', 256), 'SHA256',
        'DISPLAY_FIRST', NOW());

INSERT INTO user_role(id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES(1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', NOW());
