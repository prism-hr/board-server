ALTER TABLE resource
  ADD COLUMN customer_id VARCHAR(255)
  AFTER dead_timestamp,
  ADD UNIQUE INDEX (customer_id);
