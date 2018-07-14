INSERT INTO resource (id, scope, parent_id, name, apply_email, state, created_timestamp)
VALUES
  (1, 'DEPARTMENT', null, 'department1', null, 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (2, 'BOARD', 1, 'board1', null, 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (3, 'POST', 2, 'post1', 'apply@prism.hr', 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)
VALUES
  (1, 1, 1, '2018-05-21 21:04:54.185'),
  (2, 1, 2, '2018-05-21 21:04:54.185'),
  (3, 1, 3, '2018-05-21 21:04:54.185'),
  (5, 2, 2, '2018-05-21 21:04:54.185'),
  (6, 2, 3, '2018-05-21 21:04:54.185'),
  (8, 3, 3, '2018-05-21 21:04:54.185');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'alastair', 'alastair', 'alastair@prism.hr', 'alastair@prism.hr', '2018-05-21 21:04:54.185'),
  (2, UUID(), 'jakub', 'jakub', 'jakub@prism.hr', 'jakub@prism.hr', '2018-05-21 21:04:54.185');

INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)
VALUES
  (1, UUID(), 1, 1, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (2, UUID(), 1, 2, 'ADMINISTRATOR', 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO resource_event (id, resource_id, event, user_id, index_data, created_timestamp)
VALUES
  (1, 3, 'RESPONSE', 1, 'F540 019 024 L535 U533 K523 U536 S335 C513 S520 2018', '2018-05-21 21:04:54.185');

INSERT INTO activity (id, resource_id, resource_event_id, activity, filter_by_category, created_timestamp)
VALUES
  (1, 3, 1, 'RESPOND_POST_ACTIVITY', 0, '2018-05-21 21:04:54.185');

INSERT INTO activity_event (id, activity_id, user_id, event, created_timestamp)
VALUES
  (1, 1, 2, 'VIEW', '2018-05-21 21:04:54.185');
