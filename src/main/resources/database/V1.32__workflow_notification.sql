ALTER TABLE permission
  DROP COLUMN role2,
  MODIFY COLUMN notificationInstance TEXT;
