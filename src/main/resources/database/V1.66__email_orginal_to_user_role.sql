ALTER TABLE user_role
  ADD COLUMN email VARCHAR(255)
  AFTER user_id,
  ADD UNIQUE INDEX resource_id2 (resource_id, email, role);

UPDATE user_role
  INNER JOIN user
    ON user_role.user_id = user.id
SET user_role.email = user.email_original;

ALTER TABLE user
  DROP COLUMN email_original;
