ALTER TABLE resource
  MODIFY COLUMN state VARCHAR(20) NOT NULL;

CREATE TABLE permission (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource1_scope VARCHAR(20)     NOT NULL,
  resource1_state VARCHAR(20)     NOT NULL,
  role            VARCHAR(20)     NOT NULL,
  resource2_scope VARCHAR(20)     NOT NULL,
  resource2_state VARCHAR(20)     NOT NULL,
  action          VARCHAR(20)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource1_scope, resource1_state, role, resource2_scope, resource2_state, action),
  INDEX (resource2_scope, resource2_state)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
