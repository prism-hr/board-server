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

INSERT INTO resource (id, scope, parent_id, name, handle, state, previous_state, index_data, quarter, created_timestamp)
VALUES
  (1, 'UNIVERSITY', 1, 'university', 'university', 'ACCEPTED', 'ACCEPTED', 'U516', '20182', '2018-05-19 08:05:29.392'),
  (2, 'DEPARTMENT', 1, 'department-accepted', 'university/department-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213', '20182', '2018-05-19 08:05:29.392'),
  (3, 'BOARD', 2, 'department-accepted-board-accepted', 'university/department-accepted/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213', '20182', '2018-05-19 08:05:29.392'),
  (4, 'POST', 3, 'department-accepted-board-accepted-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A213', '20182', '2018-05-19 08:05:29.392'),
  (5, 'DEPARTMENT', 1, 'department-rejected', 'university/department-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223', '20182', '2018-05-19 08:05:29.392'),
  (6, 'BOARD', 5, 'department-rejected-board-accepted', 'university/department-rejected/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213', '20182', '2018-05-19 08:05:29.392'),
  (7, 'POST', 6, 'department-rejected-board-accepted-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A213', '20182', '2018-05-19 08:05:29.392');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-19 08:05:29.392'),
  (2, 2, 2, '2018-05-19 08:05:29.392'),
  (3, 1, 2, '2018-05-19 08:05:29.392'),
  (4, 3, 3, '2018-05-19 08:05:29.392'),
  (5, 2, 3, '2018-05-19 08:05:29.392'),
  (6, 1, 3, '2018-05-19 08:05:29.392'),
  (7, 4, 4, '2018-05-19 08:05:29.392'),
  (8, 3, 4, '2018-05-19 08:05:29.392'),
  (9, 2, 4, '2018-05-19 08:05:29.392'),
  (10, 1, 4, '2018-05-19 08:05:29.392'),
  (11, 5, 5, '2018-05-19 08:05:29.392'),
  (12, 1, 5, '2018-05-19 08:05:29.392'),
  (13, 6, 6, '2018-05-19 08:05:29.392'),
  (14, 5, 6, '2018-05-19 08:05:29.392'),
  (15, 1, 6, '2018-05-19 08:05:29.392'),
  (16, 7, 7, '2018-05-19 08:05:29.392'),
  (17, 6, 7, '2018-05-19 08:05:29.392'),
  (18, 5, 7, '2018-05-19 08:05:29.392'),
  (19, 1, 7, '2018-05-19 08:05:29.392');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'department-administrator', 'department-administrator', 'department-administrator@prism.hr', 'd......................r@prism.hr', '2018-05-19 08:05:29.392'),
  (2, UUID(), 'other-department-administrator', 'other-department-administrator', 'other-department-administrator@prism.hr', 'o............................r@prism.hr', '2018-05-19 08:05:29.392'),
  (3, UUID(), 'department-author', 'department-author', 'department-author@prism.hr', 'd...............r@prism.hr', '2018-05-19 08:05:29.392'),
  (4, UUID(), 'other-department-author', 'other-department-author', 'other-department-author@prism.hr', 'o.....................r@prism.hr', '2018-05-19 08:05:29.392'),
  (5, UUID(), 'accepted-department-member', 'accepted-department-member', 'accepted-department-member@prism.hr', 'a........................r@prism.hr', '2018-05-19 08:05:29.392'),
  (6, UUID(), 'other-accepted-department-member', 'other-accepted-department-member', 'other-accepted-department-member@prism.hr', 'o..............................r@prism.hr', '2018-05-19 08:05:29.392'),
  (7, UUID(), 'post-administrator', 'post-administrator', 'post-administrator@prism.hr', 'p................r@prism.hr', '2018-05-19 08:05:29.392'),
  (8, UUID(), 'other-post-administrator', 'other-post-administrator', 'other-post-administrator@prism.hr', 'o......................r@prism.hr', '2018-05-19 08:05:29.392'),
  (9, UUID(), 'unprivileged', 'unprivileged', 'unprivileged@prism.hr', 'u..........d@prism.hr', '2018-05-19 08:05:29.392');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (2, UUID(), 2, 3, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (3, UUID(), 2, 5, 'MEMBER', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (4, UUID(), 4, 7, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (5, UUID(), 5, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (6, UUID(), 5, 3, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (7, UUID(), 5, 5, 'MEMBER', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (8, UUID(), 5, 2, 'ADMINISTRATOR', 'REJECTED', '2018-05-19 08:05:29.392'),
  (9, UUID(), 5, 4, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (10, UUID(), 5, 6, 'MEMBER', 'ACCEPTED', '2018-05-19 08:05:29.392'),
  (11, UUID(), 7, 8, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:05:29.392');
