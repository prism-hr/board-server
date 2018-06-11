SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO organization (id, name, logo, creator_id, created_timestamp)
VALUES (1, 'organization1', 'logo1', 1, NOW()),
  (2, 'organization2', 'logo2', 2, NOW());

SET FOREIGN_KEY_CHECKS = 1;
