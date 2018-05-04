SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM resource_operation;
DELETE FROM resource_category;
DELETE FROM user;
DELETE FROM organization;
DELETE FROM location;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, parent_id, scope, name, handle, state, index_data, created_timestamp)
VALUES (1, 1, 'UNIVERSITY', 'university', 'university', 'ACCEPTED', 'U516', NOW());

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
VALUES (1, 1, NOW());

INSERT INTO resource (id, parent_id, scope, name, summary, handle, state, created_timestamp)
VALUES (2, 1, 'DEPARTMENT', 'department', 'department summary', 'university/department', 'DRAFT', NOW()),
  (3, 1, 'DEPARTMENT', 'department 2', 'department 2 summary', 'university/department-2', 'DRAFT', NOW());

INSERT INTO resource (id, parent_id, scope, name, handle, state, created_timestamp)
VALUES (4, 2, 'BOARD', 'board', 'university/department/board', 'ACCEPTED', NOW()),
  (5, 2, 'BOARD', 'board 2', 'university/department/board-2', 'ACCEPTED', NOW());

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES(4, 'POST', 'Internship', NOW()),
  (4, 'POST', 'Employment', NOW());

INSERT INTO organization(name, logo, created_timestamp)
VALUES ('organization name', 'organization logo', NOW());

SET @organizationId = (
  SELECT LAST_INSERT_ID());

INSERT INTO location(name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES('london', 'gb', 'google', 1, 1, NOW());

SET @locationId = (
  SELECT LAST_INSERT_ID());

INSERT INTO resource (id, parent_id, scope, name, summary, description, location_id, organization_id, state,
                      created_timestamp)
VALUES (6, 4, 'POST', 'post', 'post summary', 'post description', @locationId, @organizationId, 'ACCEPTED', NOW());

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES(6, 'POST', 'Internship', NOW()),
  (6, 'POST', 'Employment', NOW()),
  (6, 'MEMBER', 'UNDERGRADUATE_STUDENT', NOW()),
  (6, 'MEMBER', 'MASTER_STUDENT', NOW());

INSERT INTO user (id, uuid, given_name, surname, email, email_display, password, password_hash,
                  document_image_request_state, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', SHA2('password', 256), 'SHA256',
        'DISPLAY_FIRST', NOW());
