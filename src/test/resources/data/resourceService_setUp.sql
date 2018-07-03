INSERT INTO resource (id, parent_id, scope, name, handle, state, index_data, created_timestamp)
VALUES (1, 1, 'UNIVERSITY', 'university', 'university', 'ACCEPTED', 'U516', '2018-05-01 09:00:00');

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
VALUES (1, 1, '2018-05-01 09:00:00');

INSERT INTO resource (id, parent_id, scope, name, summary, handle, state, created_timestamp)
VALUES (2, 1, 'DEPARTMENT', 'department', 'department summary', 'university/department', 'DRAFT', '2018-05-01 09:00:00'),
  (3, 1, 'DEPARTMENT', 'department 2', 'department 2 summary', 'university/department-2', 'DRAFT', '2018-05-01 09:00:00');

INSERT INTO resource (id, parent_id, scope, name, handle, state, created_timestamp)
VALUES (4, 2, 'BOARD', 'board', 'university/department/board', 'ACCEPTED', '2018-05-01 09:00:00'),
  (5, 2, 'BOARD', 'board 2', 'university/department/board-2', 'ACCEPTED', '2018-05-01 09:00:00');

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES(4, 'POST', 'Internship', '2018-05-01 09:00:00'),
  (4, 'POST', 'Employment', '2018-05-01 09:00:00');

INSERT INTO organization(name, logo, created_timestamp)
VALUES ('organization name', 'organization logo', '2018-05-01 09:00:00');

SET @organizationId = (
  SELECT LAST_INSERT_ID());

INSERT INTO location(name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES('london', 'gb', 'google', 1, 1, '2018-05-01 09:00:00');

SET @locationId = (
  SELECT LAST_INSERT_ID());

INSERT INTO resource (id, parent_id, scope, name, summary, description, location_id, organization_id, state,
                      created_timestamp)
VALUES (6, 4, 'POST', 'post', 'post summary', 'post description', @locationId, @organizationId, 'ACCEPTED', '2018-05-01 09:00:00');

INSERT INTO resource_category(resource_id, type, name, created_timestamp)
VALUES(6, 'POST', 'Internship', '2018-05-01 09:00:00'),
  (6, 'POST', 'Employment', '2018-05-01 09:00:00'),
  (6, 'MEMBER', 'UNDERGRADUATE_STUDENT', '2018-05-01 09:00:00'),
  (6, 'MEMBER', 'MASTER_STUDENT', '2018-05-01 09:00:00');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, password, password_hash,
                  document_image_request_state, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', SHA2('password', 256), 'SHA256',
        'DISPLAY_FIRST', '2018-05-01 09:00:00');
