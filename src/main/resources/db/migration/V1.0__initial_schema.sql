CREATE TABLE user (
  id                BIGINT AUTO_INCREMENT,
  given_name        VARCHAR(30)  NOT NULL,
  surname           VARCHAR(40)  NOT NULL,
  email             VARCHAR(254) NOT NULL,
  created_timestamp DATETIME     NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (email),
  INDEX (created_timestamp),
  INDEX (updated_timestamp)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

CREATE TABLE document (
  id               BIGINT AUTO_INCREMENT,
  cloudinary_id    VARCHAR(30)  NOT NULL,
  cloudinary_url   VARCHAR(255) NOT NULL,
  file_name        VARCHAR(500) NOT NULL,
  created_timestamp DATETIME     NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

CREATE TABLE department (
  id                BIGINT AUTO_INCREMENT,
  user_id           BIGINT       NOT NULL,
  document_logo_id  BIGINT,
  name              VARCHAR(255) NOT NULL,
  created_timestamp DATETIME     NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  INDEX (name),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (document_logo_id) REFERENCES document (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

CREATE TABLE board (
  id                BIGINT AUTO_INCREMENT,
  user_id           BIGINT       NOT NULL,
  department_id     BIGINT       NOT NULL,
  name              VARCHAR(255) NOT NULL,
  purpose           TEXT         NOT NULL,
  created_timestamp DATETIME     NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  INDEX (name),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (department_id) REFERENCES department (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;
