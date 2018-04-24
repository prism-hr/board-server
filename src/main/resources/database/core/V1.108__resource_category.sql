ALTER TABLE resource_category
  DROP FOREIGN KEY resource_category_ibfk_2,
  DROP INDEX resource_id,
  MODIFY COLUMN type VARCHAR(10) AFTER resource_id,
  ADD UNIQUE INDEX (resource_id, type, name),
  ADD FOREIGN KEY (resource_id) REFERENCES resource (id);
