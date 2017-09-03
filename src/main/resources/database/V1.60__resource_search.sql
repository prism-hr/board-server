CREATE TABLE resource_search (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id       BIGINT UNSIGNED NOT NULL,
  search            VARCHAR(40)     NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource_id, search),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (resource_id) REFERENCES resource (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
