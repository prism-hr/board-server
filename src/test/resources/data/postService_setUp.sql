SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM resource_operation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;
DELETE FROM resource_task;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO user (id, uuid, given_name, surname, email, email_display, password, password_hash,
                  document_image_request_state, created_timestamp)
VALUES
  (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', SHA2('password', 256), 'SHA256',
   'DISPLAY_FIRST', NOW()),
  (2, UUID(), 'jakub', 'fibinger', 'jakub@prism.hr', 'jakub@prism.hr', SHA2('password', 256), 'SHA256', 'DISPLAY_FIRST',
   NOW()),
  (3, UUID(), 'juan', 'mingo', 'juan@prism.hr', 'juan@prism.hr', SHA2('password', 256), 'SHA256', 'DISPLAY_FIRST',
   NOW()),
  (4, UUID(), 'chris', 'neil', 'chris@prism.hr', 'chris@prism.hr', SHA2('password', 256), 'SHA256', 'DISPLAY_FIRST',
   NOW());

INSERT INTO resource (id, parent_id, scope, name, handle, state, created_timestamp)
VALUES (1, 1, 'UNIVERSITY', 'university', 'university', 'ACCEPTED', NOW()),
  (2, 1, 'DEPARTMENT', 'department1', 'university/department1', 'ACCEPTED', NOW()),
  (3, 1, 'DEPARTMENT', 'department2', 'university/department2', 'REJECTED', NOW()),
  (4, 1, 'DEPARTMENT', 'department3', 'university/department3', 'ACCEPTED', NOW()),
  (5, 2, 'BOARD', 'board1', 'university/department1/board1', 'ACCEPTED', NOW()),
  (6, 2, 'BOARD', 'board2', 'university/department1/board2', 'ACCEPTED', NOW()),
  (7, 3, 'BOARD', 'board1', 'university/department2/board1', 'ACCEPTED', NOW()),
  (8, 3, 'BOARD', 'board2', 'university/department2/board2', 'ACCEPTED', NOW()),
  (9, 4, 'BOARD', 'board1', 'university/department3/board1', 'REJECTED', NOW()),
  (10, 4, 'BOARD', 'board2', 'university/department3/board2', 'ACCEPTED', NOW()),
  (11, 5, 'POST', 'post1', null, 'DRAFT', NOW()),
  (12, 5, 'POST', 'post2', null, 'PENDING', NOW()),






