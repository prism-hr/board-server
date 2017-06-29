INSERT INTO user(given_name, surname, email, created_timestamp)
VALUES('alastair', 'knowles', 'alastair@knowles.com', NOW()),
  ('alastair', 'fibinger', 'alastair@fibinger.com', NOW()),
  ('jakub', 'knowles', 'jakub@knowles.com', NOW()),
  ('juan', 'mingo', 'juan@mingo.com', NOW()),
  ('alastair', 'knowles', 'alastair@knowles.net', NOW());

INSERT INTO document(cloudinary_id, cloudinary_url, file_name, created_timestamp)
  SELECT id, id, id, now()
  FROM user;

UPDATE user
  INNER JOIN document
    ON user.id = document.cloudinary_id
SET user.document_image_id = document.id;
