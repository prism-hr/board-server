CREATE TABLE resource_task (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id        BIGINT UNSIGNED NOT NULL,
  task               VARCHAR(50)     NOT NULL,
  notified_count     INT(1) UNSIGNED,
  notified_timestamp DATETIME,
  created_timestamp  DATETIME        NOT NULL,
  updated_timestamp  DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource_id, task),
  INDEX (notified_timestamp),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (resource_id) REFERENCES resource (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

CREATE TABLE resource_task_suppression (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_task_id  BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource_task_id, user_id),
  INDEX (user_id),
  FOREIGN KEY (resource_task_id) REFERENCES resource_task (id),
  FOREIGN KEY (user_id) REFERENCES user (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
