CREATE TABLE category (
  id                 BIGINT UNSIGNED         NOT NULL AUTO_INCREMENT,
  parent_resource_id BIGINT UNSIGNED         NOT NULL,
  name               VARCHAR(50)             NOT NULL,
  type               ENUM ('MEMBER', 'POST') NOT NULL,
  active             BIT                     NOT NULL,
  created_timestamp  DATETIME                NOT NULL,
  updated_timestamp  DATETIME,
  PRIMARY KEY (id),
  FOREIGN KEY (parent_resource_id) REFERENCES resource (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

ALTER TABLE resource
  DROP COLUMN category_list,
  MODIFY COLUMN handle VARCHAR(255);
