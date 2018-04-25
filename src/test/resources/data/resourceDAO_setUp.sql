SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE resource;
TRUNCATE TABLE resource_relation;
TRUNCATE TABLE user;
TRUNCATE TABLE user_role;
TRUNCATE TABLE resource_category;
TRUNCATE TABLE document;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO document(cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES('cloudinary id', 'cloudinary url', 'file name', NOW());

SET @documentLogoId = (SELECT
  last_insert_id());

INSERT INTO resource(scope, name, document_logo_id, state, created_timestamp)
VALUES('UNIVERSITY', 'university', @documentLogoId, 'ACCEPTED', NOW());

SET @universityId = (
  SELECT LAST_INSERT_ID());

INSERT INTO resource(parent_id, scope, name, document_logo_id, state, created_timestamp)
VALUES(@universityId, 'DEPARTMENT', 'department', @documentLogoId, 'DRAFT', NOW());

UPDATE resource
SET parent_id = @universityId
WHERE id = @universityId;

SET @departmentId = (
  SELECT LAST_INSERT_ID());

INSERT INTO user(uuid, given_name, surname, email, email_display, created_timestamp)
VALUES (UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', NOW());

SET @userId = (
  SELECT LAST_INSERT_ID());

INSERT INTO user_role(uuid, resource_id, user_id, role, state, created_timestamp)
VALUES(UUID(), @departmentId, @userId, 'ADMINISTRATOR', 'ACCEPTED', NOW());

INSERT INTO resource_relation(resource1_id, resource2_id, created_timestamp)
VALUES(@universityId, @universityId, NOW()),
  (@universityId, @departmentId, NOW()),
  (@departmentId, @departmentId, NOW());

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES (@departmentId, 'MEMBER', 'UNDERGRADUATE_STUDENT', NOW()),
  (@departmentId, 'MEMBER', 'MASTER_STUDENT', NOW());
