CREATE TABLE post_category (
  category_id BIGINT UNSIGNED NOT NULL,
  post_id     BIGINT UNSIGNED NOT NULL,
  UNIQUE INDEX (category_id, post_id),
  FOREIGN KEY (category_id) REFERENCES category (id),
  FOREIGN KEY (post_id) REFERENCES resource (id)
)
  COLLATE = utf8_general_ci
  ENGINE = InnoDB;
