ALTER TABLE activity
  DROP FOREIGN KEY activity_ibfk_6,
  DROP INDEX resource_task_id,
  DROP COLUMN resource_task_id;
