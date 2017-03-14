CREATE TABLE resource (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  type              VARCHAR(20)     NOT NULL,
  name              VARCHAR(255)    NOT NULL,
  description       TEXT,
  document_logo_id  BIGINT,
  category_list     TEXT,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  old_id            BIGINT,
  PRIMARY KEY (id),
  INDEX name (type, name),
  INDEX (document_logo_id),
  INDEX created_timestamp (type, created_timestamp),
  INDEX updated_timestamp (type, updated_timestamp),
  FOREIGN KEY (document_logo_id) REFERENCES document (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

INSERT INTO resource (type, name, document_logo_id, category_list, created_timestamp, updated_timestamp, old_id)
  SELECT
    'DEPARTMENT',
    name,
    document_logo_id,
    member_categories,
    created_timestamp,
    updated_timestamp,
    id
  FROM department;

INSERT INTO resource (type, name, description, category_list, created_timestamp, updated_timestamp, old_id)
  SELECT
    'BOARD',
    name,
    purpose,
    post_categories,
    created_timestamp,
    updated_timestamp,
    id
  FROM board;

CREATE TABLE resource_relation (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource1_id      BIGINT UNSIGNED,
  resource2_id      BIGINT UNSIGNED,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource1_id, resource2_id),
  INDEX (resource2_id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (resource1_id) REFERENCES resource (id),
  FOREIGN KEY (resource2_id) REFERENCES resource (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp, updated_timestamp)
  SELECT
    id,
    id,
    created_timestamp,
    updated_timestamp
  FROM resource;

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp, updated_timestamp)
  SELECT
    resource.id,
    resource2.id,
    board.created_timestamp,
    board.updated_timestamp
  FROM resource
    INNER JOIN department
      ON resource.old_id = department.id
         AND resource.type = 'DEPARTMENT'
    INNER JOIN board
      ON department.id = board.department_id
    INNER JOIN resource AS resource2
      ON resource2.old_id = board.id
         AND resource2.type = 'BOARD';

ALTER TABLE board
  DROP FOREIGN KEY board_ibfk_1;

ALTER TABLE department
  DROP FOREIGN KEY department_ibfk_1;

ALTER TABLE user
  MODIFY COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT;

CREATE TABLE user_role (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id       BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  role              VARCHAR(20)     NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (resource_id, user_id, role),
  INDEX (user_id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (resource_id) REFERENCES resource (id),
  FOREIGN KEY (user_id) REFERENCES user (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

INSERT INTO user_role (resource_id, user_id, role, created_timestamp, updated_timestamp)
  SELECT
    resource.id,
    department.user_id,
    'ADMINISTRATOR',
    resource.created_timestamp,
    resource.updated_timestamp
  FROM resource
    INNER JOIN department
      ON resource.old_id = department.id
         AND resource.type = 'DEPARTMENT';

INSERT INTO user_role (resource_id, user_id, role, created_timestamp, updated_timestamp)
  SELECT
    resource.id,
    board.user_id,
    'ADMINISTRATOR',
    resource.created_timestamp,
    resource.updated_timestamp
  FROM resource
    INNER JOIN board
      ON resource.old_id = board.id
         AND resource.type = 'BOARD';

ALTER TABLE resource
  DROP COLUMN old_id;

DROP TABLE board;

DROP TABLE department;
