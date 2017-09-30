ALTER TABLE user
  ADD COLUMN email_display VARCHAR(255)
  AFTER email;

UPDATE user
SET email_display = email;

ALTER TABLE user
  MODIFY COLUMN email_display VARCHAR(255) NOT NULL;

ALTER TABLE post
  ADD COLUMN apply_email_display VARCHAR(255)
  AFTER apply_email;
