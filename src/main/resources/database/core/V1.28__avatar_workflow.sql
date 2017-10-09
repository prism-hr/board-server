ALTER TABLE user
  ADD COLUMN document_image_request_state VARCHAR(20)
  AFTER document_image_id;
