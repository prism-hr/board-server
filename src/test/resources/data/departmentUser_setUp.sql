INSERT INTO resource (id, scope, parent_id, name, state, created_timestamp)
VALUES
  (1, 'DEPARTMENT', null, 'department1', 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (2, 'DEPARTMENT', null, 'department2', 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO resource_category (id, resource_id, type, name, created_timestamp)
VALUES
  (1, 1, 'MEMBER', 'UNDERGRADUATE_STUDENT', '2018-05-21 21:04:54.185');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-21 21:04:54.185');

INSERT INTO location (id, name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES
  (1, 'london', 'GB', 'googleId', 1.00, 1.00, '2018-05-21 21:04:54.185');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, gender, age_range, location_nationality_id, created_timestamp)
VALUES
  (1, UUID(), 'user1', 'user1', 'user1@prism.hr', 'user1@prism.hr', 'MALE', 'THIRTY_THIRTYNINE', 1, '2018-05-21 21:04:54.185'),
  (2, UUID(), 'user2', 'user2', 'user2@prism.hr', 'user2@prism.hr', null, null, null, '2018-05-21 21:04:54.185'),
  (3, UUID(), 'user3', 'user3', 'user3@prism.hr', 'user3@prism.hr', 'MALE', 'THIRTY_THIRTYNINE', 1, '2018-05-21 21:04:54.185'),
  (4, UUID(), 'user4', 'user4', 'user4@prism.hr', 'user4@prism.hr', null, null, null, '2018-05-21 21:04:54.185'),
  (5, UUID(), 'user5', 'user5', 'user5@prism.hr', 'user5@prism.hr', 'MALE', 'THIRTY_THIRTYNINE', 1, '2018-05-21 21:04:54.185'),
  (6, UUID(), 'user6', 'user6', 'user6@prism.hr', 'user6@prism.hr', null, null, null, '2018-05-21 21:04:54.185'),
  (7, UUID(), 'user7', 'user7', 'user7@prism.hr', 'user7@prism.hr', 'MALE', 'THIRTY_THIRTYNINE', 1, '2018-05-21 21:04:54.185'),
  (8, UUID(), 'user8', 'user8', 'user8@prism.hr', 'user8@prism.hr', null, null, null, '2018-05-21 21:04:54.185');

INSERT INTO user_role(id, uuid, resource_id, user_id, role, state, member_category, member_program, member_year, member_date, expiry_date, created_timestamp)
VALUES
  (1, UUID(), 1, 1, 'MEMBER', 'ACCEPTED', 'UNDERGRADUATE_STUDENT', 'memberProgram', 2018, CURRENT_DATE() + INTERVAL 1 DAY, '2020-06-01', '2018-05-21 21:04:54.185'),
  (2, UUID(), 1, 2, 'MEMBER', 'ACCEPTED', null, null, null, null, null, '2018-05-21 21:04:54.185'),
  (3, UUID(), 1, 3, 'MEMBER', 'ACCEPTED', null, null, null, null, null, '2018-05-21 21:04:54.185'),
  (4, UUID(), 1, 4, 'MEMBER', 'ACCEPTED', 'UNDERGRADUATE_STUDENT', 'memberProgram', 2018, CURRENT_DATE() + INTERVAL 1 DAY, '2020-06-01', '2018-05-21 21:04:54.185'),
  (5, UUID(), 1, 5, 'ADMINISTRATOR', 'ACCEPTED', null, null, null, null, null, '2018-05-21 21:04:54.185'),
  (6, UUID(), 1, 6, 'ADMINISTRATOR', 'ACCEPTED', null, null, null, null, null, '2018-05-21 21:04:54.185'),
  (7, UUID(), 1, 7, 'MEMBER', 'ACCEPTED', 'UNDERGRADUATE_STUDENT', 'memberProgram', 2018, CURRENT_DATE() - INTERVAL 1 YEAR, '2020-06-01', '2018-05-21 21:04:54.185'),
  (8, UUID(), 1, 8, 'MEMBER', 'ACCEPTED', 'UNDERGRADUATE_STUDENT', 'memberProgram', 2018, CURRENT_DATE() - INTERVAL 1 YEAR, '2020-06-01', '2018-05-21 21:04:54.185');
