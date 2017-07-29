ALTER TABLE USER
  CHANGE COLUMN temporary_password password_reset_uuid VARCHAR(40),
  CHANGE COLUMN temporary_password_expiry_timestamp password_reset_timestamp DATETIME;
