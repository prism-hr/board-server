ALTER TABLE resource
  MODIFY COLUMN state VARCHAR(20) NOT NULL,
  ADD COLUMN previous_state VARCHAR(20)
  AFTER state,
  ADD INDEX (scope, state);

UPDATE resource
SET previous_state = state;

ALTER TABLE resource
  MODIFY COLUMN previous_state VARCHAR(20) NOT NULL;

CREATE TABLE permission (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource1_scope VARCHAR(20)     NOT NULL,
  role            VARCHAR(20)     NOT NULL,
  resource2_scope VARCHAR(20)     NOT NULL,
  resource2_state VARCHAR(20)     NOT NULL,
  action          VARCHAR(20)     NOT NULL,
  resource3_scope VARCHAR(20),
  resource3_state VARCHAR(20),
  PRIMARY KEY (id),
  UNIQUE INDEX (resource1_scope, role, resource2_scope, resource2_state, action, resource3_scope, resource3_state),
  INDEX (resource2_scope, resource2_state),
  INDEX (resource3_scope, resource3_state)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
