ALTER TABLE activity
  ADD COLUMN scope VARCHAR(20) NOT NULL after user_role_id,
  DROP INDEX resource_id,
  ADD UNIQUE INDEX (resource_id, user_role_id, scope, role);
