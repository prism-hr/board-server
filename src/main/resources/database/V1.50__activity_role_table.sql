DELETE
FROM activity_dismissal;

DELETE
FROM activity;

ALTER TABLE activity
  DROP FOREIGN KEY activity_ibfk_1,
  DROP INDEX resource_id,
  DROP COLUMN scope,
  DROP COLUMN role,
  ADD UNIQUE INDEX (resource_id, user_role_id, activity),
  ADD FOREIGN KEY (resource_id) REFERENCES resource (id);

CREATE TABLE activity_role (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_id BIGINT UNSIGNED NOT NULL,
  scope       VARCHAR(20)     NOT NULL,
  role        VARCHAR(20)     NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (activity_id, scope, role),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (activity_id) REFERENCES activity (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
