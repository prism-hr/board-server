ALTER TABLE user_role
  ADD COLUMN uuid VARCHAR(40)
  AFTER id,
  ADD INDEX (uuid);

UPDATE user_role
SET uuid = UUID();

ALTER TABLE user_role
  MODIFY COLUMN uuid VARCHAR(40) NOT NULL;
