SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM resource_operation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, parent_id, name, handle, state, previous_state, index_data, quarter, created_timestamp)
VALUES
  (1, 'UNIVERSITY', 1, 'university', 'university', 'ACCEPTED', 'ACCEPTED', 'U516', '20182', '2018-05-21 21:04:54.171'),
  (2, 'DEPARTMENT', 1, 'department-accepted', 'university/department-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213', '20182', '2018-05-21 21:04:54.171'),
  (3, 'BOARD', 2, 'department-accepted-board-accepted', 'university/department-accepted/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213', '20182', '2018-05-21 21:04:54.171'),
  (4, 'POST', 3, 'department-accepted-board-accepted-post-accepted', 'university/department-accepted/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A213', '20182', '2018-05-21 21:04:54.171'),
  (5, 'BOARD', 2, 'department-accepted-board-rejected', 'university/department-accepted/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 A213 D163 A213 B630 R223', '20182', '2018-05-21 21:04:54.171'),
  (6, 'POST', 5, 'department-accepted-board-rejected-post-accepted', 'university/department-accepted/board-rejected/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 A213', '20182', '2018-05-21 21:04:54.171'),
  (7, 'DEPARTMENT', 1, 'department-rejected', 'university/department-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223', '20182', '2018-05-21 21:04:54.171'),
  (8, 'BOARD', 7, 'department-rejected-board-accepted', 'university/department-rejected/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213', '20182', '2018-05-21 21:04:54.171'),
  (9, 'POST', 8, 'department-rejected-board-accepted-post-accepted', 'university/department-rejected/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A213', '20182', '2018-05-21 21:04:54.171'),
  (10, 'BOARD', 7, 'department-rejected-board-rejected', 'university/department-rejected/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223 D163 R223 B630 R223', '20182', '2018-05-21 21:04:54.171'),
  (11, 'POST', 10, 'department-rejected-board-rejected-post-accepted', 'university/department-rejected/board-rejected/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 A213', '20182', '2018-05-21 21:04:54.171');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-21 21:04:54.171'),
  (2, 2, 2, '2018-05-21 21:04:54.171'),
  (3, 1, 2, '2018-05-21 21:04:54.171'),
  (4, 3, 3, '2018-05-21 21:04:54.171'),
  (5, 2, 3, '2018-05-21 21:04:54.171'),
  (6, 1, 3, '2018-05-21 21:04:54.171'),
  (7, 4, 4, '2018-05-21 21:04:54.171'),
  (8, 3, 4, '2018-05-21 21:04:54.171'),
  (9, 2, 4, '2018-05-21 21:04:54.171'),
  (10, 1, 4, '2018-05-21 21:04:54.171'),
  (11, 5, 5, '2018-05-21 21:04:54.171'),
  (12, 2, 5, '2018-05-21 21:04:54.171'),
  (13, 1, 5, '2018-05-21 21:04:54.171'),
  (14, 6, 6, '2018-05-21 21:04:54.171'),
  (15, 5, 6, '2018-05-21 21:04:54.171'),
  (16, 2, 6, '2018-05-21 21:04:54.171'),
  (17, 1, 6, '2018-05-21 21:04:54.171'),
  (18, 7, 7, '2018-05-21 21:04:54.171'),
  (19, 1, 7, '2018-05-21 21:04:54.171'),
  (20, 8, 8, '2018-05-21 21:04:54.171'),
  (21, 7, 8, '2018-05-21 21:04:54.171'),
  (22, 1, 8, '2018-05-21 21:04:54.171'),
  (23, 9, 9, '2018-05-21 21:04:54.171'),
  (24, 8, 9, '2018-05-21 21:04:54.171'),
  (25, 7, 9, '2018-05-21 21:04:54.171'),
  (26, 1, 9, '2018-05-21 21:04:54.171'),
  (27, 10, 10, '2018-05-21 21:04:54.171'),
  (28, 7, 10, '2018-05-21 21:04:54.171'),
  (29, 1, 10, '2018-05-21 21:04:54.171'),
  (30, 11, 11, '2018-05-21 21:04:54.171'),
  (31, 10, 11, '2018-05-21 21:04:54.171'),
  (32, 7, 11, '2018-05-21 21:04:54.171'),
  (33, 1, 11, '2018-05-21 21:04:54.171');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'department-administrator', 'department-administrator', 'department-administrator@prism.hr', 'd......................r@prism.hr', '2018-05-21 21:04:54.171'),
  (2, UUID(), 'department-author', 'department-author', 'department-author@prism.hr', 'd...............r@prism.hr', '2018-05-21 21:04:54.171'),
  (3, UUID(), 'department-member-pending', 'department-member-pending', 'department-member-pending@prism.hr', 'd.......................g@prism.hr', '2018-05-21 21:04:54.171'),
  (4, UUID(), 'department-member-accepted', 'department-member-accepted', 'department-member-accepted@prism.hr', 'd........................d@prism.hr', '2018-05-21 21:04:54.171'),
  (5, UUID(), 'department-member-rejected', 'department-member-rejected', 'department-member-rejected@prism.hr', 'd........................d@prism.hr', '2018-05-21 21:04:54.171'),
  (6, UUID(), 'department-accepted-administrator', 'department-accepted-administrator', 'department-accepted-administrator@prism.hr', 'd...............................r@prism.hr', '2018-05-21 21:04:54.171'),
  (7, UUID(), 'department-accepted-author', 'department-accepted-author', 'department-accepted-author@prism.hr', 'd........................r@prism.hr', '2018-05-21 21:04:54.171'),
  (8, UUID(), 'department-accepted-member-pending', 'department-accepted-member-pending', 'department-accepted-member-pending@prism.hr', 'd................................g@prism.hr', '2018-05-21 21:04:54.171'),
  (9, UUID(), 'department-accepted-member-accepted', 'department-accepted-member-accepted', 'department-accepted-member-accepted@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.171'),
  (10, UUID(), 'department-accepted-member-rejected', 'department-accepted-member-rejected', 'department-accepted-member-rejected@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.171'),
  (11, UUID(), 'department-accepted-post-administrator', 'department-accepted-post-administrator', 'department-accepted-post-administrator@prism.hr', 'd....................................r@prism.hr', '2018-05-21 21:04:54.171'),
  (12, UUID(), 'department-rejected-administrator', 'department-rejected-administrator', 'department-rejected-administrator@prism.hr', 'd...............................r@prism.hr', '2018-05-21 21:04:54.171'),
  (13, UUID(), 'department-rejected-author', 'department-rejected-author', 'department-rejected-author@prism.hr', 'd........................r@prism.hr', '2018-05-21 21:04:54.171'),
  (14, UUID(), 'department-rejected-member-pending', 'department-rejected-member-pending', 'department-rejected-member-pending@prism.hr', 'd................................g@prism.hr', '2018-05-21 21:04:54.171'),
  (15, UUID(), 'department-rejected-member-accepted', 'department-rejected-member-accepted', 'department-rejected-member-accepted@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.171'),
  (16, UUID(), 'department-rejected-member-rejected', 'department-rejected-member-rejected', 'department-rejected-member-rejected@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.171'),
  (17, UUID(), 'department-rejected-post-administrator', 'department-rejected-post-administrator', 'department-rejected-post-administrator@prism.hr', 'd....................................r@prism.hr', '2018-05-21 21:04:54.171'),
  (18, UUID(), 'no-roles', 'no-roles', 'no-roles@prism.hr', 'n......s@prism.hr', '2018-05-21 21:04:54.171');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (2, UUID(), 2, 2, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (3, UUID(), 2, 3, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.171'),
  (4, UUID(), 2, 4, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (5, UUID(), 2, 5, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.171'),
  (6, UUID(), 2, 6, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (7, UUID(), 2, 7, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (8, UUID(), 2, 8, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.171'),
  (9, UUID(), 2, 9, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (10, UUID(), 2, 10, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.171'),
  (11, UUID(), 4, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (12, UUID(), 6, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (13, UUID(), 7, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (14, UUID(), 7, 2, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (15, UUID(), 7, 3, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.171'),
  (16, UUID(), 7, 4, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (17, UUID(), 7, 5, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.171'),
  (18, UUID(), 7, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (19, UUID(), 7, 13, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (20, UUID(), 7, 14, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.171'),
  (21, UUID(), 7, 15, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (22, UUID(), 7, 16, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.171'),
  (23, UUID(), 9, 17, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171'),
  (24, UUID(), 11, 17, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.171');
