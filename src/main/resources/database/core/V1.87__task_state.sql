ALTER TABLE resource_task
  ADD COLUMN completed INT(1) UNSIGNED
  AFTER task;
