ALTER TABLE resource
  ADD COLUMN homepage VARCHAR(100)
  AFTER summary;

ALTER TABLE document
  MODIFY COLUMN cloudinary_id VARCHAR(50) NOT NULL;
