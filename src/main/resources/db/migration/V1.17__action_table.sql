CREATE TABLE operation (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id BIGINT UNSIGNED NOT NULL,
  action      VARCHAR(20)     NOT NULL,
  user_id     BIGINT UNSIGNED,
  change_list TEXT,
  comment     TEXT,
  PRIMARY KEY (id),
  INDEX (resource_id, action),
  INDEX (user_id),
  FOREIGN KEY (resource_id) REFERENCES resource (id),
  FOREIGN KEY (user_id) REFERENCES user (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
