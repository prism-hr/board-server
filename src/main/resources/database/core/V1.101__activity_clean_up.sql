ALTER TABLE resource
  ADD COLUMN state_change_timestamp DATETIME AFTER previous_state;

