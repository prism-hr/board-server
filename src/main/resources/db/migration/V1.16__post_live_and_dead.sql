ALTER TABLE resource
  ADD COLUMN live_timestamp DATETIME
  AFTER apply_email,
  ADD COLUMN dead_timestamp DATETIME
  AFTER live_timestamp,
  ADD INDEX (live_timestamp, dead_timestamp);
