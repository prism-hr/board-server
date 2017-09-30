ALTER TABLE USER
  ADD COLUMN email_display VARCHAR(255) NOT NULL
  AFTER email;

ALTER TABLE POST
  ADD COLUMN apply_email_display VARCHAR(255)
  AFTER apply_email;
