CREATE TABLE activity (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id       BIGINT UNSIGNED NOT NULL,
  user_role_id      BIGINT UNSIGNED,
  role              VARCHAR(20) NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource_id, user_role_id, role),
  INDEX (user_role_id),
  FOREIGN KEY (resource_id) REFERENCES resource (id),
  FOREIGN KEY (user_role_id) REFERENCES user_role (id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

CREATE TABLE activity_dismissal (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_id       BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (activity_id, user_id),
  INDEX (user_id),
  FOREIGN KEY (activity_id) REFERENCES activity (id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
