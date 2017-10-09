TRUNCATE TABLE permission;

ALTER TABLE permission
  DROP INDEX resource2_scope,
  DROP INDEX resource3_scope,
  ADD INDEX (resource2_scope, resource2_state, action);
