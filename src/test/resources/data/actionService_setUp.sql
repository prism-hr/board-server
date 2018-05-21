SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM user;
DELETE FROM user_role;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, parent_id, name, handle, state, previous_state, index_data, quarter, created_timestamp)
VALUES
  (1, 'UNIVERSITY', 1, 'university', 'university', 'ACCEPTED', 'ACCEPTED', 'U516', '20182', '2018-05-21 18:42:12.179'),
  (2, 'DEPARTMENT', 1, 'department-accepted', 'university/department-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213', '20182', '2018-05-21 18:42:12.179'),
  (3, 'BOARD', 2, 'department-accepted-board-accepted', 'university/department-accepted/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213', '20182', '2018-05-21 18:42:12.179'),
  (4, 'POST', 3, 'department-accepted-board-accepted-post-accepted', 'university/department-accepted/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A213', '20182', '2018-05-21 18:42:12.179'),
  (5, 'BOARD', 2, 'department-accepted-board-rejected', 'university/department-accepted/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 A213 D163 A213 B630 R223', '20182', '2018-05-21 18:42:12.179'),
  (6, 'POST', 5, 'department-accepted-board-rejected-post-accepted', 'university/department-accepted/board-rejected/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 A213', '20182', '2018-05-21 18:42:12.179'),
  (7, 'DEPARTMENT', 1, 'department-rejected', 'university/department-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223', '20182', '2018-05-21 18:42:12.179'),
  (8, 'BOARD', 7, 'department-rejected-board-accepted', 'university/department-rejected/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213', '20182', '2018-05-21 18:42:12.179'),
  (9, 'POST', 8, 'department-rejected-board-accepted-post-accepted', 'university/department-rejected/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A213', '20182', '2018-05-21 18:42:12.179'),
  (10, 'BOARD', 7, 'department-rejected-board-rejected', 'university/department-rejected/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223 D163 R223 B630 R223', '20182', '2018-05-21 18:42:12.179'),
  (11, 'POST', 10, 'department-rejected-board-rejected-post-accepted', 'university/department-rejected/board-rejected/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 A213', '20182', '2018-05-21 18:42:12.179');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-21 08:16:29.609'),
  (2, 2, 2, '2018-05-21 08:16:29.609'),
  (3, 1, 2, '2018-05-21 08:16:29.609'),
  (4, 3, 3, '2018-05-21 08:16:29.609'),
  (5, 2, 3, '2018-05-21 08:16:29.609'),
  (6, 1, 3, '2018-05-21 08:16:29.609'),
  (7, 4, 4, '2018-05-21 08:16:29.609'),
  (8, 3, 4, '2018-05-21 08:16:29.609'),
  (9, 2, 4, '2018-05-21 08:16:29.609'),
  (10, 1, 4, '2018-05-21 08:16:29.609'),
  (11, 5, 5, '2018-05-21 08:16:29.609'),
  (12, 2, 5, '2018-05-21 08:16:29.609'),
  (13, 1, 5, '2018-05-21 08:16:29.609'),
  (14, 6, 6, '2018-05-21 08:16:29.609'),
  (15, 5, 6, '2018-05-21 08:16:29.609'),
  (16, 2, 6, '2018-05-21 08:16:29.609'),
  (17, 1, 6, '2018-05-21 08:16:29.609'),
  (18, 7, 7, '2018-05-21 08:16:29.609'),
  (19, 1, 7, '2018-05-21 08:16:29.609'),
  (20, 8, 8, '2018-05-21 08:16:29.609'),
  (21, 7, 8, '2018-05-21 08:16:29.609'),
  (22, 1, 8, '2018-05-21 08:16:29.609'),
  (23, 9, 9, '2018-05-21 08:16:29.609'),
  (24, 8, 9, '2018-05-21 08:16:29.609'),
  (25, 7, 9, '2018-05-21 08:16:29.609'),
  (26, 1, 9, '2018-05-21 08:16:29.609'),
  (27, 10, 10, '2018-05-21 08:16:29.609'),
  (28, 7, 10, '2018-05-21 08:16:29.609'),
  (29, 1, 10, '2018-05-21 08:16:29.609'),
  (30, 11, 11, '2018-05-21 08:16:29.609'),
  (31, 10, 11, '2018-05-21 08:16:29.609'),
  (32, 7, 11, '2018-05-21 08:16:29.609'),
  (33, 1, 11, '2018-05-21 08:16:29.609');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'department-administrator', 'department-administrator', 'department-administrator@prism.hr', 'd......................r@prism.hr', '2018-05-21 08:16:29.609'),
  (2, UUID(), 'other-department-administrator', 'other-department-administrator', 'other-department-administrator@prism.hr', 'o............................r@prism.hr', '2018-05-21 08:16:29.609'),
  (3, UUID(), 'department-author', 'department-author', 'department-author@prism.hr', 'd...............r@prism.hr', '2018-05-21 08:16:29.609'),
  (4, UUID(), 'other-department-author', 'other-department-author', 'other-department-author@prism.hr', 'o.....................r@prism.hr', '2018-05-21 08:16:29.609'),
  (5, UUID(), 'accepted-department-member', 'accepted-department-member', 'accepted-department-member@prism.hr', 'a........................r@prism.hr', '2018-05-21 08:16:29.609'),
  (6, UUID(), 'other-accepted-department-member', 'other-accepted-department-member', 'other-accepted-department-member@prism.hr', 'o..............................r@prism.hr', '2018-05-21 08:16:29.609'),
  (7, UUID(), 'pending-department-member', 'pending-department-member', 'pending-department-member@prism.hr', 'p.......................r@prism.hr', '2018-05-21 08:16:29.609'),
  (8, UUID(), 'other-pending-department-member', 'other-pending-department-member', 'other-pending-department-member@prism.hr', 'o.............................r@prism.hr', '2018-05-21 08:16:29.609'),
  (9, UUID(), 'rejected-department-member', 'rejected-department-member', 'rejected-department-member@prism.hr', 'r........................r@prism.hr', '2018-05-21 08:16:29.609'),
  (10, UUID(), 'other-rejected-department-member', 'other-rejected-department-member', 'other-rejected-department-member@prism.hr', 'o..............................r@prism.hr', '2018-05-21 08:16:29.609'),
  (11, UUID(), 'post-administrator', 'post-administrator', 'post-administrator@prism.hr', 'p................r@prism.hr', '2018-05-21 08:16:29.609'),
  (12, UUID(), 'other-post-administrator', 'other-post-administrator', 'other-post-administrator@prism.hr', 'o......................r@prism.hr', '2018-05-21 08:16:29.609'),
  (13, UUID(), 'unprivileged', 'unprivileged', 'unprivileged@prism.hr', 'u..........d@prism.hr', '2018-05-21 08:16:29.609');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (2, UUID(), 2, 3, 'AUTHOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (3, UUID(), 2, 5, 'MEMBER', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (4, UUID(), 2, 7, 'MEMBER', 'PENDING', '2018-05-21 08:16:29.609'),
  (5, UUID(), 2, 9, 'MEMBER', 'REJECTED', '2018-05-21 08:16:29.609'),
  (6, UUID(), 4, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (7, UUID(), 6, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (8, UUID(), 7, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (9, UUID(), 7, 3, 'AUTHOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (10, UUID(), 7, 5, 'MEMBER', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (11, UUID(), 7, 7, 'MEMBER', 'PENDING', '2018-05-21 08:16:29.609'),
  (12, UUID(), 7, 9, 'MEMBER', 'REJECTED', '2018-05-21 08:16:29.609'),
  (13, UUID(), 7, 2, 'ADMINISTRATOR', 'REJECTED', '2018-05-21 08:16:29.609'),
  (14, UUID(), 7, 4, 'AUTHOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (15, UUID(), 7, 6, 'MEMBER', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (16, UUID(), 7, 8, 'MEMBER', 'PENDING', '2018-05-21 08:16:29.609'),
  (17, UUID(), 7, 10, 'MEMBER', 'REJECTED', '2018-05-21 08:16:29.609'),
  (18, UUID(), 9, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 08:16:29.609'),
  (19, UUID(), 11, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 08:16:29.609');
