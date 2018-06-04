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
  (1, 'UNIVERSITY', 1, 'university', 'university', 'ACCEPTED', 'ACCEPTED', 'U516', '20182', '2018-05-21 21:04:54.155'),
  (2, 'DEPARTMENT', 1, 'department-draft', 'university/department-draft', 'DRAFT', 'DRAFT', 'U516 D163 D613', '20182', '2018-05-21 21:04:54.155'),
  (3, 'BOARD', 2, 'department-draft-board-accepted', 'university/department-draft/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 D613 D163 D613 B630 A213', '20182', '2018-05-21 21:04:54.155'),
  (4, 'POST', 3, 'department-draft-board-accepted-post-accepted', 'university/department-draft/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 D613 D163 D613 B630 A213 D163 D613 B630 A213 P230 A213', '20182', '2018-05-21 21:04:54.155'),
  (5, 'DEPARTMENT', 1, 'department-pending', 'university/department-pending', 'PENDING', 'PENDING', 'U516 D163 P535', '20182', '2018-05-21 21:04:54.155'),
  (6, 'BOARD', 5, 'department-pending-board-accepted', 'university/department-pending/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 P535 D163 P535 B630 A213', '20182', '2018-05-21 21:04:54.155'),
  (7, 'POST', 6, 'department-pending-board-accepted-post-accepted', 'university/department-pending/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 P535 D163 P535 B630 A213 D163 P535 B630 A213 P230 A213', '20182', '2018-05-21 21:04:54.155'),
  (8, 'DEPARTMENT', 1, 'department-accepted', 'university/department-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213', '20182', '2018-05-21 21:04:54.155'),
  (9, 'BOARD', 8, 'department-accepted-board-accepted', 'university/department-accepted/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213', '20182', '2018-05-21 21:04:54.155'),
  (10, 'POST', 9, 'department-accepted-board-accepted-post-accepted', 'university/department-accepted/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A213', '20182', '2018-05-21 21:04:54.155'),
  (11, 'DEPARTMENT', 1, 'department-rejected', 'university/department-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223', '20182', '2018-05-21 21:04:54.155'),
  (12, 'BOARD', 11, 'department-rejected-board-accepted', 'university/department-rejected/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213', '20182', '2018-05-21 21:04:54.155'),
  (13, 'POST', 12, 'department-rejected-board-accepted-post-accepted', 'university/department-rejected/board-accepted/post-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A213', '20182', '2018-05-21 21:04:54.155');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-21 21:04:54.155'),
  (2, 2, 2, '2018-05-21 21:04:54.155'),
  (3, 1, 2, '2018-05-21 21:04:54.155'),
  (4, 3, 3, '2018-05-21 21:04:54.155'),
  (5, 2, 3, '2018-05-21 21:04:54.155'),
  (6, 1, 3, '2018-05-21 21:04:54.155'),
  (7, 4, 4, '2018-05-21 21:04:54.155'),
  (8, 3, 4, '2018-05-21 21:04:54.155'),
  (9, 2, 4, '2018-05-21 21:04:54.155'),
  (10, 1, 4, '2018-05-21 21:04:54.155'),
  (11, 5, 5, '2018-05-21 21:04:54.155'),
  (12, 1, 5, '2018-05-21 21:04:54.155'),
  (13, 6, 6, '2018-05-21 21:04:54.155'),
  (14, 5, 6, '2018-05-21 21:04:54.155'),
  (15, 1, 6, '2018-05-21 21:04:54.155'),
  (16, 7, 7, '2018-05-21 21:04:54.155'),
  (17, 6, 7, '2018-05-21 21:04:54.155'),
  (18, 5, 7, '2018-05-21 21:04:54.155'),
  (19, 1, 7, '2018-05-21 21:04:54.155'),
  (20, 8, 8, '2018-05-21 21:04:54.155'),
  (21, 1, 8, '2018-05-21 21:04:54.155'),
  (22, 9, 9, '2018-05-21 21:04:54.155'),
  (23, 8, 9, '2018-05-21 21:04:54.155'),
  (24, 1, 9, '2018-05-21 21:04:54.155'),
  (25, 10, 10, '2018-05-21 21:04:54.155'),
  (26, 9, 10, '2018-05-21 21:04:54.155'),
  (27, 8, 10, '2018-05-21 21:04:54.155'),
  (28, 1, 10, '2018-05-21 21:04:54.155'),
  (29, 11, 11, '2018-05-21 21:04:54.155'),
  (30, 1, 11, '2018-05-21 21:04:54.155'),
  (31, 12, 12, '2018-05-21 21:04:54.155'),
  (32, 11, 12, '2018-05-21 21:04:54.155'),
  (33, 1, 12, '2018-05-21 21:04:54.155'),
  (34, 13, 13, '2018-05-21 21:04:54.155'),
  (35, 12, 13, '2018-05-21 21:04:54.155'),
  (36, 11, 13, '2018-05-21 21:04:54.155'),
  (37, 1, 13, '2018-05-21 21:04:54.155');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'department-administrator', 'department-administrator', 'department-administrator@prism.hr', 'd......................r@prism.hr', '2018-05-21 21:04:54.155'),
  (2, UUID(), 'department-author', 'department-author', 'department-author@prism.hr', 'd...............r@prism.hr', '2018-05-21 21:04:54.155'),
  (3, UUID(), 'department-member-pending', 'department-member-pending', 'department-member-pending@prism.hr', 'd.......................g@prism.hr', '2018-05-21 21:04:54.155'),
  (4, UUID(), 'department-member-accepted', 'department-member-accepted', 'department-member-accepted@prism.hr', 'd........................d@prism.hr', '2018-05-21 21:04:54.155'),
  (5, UUID(), 'department-member-rejected', 'department-member-rejected', 'department-member-rejected@prism.hr', 'd........................d@prism.hr', '2018-05-21 21:04:54.155'),
  (6, UUID(), 'department-draft-administrator', 'department-draft-administrator', 'department-draft-administrator@prism.hr', 'd............................r@prism.hr', '2018-05-21 21:04:54.155'),
  (7, UUID(), 'department-draft-author', 'department-draft-author', 'department-draft-author@prism.hr', 'd.....................r@prism.hr', '2018-05-21 21:04:54.155'),
  (8, UUID(), 'department-draft-member-pending', 'department-draft-member-pending', 'department-draft-member-pending@prism.hr', 'd.............................g@prism.hr', '2018-05-21 21:04:54.155'),
  (9, UUID(), 'department-draft-member-accepted', 'department-draft-member-accepted', 'department-draft-member-accepted@prism.hr', 'd..............................d@prism.hr', '2018-05-21 21:04:54.155'),
  (10, UUID(), 'department-draft-member-rejected', 'department-draft-member-rejected', 'department-draft-member-rejected@prism.hr', 'd..............................d@prism.hr', '2018-05-21 21:04:54.155'),
  (11, UUID(), 'department-draft-post-administrator', 'department-draft-post-administrator', 'department-draft-post-administrator@prism.hr', 'd.................................r@prism.hr', '2018-05-21 21:04:54.155'),
  (12, UUID(), 'department-pending-administrator', 'department-pending-administrator', 'department-pending-administrator@prism.hr', 'd..............................r@prism.hr', '2018-05-21 21:04:54.155'),
  (13, UUID(), 'department-pending-author', 'department-pending-author', 'department-pending-author@prism.hr', 'd.......................r@prism.hr', '2018-05-21 21:04:54.155'),
  (14, UUID(), 'department-pending-member-pending', 'department-pending-member-pending', 'department-pending-member-pending@prism.hr', 'd...............................g@prism.hr', '2018-05-21 21:04:54.155'),
  (15, UUID(), 'department-pending-member-accepted', 'department-pending-member-accepted', 'department-pending-member-accepted@prism.hr', 'd................................d@prism.hr', '2018-05-21 21:04:54.155'),
  (16, UUID(), 'department-pending-member-rejected', 'department-pending-member-rejected', 'department-pending-member-rejected@prism.hr', 'd................................d@prism.hr', '2018-05-21 21:04:54.155'),
  (17, UUID(), 'department-pending-post-administrator', 'department-pending-post-administrator', 'department-pending-post-administrator@prism.hr', 'd...................................r@prism.hr', '2018-05-21 21:04:54.155'),
  (18, UUID(), 'department-accepted-administrator', 'department-accepted-administrator', 'department-accepted-administrator@prism.hr', 'd...............................r@prism.hr', '2018-05-21 21:04:54.155'),
  (19, UUID(), 'department-accepted-author', 'department-accepted-author', 'department-accepted-author@prism.hr', 'd........................r@prism.hr', '2018-05-21 21:04:54.155'),
  (20, UUID(), 'department-accepted-member-pending', 'department-accepted-member-pending', 'department-accepted-member-pending@prism.hr', 'd................................g@prism.hr', '2018-05-21 21:04:54.155'),
  (21, UUID(), 'department-accepted-member-accepted', 'department-accepted-member-accepted', 'department-accepted-member-accepted@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.155'),
  (22, UUID(), 'department-accepted-member-rejected', 'department-accepted-member-rejected', 'department-accepted-member-rejected@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.155'),
  (23, UUID(), 'department-accepted-post-administrator', 'department-accepted-post-administrator', 'department-accepted-post-administrator@prism.hr', 'd....................................r@prism.hr', '2018-05-21 21:04:54.155'),
  (24, UUID(), 'department-rejected-administrator', 'department-rejected-administrator', 'department-rejected-administrator@prism.hr', 'd...............................r@prism.hr', '2018-05-21 21:04:54.155'),
  (25, UUID(), 'department-rejected-author', 'department-rejected-author', 'department-rejected-author@prism.hr', 'd........................r@prism.hr', '2018-05-21 21:04:54.155'),
  (26, UUID(), 'department-rejected-member-pending', 'department-rejected-member-pending', 'department-rejected-member-pending@prism.hr', 'd................................g@prism.hr', '2018-05-21 21:04:54.155'),
  (27, UUID(), 'department-rejected-member-accepted', 'department-rejected-member-accepted', 'department-rejected-member-accepted@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.155'),
  (28, UUID(), 'department-rejected-member-rejected', 'department-rejected-member-rejected', 'department-rejected-member-rejected@prism.hr', 'd.................................d@prism.hr', '2018-05-21 21:04:54.155'),
  (29, UUID(), 'department-rejected-post-administrator', 'department-rejected-post-administrator', 'department-rejected-post-administrator@prism.hr', 'd....................................r@prism.hr', '2018-05-21 21:04:54.155'),
  (30, UUID(), 'no-roles', 'no-roles', 'no-roles@prism.hr', 'n......s@prism.hr', '2018-05-21 21:04:54.155');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (2, UUID(), 2, 2, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (3, UUID(), 2, 3, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (4, UUID(), 2, 4, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (5, UUID(), 2, 5, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (6, UUID(), 2, 6, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (7, UUID(), 2, 7, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (8, UUID(), 2, 8, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (9, UUID(), 2, 9, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (10, UUID(), 2, 10, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (11, UUID(), 4, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (12, UUID(), 5, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (13, UUID(), 5, 2, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (14, UUID(), 5, 3, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (15, UUID(), 5, 4, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (16, UUID(), 5, 5, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (17, UUID(), 5, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (18, UUID(), 5, 13, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (19, UUID(), 5, 14, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (20, UUID(), 5, 15, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (21, UUID(), 5, 16, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (22, UUID(), 7, 17, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (23, UUID(), 8, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (24, UUID(), 8, 2, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (25, UUID(), 8, 3, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (26, UUID(), 8, 4, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (27, UUID(), 8, 5, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (28, UUID(), 8, 18, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (29, UUID(), 8, 19, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (30, UUID(), 8, 20, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (31, UUID(), 8, 21, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (32, UUID(), 8, 22, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (33, UUID(), 10, 23, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (34, UUID(), 11, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (35, UUID(), 11, 2, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (36, UUID(), 11, 3, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (37, UUID(), 11, 4, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (38, UUID(), 11, 5, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (39, UUID(), 11, 24, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (40, UUID(), 11, 25, 'AUTHOR', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (41, UUID(), 11, 26, 'MEMBER', 'PENDING', '2018-05-21 21:04:54.155'),
  (42, UUID(), 11, 27, 'MEMBER', 'ACCEPTED', '2018-05-21 21:04:54.155'),
  (43, UUID(), 11, 28, 'MEMBER', 'REJECTED', '2018-05-21 21:04:54.155'),
  (44, UUID(), 13, 29, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.155');

INSERT INTO document(id, cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES
  (1, 'cloudinary id', 'cloudinary url', 'file name', NOW());

UPDATE resource
SET document_logo_id = 1
WHERE id = 1;
