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
  (1, 'UNIVERSITY', 1, 'university', 'university', 'ACCEPTED', 'ACCEPTED', 'U516', '20182', '2018-05-19 08:06:47.066'),
  (2, 'DEPARTMENT', 1, 'department-accepted', 'university/department-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213', '20182', '2018-05-19 08:06:47.066'),
  (3, 'BOARD', 2, 'department-accepted-board-accepted', 'university/department-accepted/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213', '20182', '2018-05-19 08:06:47.066'),
  (4, 'POST', 3, 'department-accepted-board-accepted-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A213', '20182', '2018-05-19 08:06:47.066'),
  (5, 'BOARD', 2, 'department-accepted-board-rejected', 'university/department-accepted/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 A213 D163 A213 B630 R223', '20182', '2018-05-19 08:06:47.066'),
  (6, 'POST', 5, 'department-accepted-board-rejected-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 A213', '20182', '2018-05-19 08:06:47.066'),
  (7, 'DEPARTMENT', 1, 'department-rejected', 'university/department-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223', '20182', '2018-05-19 08:06:47.066'),
  (8, 'BOARD', 7, 'department-rejected-board-accepted', 'university/department-rejected/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213', '20182', '2018-05-19 08:06:47.066'),
  (9, 'POST', 8, 'department-rejected-board-accepted-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A213', '20182', '2018-05-19 08:06:47.066'),
  (10, 'BOARD', 7, 'department-rejected-board-rejected', 'university/department-rejected/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223 D163 R223 B630 R223', '20182', '2018-05-19 08:06:47.066'),
  (11, 'POST', 10, 'department-rejected-board-rejected-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 A213', '20182', '2018-05-19 08:06:47.066');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-19 08:06:47.066'),
  (2, 2, 2, '2018-05-19 08:06:47.066'),
  (3, 1, 2, '2018-05-19 08:06:47.066'),
  (4, 3, 3, '2018-05-19 08:06:47.066'),
  (5, 2, 3, '2018-05-19 08:06:47.066'),
  (6, 1, 3, '2018-05-19 08:06:47.066'),
  (7, 4, 4, '2018-05-19 08:06:47.066'),
  (8, 3, 4, '2018-05-19 08:06:47.066'),
  (9, 2, 4, '2018-05-19 08:06:47.066'),
  (10, 1, 4, '2018-05-19 08:06:47.066'),
  (11, 5, 5, '2018-05-19 08:06:47.066'),
  (12, 2, 5, '2018-05-19 08:06:47.066'),
  (13, 1, 5, '2018-05-19 08:06:47.066'),
  (14, 6, 6, '2018-05-19 08:06:47.066'),
  (15, 5, 6, '2018-05-19 08:06:47.066'),
  (16, 2, 6, '2018-05-19 08:06:47.066'),
  (17, 1, 6, '2018-05-19 08:06:47.066'),
  (18, 7, 7, '2018-05-19 08:06:47.066'),
  (19, 1, 7, '2018-05-19 08:06:47.066'),
  (20, 8, 8, '2018-05-19 08:06:47.066'),
  (21, 7, 8, '2018-05-19 08:06:47.066'),
  (22, 1, 8, '2018-05-19 08:06:47.066'),
  (23, 9, 9, '2018-05-19 08:06:47.066'),
  (24, 8, 9, '2018-05-19 08:06:47.066'),
  (25, 7, 9, '2018-05-19 08:06:47.066'),
  (26, 1, 9, '2018-05-19 08:06:47.066'),
  (27, 10, 10, '2018-05-19 08:06:47.066'),
  (28, 7, 10, '2018-05-19 08:06:47.066'),
  (29, 1, 10, '2018-05-19 08:06:47.066'),
  (30, 11, 11, '2018-05-19 08:06:47.066'),
  (31, 10, 11, '2018-05-19 08:06:47.066'),
  (32, 7, 11, '2018-05-19 08:06:47.066'),
  (33, 1, 11, '2018-05-19 08:06:47.066');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'department-administrator', 'department-administrator', 'department-administrator@prism.hr', 'd......................r@prism.hr', '2018-05-19 08:06:47.066'),
  (2, UUID(), 'other-department-administrator', 'other-department-administrator', 'other-department-administrator@prism.hr', 'o............................r@prism.hr', '2018-05-19 08:06:47.066'),
  (3, UUID(), 'department-author', 'department-author', 'department-author@prism.hr', 'd...............r@prism.hr', '2018-05-19 08:06:47.066'),
  (4, UUID(), 'other-department-author', 'other-department-author', 'other-department-author@prism.hr', 'o.....................r@prism.hr', '2018-05-19 08:06:47.066'),
  (5, UUID(), 'accepted-department-member', 'accepted-department-member', 'accepted-department-member@prism.hr', 'a........................r@prism.hr', '2018-05-19 08:06:47.066'),
  (6, UUID(), 'other-accepted-department-member', 'other-accepted-department-member', 'other-accepted-department-member@prism.hr', 'o..............................r@prism.hr', '2018-05-19 08:06:47.066'),
  (7, UUID(), 'post-administrator', 'post-administrator', 'post-administrator@prism.hr', 'p................r@prism.hr', '2018-05-19 08:06:47.066'),
  (8, UUID(), 'other-post-administrator', 'other-post-administrator', 'other-post-administrator@prism.hr', 'o......................r@prism.hr', '2018-05-19 08:06:47.066'),
  (9, UUID(), 'unprivileged', 'unprivileged', 'unprivileged@prism.hr', 'u..........d@prism.hr', '2018-05-19 08:06:47.066');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (2, UUID(), 2, 3, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (3, UUID(), 2, 5, 'MEMBER', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (4, UUID(), 4, 7, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (5, UUID(), 6, 7, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (6, UUID(), 7, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (7, UUID(), 7, 3, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (8, UUID(), 7, 5, 'MEMBER', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (9, UUID(), 7, 2, 'ADMINISTRATOR', 'REJECTED', '2018-05-19 08:06:47.066'),
  (10, UUID(), 7, 4, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (11, UUID(), 7, 6, 'MEMBER', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (12, UUID(), 9, 8, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.066'),
  (13, UUID(), 11, 8, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.066');
