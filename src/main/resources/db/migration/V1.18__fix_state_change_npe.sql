UPDATE resource
SET live_timestamp = now(),
  dead_timestamp   = now() + INTERVAL 1 MONTH;

ALTER TABLE resource
  MODIFY COLUMN live_timestamp DATETIME NOT NULL,
  MODIFY COLUMN dead_timestamp DATETIME NOT NULL;
