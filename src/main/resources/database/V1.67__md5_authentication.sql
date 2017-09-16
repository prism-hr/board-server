ALTER TABLE user
  ADD COLUMN password_hash VARCHAR(10)
  AFTER password;

UPDATE user
SET password_hash = 'SHA256'
WHERE password IS NOT NULL;
