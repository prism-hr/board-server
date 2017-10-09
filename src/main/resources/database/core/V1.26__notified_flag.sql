ALTER TABLE resource_operation
  ADD COLUMN notified_timestamp DATETIME
  AFTER comment,
  ADD INDEX (notified_timestamp);
