ALTER TABLE user
  ADD COLUMN document_image_id BIGINT UNSIGNED
  AFTER stormpath_id,
  ADD FOREIGN KEY (document_image_id) REFERENCES document (id);
