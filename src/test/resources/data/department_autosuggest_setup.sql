SET @universityId = (
  SELECT resource.id
  FROM resource
  WHERE resource.scope = 'UNIVERSITY');

INSERT INTO resource(parent_id, scope, state, name, created_timestamp)
VALUES(@universityId, 'DEPARTMENT', 'ACCEPTED', 'Computer Science Department', NOW()),
  (@universityId, 'DEPARTMENT', 'ACCEPTED', 'Department of Computer Science', NOW()),
  (@universityId, 'DEPARTMENT', 'ACCEPTED', 'Laboratory for the Foundations of Computer Science', NOW()),
  (@universityId, 'DEPARTMENT', 'ACCEPTED', 'School of Informatics', NOW()),
  (@universityId, 'DEPARTMENT', 'REJECTED', 'Physics Department', NOW());

INSERT INTO document(cloudinary_id, cloudinary_url, file_name, created_timestamp)
  SELECT id, id, id, now()
  FROM resource;

UPDATE resource
INNER JOIN document
  ON resource.id = document.cloudinary_id
SET resource.document_logo_id = document.id;
