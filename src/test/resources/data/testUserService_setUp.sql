INSERT INTO user(id, uuid, given_name, surname, email, email_display, test_user, creator_id, created_timestamp)
VALUES (1, UUID(), 'user1', 'user1', 'user1@prism.hr', 'user1@prism.hr', 0, 1, NOW()),
  (2, UUID(), 'user2', 'user2', 'user2@prism.hr', 'user2@prism.hr', 1, 2, NOW());
