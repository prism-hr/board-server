ALTER TABLE resource_category
  DROP COLUMN ordinal;

ALTER TABLE activity
  ADD COLUMN resource_task_id BIGINT UNSIGNED
  AFTER resource_event_id,
  ADD INDEX (resource_task_id),
  ADD FOREIGN KEY (resource_task_id) REFERENCES resource_task (id);
