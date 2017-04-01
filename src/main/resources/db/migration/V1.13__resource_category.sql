ALTER TABLE category
  DROP FOREIGN KEY category_ibfk_1,
  DROP INDEX parent_resource_id,
  CHANGE COLUMN parent_resource_id resource_id BIGINT UNSIGNED NOT NULL,
  ADD INDEX (resource_id),
  ADD FOREIGN KEY (resource_id) REFERENCES resource (id);

RENAME TABLE
    category TO resource_category;
