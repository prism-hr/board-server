TRUNCATE TABLE permission;

ALTER TABLE permission
  DROP INDEX resource2_scope,
  DROP INDEX resource3_scope,
  ADD INDEX (resource2_scope, resource2_state, action),
  MODIFY COLUMN resource3_scope VARCHAR(20) NOT NULL,
  MODIFY COLUMN resource3_state VARCHAR(20) NOT NULL;
