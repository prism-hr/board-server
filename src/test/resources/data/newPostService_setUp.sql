SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM resource;
DELETE FROM resource_relation;
DELETE FROM resource_operation;
DELETE FROM user;
DELETE FROM user_role;
DELETE FROM resource_category;
DELETE FROM document;
DELETE FROM resource_task;
DELETE FROM organization;
DELETE FROM location;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO resource (id, scope, parent_id, name, handle, state, previous_state, index_data, quarter, created_timestamp)
VALUES
  (1, 'UNIVERSITY', 1, 'university', 'university', 'ACCEPTED', 'ACCEPTED', 'U516', '20182', '2018-05-19 08:06:47.084'),
  (2, 'DEPARTMENT', 1, 'department-accepted', 'university/department-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213', '20182', '2018-05-19 08:06:47.084'),
  (3, 'BOARD', 2, 'department-accepted-board-accepted', 'university/department-accepted/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213', '20182', '2018-05-19 08:06:47.084'),
  (4, 'POST', 3, 'department-accepted-board-accepted-post-draft', NULL, 'DRAFT', 'DRAFT', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 D613', '20182', '2018-05-19 08:06:47.084'),
  (5, 'POST', 3, 'department-accepted-board-accepted-post-pending', NULL, 'PENDING', 'PENDING', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 P535', '20182', '2018-05-19 08:06:47.084'),
  (6, 'POST', 3, 'department-accepted-board-accepted-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A213', '20182', '2018-05-19 08:06:47.084'),
  (7, 'POST', 3, 'department-accepted-board-accepted-post-expired', NULL, 'EXPIRED', 'EXPIRED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 E216', '20182', '2018-05-19 08:06:47.084'),
  (8, 'POST', 3, 'department-accepted-board-accepted-post-suspended', NULL, 'SUSPENDED', 'SUSPENDED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 S215', '20182', '2018-05-19 08:06:47.084'),
  (9, 'POST', 3, 'department-accepted-board-accepted-post-rejected', NULL, 'REJECTED', 'REJECTED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 R223', '20182', '2018-05-19 08:06:47.084'),
  (10, 'POST', 3, 'department-accepted-board-accepted-post-withdrawn', NULL, 'WITHDRAWN', 'WITHDRAWN', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 W365', '20182', '2018-05-19 08:06:47.084'),
  (11, 'POST', 3, 'department-accepted-board-accepted-post-archived', NULL, 'ARCHIVED', 'ARCHIVED', 'U516 D163 A213 D163 A213 B630 A213 D163 A213 B630 A213 P230 A621', '20182', '2018-05-19 08:06:47.084'),
  (12, 'BOARD', 2, 'department-accepted-board-rejected', 'university/department-accepted/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 A213 D163 A213 B630 R223', '20182', '2018-05-19 08:06:47.084'),
  (13, 'POST', 12, 'department-accepted-board-rejected-post-draft', NULL, 'DRAFT', 'DRAFT', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 D613', '20182', '2018-05-19 08:06:47.084'),
  (14, 'POST', 12, 'department-accepted-board-rejected-post-pending', NULL, 'PENDING', 'PENDING', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 P535', '20182', '2018-05-19 08:06:47.084'),
  (15, 'POST', 12, 'department-accepted-board-rejected-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 A213', '20182', '2018-05-19 08:06:47.084'),
  (16, 'POST', 12, 'department-accepted-board-rejected-post-expired', NULL, 'EXPIRED', 'EXPIRED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 E216', '20182', '2018-05-19 08:06:47.084'),
  (17, 'POST', 12, 'department-accepted-board-rejected-post-suspended', NULL, 'SUSPENDED', 'SUSPENDED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 S215', '20182', '2018-05-19 08:06:47.084'),
  (18, 'POST', 12, 'department-accepted-board-rejected-post-rejected', NULL, 'REJECTED', 'REJECTED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 R223', '20182', '2018-05-19 08:06:47.084'),
  (19, 'POST', 12, 'department-accepted-board-rejected-post-withdrawn', NULL, 'WITHDRAWN', 'WITHDRAWN', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 W365', '20182', '2018-05-19 08:06:47.084'),
  (20, 'POST', 12, 'department-accepted-board-rejected-post-archived', NULL, 'ARCHIVED', 'ARCHIVED', 'U516 D163 A213 D163 A213 B630 R223 D163 A213 B630 R223 P230 A621', '20182', '2018-05-19 08:06:47.084'),
  (21, 'DEPARTMENT', 1, 'department-rejected', 'university/department-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223', '20182', '2018-05-19 08:06:47.084'),
  (22, 'BOARD', 21, 'department-rejected-board-accepted', 'university/department-rejected/board-accepted', 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213', '20182', '2018-05-19 08:06:47.084'),
  (23, 'POST', 22, 'department-rejected-board-accepted-post-draft', NULL, 'DRAFT', 'DRAFT', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 D613', '20182', '2018-05-19 08:06:47.084'),
  (24, 'POST', 22, 'department-rejected-board-accepted-post-pending', NULL, 'PENDING', 'PENDING', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 P535', '20182', '2018-05-19 08:06:47.084'),
  (25, 'POST', 22, 'department-rejected-board-accepted-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A213', '20182', '2018-05-19 08:06:47.084'),
  (26, 'POST', 22, 'department-rejected-board-accepted-post-expired', NULL, 'EXPIRED', 'EXPIRED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 E216', '20182', '2018-05-19 08:06:47.084'),
  (27, 'POST', 22, 'department-rejected-board-accepted-post-suspended', NULL, 'SUSPENDED', 'SUSPENDED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 S215', '20182', '2018-05-19 08:06:47.084'),
  (28, 'POST', 22, 'department-rejected-board-accepted-post-rejected', NULL, 'REJECTED', 'REJECTED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 R223', '20182', '2018-05-19 08:06:47.084'),
  (29, 'POST', 22, 'department-rejected-board-accepted-post-withdrawn', NULL, 'WITHDRAWN', 'WITHDRAWN', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 W365', '20182', '2018-05-19 08:06:47.084'),
  (30, 'POST', 22, 'department-rejected-board-accepted-post-archived', NULL, 'ARCHIVED', 'ARCHIVED', 'U516 D163 R223 D163 R223 B630 A213 D163 R223 B630 A213 P230 A621', '20182', '2018-05-19 08:06:47.084'),
  (31, 'BOARD', 21, 'department-rejected-board-rejected', 'university/department-rejected/board-rejected', 'REJECTED', 'REJECTED', 'U516 D163 R223 D163 R223 B630 R223', '20182', '2018-05-19 08:06:47.084'),
  (32, 'POST', 31, 'department-rejected-board-rejected-post-draft', NULL, 'DRAFT', 'DRAFT', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 D613', '20182', '2018-05-19 08:06:47.084'),
  (33, 'POST', 31, 'department-rejected-board-rejected-post-pending', NULL, 'PENDING', 'PENDING', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 P535', '20182', '2018-05-19 08:06:47.084'),
  (34, 'POST', 31, 'department-rejected-board-rejected-post-accepted', NULL, 'ACCEPTED', 'ACCEPTED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 A213', '20182', '2018-05-19 08:06:47.084'),
  (35, 'POST', 31, 'department-rejected-board-rejected-post-expired', NULL, 'EXPIRED', 'EXPIRED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 E216', '20182', '2018-05-19 08:06:47.084'),
  (36, 'POST', 31, 'department-rejected-board-rejected-post-suspended', NULL, 'SUSPENDED', 'SUSPENDED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 S215', '20182', '2018-05-19 08:06:47.084'),
  (37, 'POST', 31, 'department-rejected-board-rejected-post-rejected', NULL, 'REJECTED', 'REJECTED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 R223', '20182', '2018-05-19 08:06:47.084'),
  (38, 'POST', 31, 'department-rejected-board-rejected-post-withdrawn', NULL, 'WITHDRAWN', 'WITHDRAWN', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 W365', '20182', '2018-05-19 08:06:47.084'),
  (39, 'POST', 31, 'department-rejected-board-rejected-post-archived', NULL, 'ARCHIVED', 'ARCHIVED', 'U516 D163 R223 D163 R223 B630 R223 D163 R223 B630 R223 P230 A621', '20182', '2018-05-19 08:06:47.084');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-19 08:06:47.084'),
  (2, 2, 2, '2018-05-19 08:06:47.084'),
  (3, 1, 2, '2018-05-19 08:06:47.084'),
  (4, 3, 3, '2018-05-19 08:06:47.084'),
  (5, 2, 3, '2018-05-19 08:06:47.084'),
  (6, 1, 3, '2018-05-19 08:06:47.084'),
  (7, 4, 4, '2018-05-19 08:06:47.084'),
  (8, 3, 4, '2018-05-19 08:06:47.084'),
  (9, 2, 4, '2018-05-19 08:06:47.084'),
  (10, 1, 4, '2018-05-19 08:06:47.084'),
  (11, 5, 5, '2018-05-19 08:06:47.084'),
  (12, 3, 5, '2018-05-19 08:06:47.084'),
  (13, 2, 5, '2018-05-19 08:06:47.084'),
  (14, 1, 5, '2018-05-19 08:06:47.084'),
  (15, 6, 6, '2018-05-19 08:06:47.084'),
  (16, 3, 6, '2018-05-19 08:06:47.084'),
  (17, 2, 6, '2018-05-19 08:06:47.084'),
  (18, 1, 6, '2018-05-19 08:06:47.084'),
  (19, 7, 7, '2018-05-19 08:06:47.084'),
  (20, 3, 7, '2018-05-19 08:06:47.084'),
  (21, 2, 7, '2018-05-19 08:06:47.084'),
  (22, 1, 7, '2018-05-19 08:06:47.084'),
  (23, 8, 8, '2018-05-19 08:06:47.084'),
  (24, 3, 8, '2018-05-19 08:06:47.084'),
  (25, 2, 8, '2018-05-19 08:06:47.084'),
  (26, 1, 8, '2018-05-19 08:06:47.084'),
  (27, 9, 9, '2018-05-19 08:06:47.084'),
  (28, 3, 9, '2018-05-19 08:06:47.084'),
  (29, 2, 9, '2018-05-19 08:06:47.084'),
  (30, 1, 9, '2018-05-19 08:06:47.084'),
  (31, 10, 10, '2018-05-19 08:06:47.084'),
  (32, 3, 10, '2018-05-19 08:06:47.084'),
  (33, 2, 10, '2018-05-19 08:06:47.084'),
  (34, 1, 10, '2018-05-19 08:06:47.084'),
  (35, 11, 11, '2018-05-19 08:06:47.084'),
  (36, 3, 11, '2018-05-19 08:06:47.084'),
  (37, 2, 11, '2018-05-19 08:06:47.084'),
  (38, 1, 11, '2018-05-19 08:06:47.084'),
  (39, 12, 12, '2018-05-19 08:06:47.084'),
  (40, 2, 12, '2018-05-19 08:06:47.084'),
  (41, 1, 12, '2018-05-19 08:06:47.084'),
  (42, 13, 13, '2018-05-19 08:06:47.084'),
  (43, 12, 13, '2018-05-19 08:06:47.084'),
  (44, 2, 13, '2018-05-19 08:06:47.084'),
  (45, 1, 13, '2018-05-19 08:06:47.084'),
  (46, 14, 14, '2018-05-19 08:06:47.084'),
  (47, 12, 14, '2018-05-19 08:06:47.084'),
  (48, 2, 14, '2018-05-19 08:06:47.084'),
  (49, 1, 14, '2018-05-19 08:06:47.084'),
  (50, 15, 15, '2018-05-19 08:06:47.084'),
  (51, 12, 15, '2018-05-19 08:06:47.084'),
  (52, 2, 15, '2018-05-19 08:06:47.084'),
  (53, 1, 15, '2018-05-19 08:06:47.084'),
  (54, 16, 16, '2018-05-19 08:06:47.084'),
  (55, 12, 16, '2018-05-19 08:06:47.084'),
  (56, 2, 16, '2018-05-19 08:06:47.084'),
  (57, 1, 16, '2018-05-19 08:06:47.084'),
  (58, 17, 17, '2018-05-19 08:06:47.084'),
  (59, 12, 17, '2018-05-19 08:06:47.084'),
  (60, 2, 17, '2018-05-19 08:06:47.084'),
  (61, 1, 17, '2018-05-19 08:06:47.084'),
  (62, 18, 18, '2018-05-19 08:06:47.084'),
  (63, 12, 18, '2018-05-19 08:06:47.084'),
  (64, 2, 18, '2018-05-19 08:06:47.084'),
  (65, 1, 18, '2018-05-19 08:06:47.084'),
  (66, 19, 19, '2018-05-19 08:06:47.084'),
  (67, 12, 19, '2018-05-19 08:06:47.084'),
  (68, 2, 19, '2018-05-19 08:06:47.084'),
  (69, 1, 19, '2018-05-19 08:06:47.084'),
  (70, 20, 20, '2018-05-19 08:06:47.084'),
  (71, 12, 20, '2018-05-19 08:06:47.084'),
  (72, 2, 20, '2018-05-19 08:06:47.084'),
  (73, 1, 20, '2018-05-19 08:06:47.084'),
  (74, 21, 21, '2018-05-19 08:06:47.084'),
  (75, 1, 21, '2018-05-19 08:06:47.084'),
  (76, 22, 22, '2018-05-19 08:06:47.084'),
  (77, 21, 22, '2018-05-19 08:06:47.084'),
  (78, 1, 22, '2018-05-19 08:06:47.084'),
  (79, 23, 23, '2018-05-19 08:06:47.084'),
  (80, 22, 23, '2018-05-19 08:06:47.084'),
  (81, 21, 23, '2018-05-19 08:06:47.084'),
  (82, 1, 23, '2018-05-19 08:06:47.084'),
  (83, 24, 24, '2018-05-19 08:06:47.084'),
  (84, 22, 24, '2018-05-19 08:06:47.084'),
  (85, 21, 24, '2018-05-19 08:06:47.084'),
  (86, 1, 24, '2018-05-19 08:06:47.084'),
  (87, 25, 25, '2018-05-19 08:06:47.084'),
  (88, 22, 25, '2018-05-19 08:06:47.084'),
  (89, 21, 25, '2018-05-19 08:06:47.084'),
  (90, 1, 25, '2018-05-19 08:06:47.084'),
  (91, 26, 26, '2018-05-19 08:06:47.084'),
  (92, 22, 26, '2018-05-19 08:06:47.084'),
  (93, 21, 26, '2018-05-19 08:06:47.084'),
  (94, 1, 26, '2018-05-19 08:06:47.084'),
  (95, 27, 27, '2018-05-19 08:06:47.084'),
  (96, 22, 27, '2018-05-19 08:06:47.084'),
  (97, 21, 27, '2018-05-19 08:06:47.084'),
  (98, 1, 27, '2018-05-19 08:06:47.084'),
  (99, 28, 28, '2018-05-19 08:06:47.084'),
  (100, 22, 28, '2018-05-19 08:06:47.084'),
  (101, 21, 28, '2018-05-19 08:06:47.084'),
  (102, 1, 28, '2018-05-19 08:06:47.084'),
  (103, 29, 29, '2018-05-19 08:06:47.084'),
  (104, 22, 29, '2018-05-19 08:06:47.084'),
  (105, 21, 29, '2018-05-19 08:06:47.084'),
  (106, 1, 29, '2018-05-19 08:06:47.084'),
  (107, 30, 30, '2018-05-19 08:06:47.084'),
  (108, 22, 30, '2018-05-19 08:06:47.084'),
  (109, 21, 30, '2018-05-19 08:06:47.084'),
  (110, 1, 30, '2018-05-19 08:06:47.084'),
  (111, 31, 31, '2018-05-19 08:06:47.084'),
  (112, 21, 31, '2018-05-19 08:06:47.084'),
  (113, 1, 31, '2018-05-19 08:06:47.084'),
  (114, 32, 32, '2018-05-19 08:06:47.084'),
  (115, 31, 32, '2018-05-19 08:06:47.084'),
  (116, 21, 32, '2018-05-19 08:06:47.084'),
  (117, 1, 32, '2018-05-19 08:06:47.084'),
  (118, 33, 33, '2018-05-19 08:06:47.084'),
  (119, 31, 33, '2018-05-19 08:06:47.084'),
  (120, 21, 33, '2018-05-19 08:06:47.084'),
  (121, 1, 33, '2018-05-19 08:06:47.084'),
  (122, 34, 34, '2018-05-19 08:06:47.084'),
  (123, 31, 34, '2018-05-19 08:06:47.084'),
  (124, 21, 34, '2018-05-19 08:06:47.084'),
  (125, 1, 34, '2018-05-19 08:06:47.084'),
  (126, 35, 35, '2018-05-19 08:06:47.084'),
  (127, 31, 35, '2018-05-19 08:06:47.084'),
  (128, 21, 35, '2018-05-19 08:06:47.084'),
  (129, 1, 35, '2018-05-19 08:06:47.084'),
  (130, 36, 36, '2018-05-19 08:06:47.084'),
  (131, 31, 36, '2018-05-19 08:06:47.084'),
  (132, 21, 36, '2018-05-19 08:06:47.084'),
  (133, 1, 36, '2018-05-19 08:06:47.084'),
  (134, 37, 37, '2018-05-19 08:06:47.084'),
  (135, 31, 37, '2018-05-19 08:06:47.084'),
  (136, 21, 37, '2018-05-19 08:06:47.084'),
  (137, 1, 37, '2018-05-19 08:06:47.084'),
  (138, 38, 38, '2018-05-19 08:06:47.084'),
  (139, 31, 38, '2018-05-19 08:06:47.084'),
  (140, 21, 38, '2018-05-19 08:06:47.084'),
  (141, 1, 38, '2018-05-19 08:06:47.084'),
  (142, 39, 39, '2018-05-19 08:06:47.084'),
  (143, 31, 39, '2018-05-19 08:06:47.084'),
  (144, 21, 39, '2018-05-19 08:06:47.084'),
  (145, 1, 39, '2018-05-19 08:06:47.084');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'department-administrator', 'department-administrator', 'department-administrator@prism.hr', 'd......................r@prism.hr', '2018-05-19 08:06:47.084'),
  (2, UUID(), 'other-department-administrator', 'other-department-administrator', 'other-department-administrator@prism.hr', 'o............................r@prism.hr', '2018-05-19 08:06:47.084'),
  (3, UUID(), 'department-author', 'department-author', 'department-author@prism.hr', 'd...............r@prism.hr', '2018-05-19 08:06:47.084'),
  (4, UUID(), 'other-department-author', 'other-department-author', 'other-department-author@prism.hr', 'o.....................r@prism.hr', '2018-05-19 08:06:47.084'),
  (5, UUID(), 'accepted-department-member', 'accepted-department-member', 'accepted-department-member@prism.hr', 'a........................r@prism.hr', '2018-05-19 08:06:47.084'),
  (6, UUID(), 'other-accepted-department-member', 'other-accepted-department-member', 'other-accepted-department-member@prism.hr', 'o..............................r@prism.hr', '2018-05-19 08:06:47.084'),
  (7, UUID(), 'pending-department-member', 'pending-department-member', 'pending-department-member@prism.hr', 'p.......................r@prism.hr', '2018-05-19 08:06:47.084'),
  (8, UUID(), 'other-pending-department-member', 'other-pending-department-member', 'other-pending-department-member@prism.hr', 'o.............................r@prism.hr', '2018-05-19 08:06:47.084'),
  (9, UUID(), 'rejected-department-member', 'rejected-department-member', 'rejected-department-member@prism.hr', 'r........................r@prism.hr', '2018-05-19 08:06:47.084'),
  (10, UUID(), 'other-rejected-department-member', 'other-rejected-department-member', 'other-rejected-department-member@prism.hr', 'o..............................r@prism.hr', '2018-05-19 08:06:47.084'),
  (11, UUID(), 'post-administrator', 'post-administrator', 'post-administrator@prism.hr', 'p................r@prism.hr', '2018-05-19 08:06:47.084'),
  (12, UUID(), 'other-post-administrator', 'other-post-administrator', 'other-post-administrator@prism.hr', 'o......................r@prism.hr', '2018-05-19 08:06:47.084'),
  (13, UUID(), 'unprivileged', 'unprivileged', 'unprivileged@prism.hr', 'u..........d@prism.hr', '2018-05-19 08:06:47.084');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 2, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (2, UUID(), 2, 3, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (3, UUID(), 2, 5, 'MEMBER', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (4, UUID(), 2, 7, 'MEMBER', 'PENDING', '2018-05-19 08:06:47.084'),
  (5, UUID(), 2, 9, 'MEMBER', 'REJECTED', '2018-05-19 08:06:47.084'),
  (6, UUID(), 4, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (7, UUID(), 5, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (8, UUID(), 6, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (9, UUID(), 7, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (10, UUID(), 8, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (11, UUID(), 9, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (12, UUID(), 10, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (13, UUID(), 11, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (14, UUID(), 13, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (15, UUID(), 14, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (16, UUID(), 15, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (17, UUID(), 16, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (18, UUID(), 17, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (19, UUID(), 18, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (20, UUID(), 19, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (21, UUID(), 20, 11, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (22, UUID(), 21, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (23, UUID(), 21, 3, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (24, UUID(), 21, 5, 'MEMBER', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (25, UUID(), 21, 7, 'MEMBER', 'PENDING', '2018-05-19 08:06:47.084'),
  (26, UUID(), 21, 9, 'MEMBER', 'REJECTED', '2018-05-19 08:06:47.084'),
  (27, UUID(), 21, 2, 'ADMINISTRATOR', 'REJECTED', '2018-05-19 08:06:47.084'),
  (28, UUID(), 21, 4, 'AUTHOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (29, UUID(), 21, 6, 'MEMBER', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (30, UUID(), 21, 8, 'MEMBER', 'PENDING', '2018-05-19 08:06:47.084'),
  (31, UUID(), 21, 10, 'MEMBER', 'REJECTED', '2018-05-19 08:06:47.084'),
  (32, UUID(), 23, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (33, UUID(), 24, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (34, UUID(), 25, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (35, UUID(), 26, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (36, UUID(), 27, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (37, UUID(), 28, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (38, UUID(), 29, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (39, UUID(), 30, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (40, UUID(), 32, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (41, UUID(), 33, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (42, UUID(), 34, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (43, UUID(), 35, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (44, UUID(), 36, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (45, UUID(), 37, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (46, UUID(), 38, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084'),
  (47, UUID(), 39, 12, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-19 08:06:47.084');