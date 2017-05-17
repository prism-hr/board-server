UPDATE resource
SET live_timestamp = now(),
  dead_timestamp   = now() + INTERVAL 1 MONTH
WHERE scope = 'POST';
