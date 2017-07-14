CREATE TABLE user_notification_suppression (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL,
  resource_id       BIGINT UNSIGNED NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (user_id, resource_id),
  INDEX (resource_id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (resource_id) REFERENCES resource (id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

ALTER TABLE user
  ADD COLUMN uuid VARCHAR(40);

UPDATE user
SET uuid = UUID();

ALTER TABLE user
  MODIFY COLUMN uuid VARCHAR(40) NOT NULL;
