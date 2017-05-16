ALTER TABLE user
  DROP INDEX stormpath_id,
  DROP COLUMN stormpath_id,
  MODIFY COLUMN email VARCHAR(255) NOT NULL,
  ADD COLUMN password VARCHAR(100)
  AFTER email,
  ADD COLUMN temporary_password VARCHAR(100)
  AFTER password,
  ADD COLUMN temporary_password_expiry_timestamp DATETIME
  AFTER temporary_password,
  ADD COLUMN oauth_provider VARCHAR(20)
  AFTER temporary_password_expiry_timestamp,
  ADD COLUMN oauth_account_id VARCHAR(255)
  AFTER oauth_provider,
  ADD UNIQUE INDEX (oauth_provider, oauth_account_id);
