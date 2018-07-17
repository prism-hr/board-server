INSERT INTO resource (id, scope, name, state, created_timestamp)
VALUES
  (1, 'POST', 'post1', 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'alastair', 'alastair', 'alastair@prism.hr', 'alastair@prism.hr', '2018-05-21 21:04:54.185');

INSERT INTO resource_event (id, resource_id, event, user_id, role, referral, created_timestamp)
VALUES
  (1, 1, 'REFERRAL', 1, 'MEMBER', 'referral', '2018-05-21 21:04:54.185');
