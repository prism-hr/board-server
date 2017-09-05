ALTER TABLE user
  ADD COLUMN email_original VARCHAR(255)
  AFTER email,
  ADD UNIQUE INDEX (email_original);

UPDATE user
SET email_original = email;

ALTER TABLE user
  MODIFY COLUMN email_original VARCHAR(255) NOT NULL;

ALTER TABLE resource
  ADD COLUMN member_count_provisional BIGINT UNSIGNED
  AFTER member_count;
