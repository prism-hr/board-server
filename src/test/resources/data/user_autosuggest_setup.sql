INSERT INTO user (uuid, given_name, surname, email, email_display, created_timestamp)
VALUES (UUID(), 'alastair', 'knowles', 'alastair@knowles.com', 'a......r@knowles.com', NOW()),
  (UUID(), 'alastair', 'fibinger', 'alastair@fibinger.com', 'a......r@fibinger.com', NOW()),
  (UUID(), 'jakub', 'knowles', 'jakub@knowles.com', 'j...b@knowles.com', NOW()),
  (UUID(), 'juan', 'mingo', 'juan@mingo.com', 'j..n@mingo.com', NOW()),
  (UUID(), 'alastair', 'knowles', 'alastair@knowles.net', 'a......r@knowles.net', NOW());

INSERT INTO document (cloudinary_id, cloudinary_url, file_name, created_timestamp)
  SELECT
    id,
    id,
    id,
    now()
  FROM user;

UPDATE user
  INNER JOIN document
    ON user.id = document.cloudinary_id
SET user.document_image_id = document.id;
