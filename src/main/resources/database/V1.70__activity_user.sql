CREATE TABLE activity_user (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_id       BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (activity_id, user_id),
  INDEX (user_id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (activity_id) REFERENCES activity (id),
  FOREIGN KEY (user_id) REFERENCES user (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
