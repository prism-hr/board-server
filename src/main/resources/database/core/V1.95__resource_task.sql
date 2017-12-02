ALTER TABLE user
  ADD COLUMN seen_walk_through INT(1) UNSIGNED
  AFTER document_image_request_state;
