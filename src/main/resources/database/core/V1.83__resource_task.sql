CREATE TABLE resource_task (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id        BIGINT UNSIGNED NOT NULL,
  task               VARCHAR(50)     NOT NULL,
  notified_count     INT(1) UNSIGNED,
  created_timestamp  DATETIME        NOT NULL,
  updated_timestamp  DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource_id, task),
  INDEX (notified_count),
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

ALTER TABLE resource
  ADD COLUMN board_type VARCHAR(50)
  AFTER handle,
  ADD COLUMN internal INT(1) UNSIGNED
  AFTER board_type,
  ADD COLUMN last_member_timestamp DATETIME
  AFTER member_to_be_uploaded_count,
  ADD COLUMN last_task_creation_timestamp DATETIME
  AFTER last_member_timestamp,
  ADD COLUMN last_internal_post_timestamp DATETIME
  AFTER last_task_creation_timestamp,
  ADD INDEX (board_type),
  ADD INDEX (last_member_timestamp),
  ADD INDEX (last_task_creation_timestamp),
  ADD INDEX (last_internal_post_timestamp);
