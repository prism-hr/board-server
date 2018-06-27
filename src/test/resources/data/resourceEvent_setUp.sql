INSERT INTO resource (id, scope, name, state, created_timestamp)
VALUES
  (1, 'POST', 'post1', 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (2, 'POST', 'post2', 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'alastair', 'alastair', 'alastair@prism.hr', 'alastair@prism.hr', '2018-05-21 21:04:54.185');
