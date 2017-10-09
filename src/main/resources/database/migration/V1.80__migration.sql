UPDATE user
SET document_image_request_state = 'DISPLAY_FIRST'
WHERE document_image_id IS NULL;
