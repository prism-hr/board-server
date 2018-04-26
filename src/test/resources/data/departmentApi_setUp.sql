SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resource;
TRUNCATE TABLE resource_relation;
TRUNCATE TABLE user;
TRUNCATE TABLE user_role;
TRUNCATE TABLE resource_category;
TRUNCATE TABLE document;
TRUNCATE TABLE resource_task;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO document (cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES ('cloudinary id', 'cloudinary url', 'file name', NOW());

SET @documentLogoId = (
  SELECT last_insert_id());

INSERT INTO resource (scope, name, document_logo_id, state, created_timestamp)
VALUES ('UNIVERSITY', 'university', @documentLogoId, 'ACCEPTED', NOW());

INSERT INTO user (uuid, given_name, surname, email, email_display, password, password_hash,
                  document_image_request_state, created_timestamp)
VALUES (UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', SHA2('password', 256), 'SHA256',
        'DISPLAY_FIRST', NOW());
