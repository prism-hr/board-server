ALTER TABLE test_email
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER message,
  ADD INDEX (creator_id);
