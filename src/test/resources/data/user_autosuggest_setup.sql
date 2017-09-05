INSERT INTO user(uuid, given_name, surname, email, email_original, created_timestamp)
VALUES(UUID(), 'alastair', 'knowles', 'alastair@knowles.com', 'alastair@knowles.com', NOW()),
  (UUID(), 'alastair', 'fibinger', 'alastair@fibinger.com', 'alastair@fibinger.com', NOW()),
  (UUID(), 'jakub', 'knowles', 'jakub@knowles.com', 'jakub@knowles.com', NOW()),
  (UUID(), 'juan', 'mingo', 'juan@mingo.com', 'juan@mingo.com', NOW()),
  (UUID(), 'alastair', 'knowles', 'alastair@knowles.net', 'alastair@knowles.net', NOW());

INSERT INTO document(cloudinary_id, cloudinary_url, file_name, created_timestamp)
  SELECT id, id, id, now()
  FROM user;

UPDATE user
  INNER JOIN document
    ON user.id = document.cloudinary_id
SET user.document_image_id = document.id;
