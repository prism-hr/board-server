ALTER TABLE resource
  ADD COLUMN state_change_timestamp DATETIME AFTER previous_state,
  ADD COLUMN notified_count INT(1) UNSIGNED AFTER dead_timestamp,
  ADD INDEX (state_change_timestamp),
  ADD INDEX (notified_count);
