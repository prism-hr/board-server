ALTER TABLE resource
  MODIFY COLUMN index_data TEXT AFTER dead_timestamp,
  ADD COLUMN quarter VARCHAR(5)
  AFTER last_response_timestamp,
  ADD INDEX (quarter);

UPDATE resource
SET quarter = CONCAT(YEAR(created_timestamp), QUARTER(created_timestamp));
