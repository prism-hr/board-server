ALTER TABLE user_role
  ADD COLUMN expiry_date DATE
  AFTER role;

CREATE TABLE user_role_category (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_role_id      BIGINT UNSIGNED NOT NULL,
  name              VARCHAR(50)     NOT NULL,
  ordinal           INT             NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  FOREIGN KEY (user_role_id) REFERENCES user_role (id)
);
