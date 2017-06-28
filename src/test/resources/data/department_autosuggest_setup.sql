INSERT INTO resource(scope, state, name, created_timestamp)
VALUES('DEPARTMENT', 'ACCEPTED', 'Computer Science Department', NOW()),
  ('DEPARTMENT', 'ACCEPTED', 'Department of Computer Science', NOW()),
  ('DEPARTMENT', 'ACCEPTED', 'Laboratory for the Foundations of Computer Science', NOW()),
  ('DEPARTMENT', 'ACCEPTED', 'School of Informatics', NOW()),
  ('DEPARTMENT', 'REJECTED', 'Physics Department', NOW());

INSERT INTO document(cloudinary_id, cloudinary_url, file_name, created_timestamp)
  SELECT id, id, id, now()
  FROM resource;

UPDATE resource
INNER JOIN document
  ON resource.id = document.cloudinary_id
SET resource.document_logo_id = document.id;
