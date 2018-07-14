INSERT INTO resource (id, scope, parent_id, name, apply_email, state, created_timestamp)
VALUES
  (1, 'DEPARTMENT', null, 'department1', null, 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (2, 'BOARD', 1, 'board1', null, 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (3, 'POST', 2, 'post1', 'apply@prism.hr', 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (4, 'POST', 2, 'post2', null, 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-21 21:04:54.185'),
  (2, 1, 2, '2018-05-21 21:04:54.185'),
  (3, 1, 3, '2018-05-21 21:04:54.185'),
  (4, 1, 4, '2018-05-21 21:04:54.185'),
  (5, 2, 2, '2018-05-21 21:04:54.185'),
  (6, 2, 3, '2018-05-21 21:04:54.185'),
  (7, 2, 4, '2018-05-21 21:04:54.185'),
  (8, 3, 3, '2018-05-21 21:04:54.185'),
  (9, 4, 4, '2018-05-21 21:04:54.185');

INSERT INTO location (id, name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES
  (1, 'london', 'GB', 'googleId', 1.00, 1.00, '2018-05-21 21:04:54.185');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, gender, age_range, location_nationality_id, created_timestamp)
VALUES
  (1, UUID(), 'alastair', 'alastair', 'alastair@prism.hr', 'alastair@prism.hr', 'MALE', 'THIRTY_THIRTYNINE', 1, '2018-05-21 21:04:54.185'),
  (2, UUID(), 'jakub', 'fibinger', 'jakub@prism.hr', 'jakub@prism.hr', 'MALE', 'THIRTY_THIRTYNINE', 1, '2018-05-21 21:04:54.185');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, member_category, member_program, member_year, created_timestamp)
VALUES
  (1, UUID(), 1, 1, 'MEMBER', 'ACCEPTED', 'UNDERGRADUATE_STUDENT', 'memberProgram', 2018, '2018-05-21 21:04:54.185');

INSERT INTO resource_event (id, resource_id, event, user_id, referral, created_timestamp)
VALUES
  (1, 3, 'REFERRAL', 1, 'referral', '2018-05-21 21:04:54.185');
