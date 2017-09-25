ALTER TABLE resource
  CHANGE COLUMN member_count_pending member_to_be_uploaded_count BIGINT UNSIGNED;

INSERT INTO resource (scope, state, previous_state, name, handle, index_data, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University College London', 'ucl', SOUNDEX('ucl'), NOW(), NOW());

SET @ucl = (
  SELECT LAST_INSERT_ID());

UPDATE resource
SET resource.parent_id = @ucl,
  resource.quarter     = QUARTER(resource.created_timestamp)
WHERE resource.id = @ucl;

UPDATE resource
SET resource.parent_id = @ucl
WHERE resource.scope = 'DEPARTMENT';

UPDATE resource
SET handle = CONCAT('ucl/', resource.handle)
WHERE resource.id <> @ucl
      AND resource.handle IS NOT NULL;

INSERT INTO resource_relation (resource_id1, resource_id2, created_timestamp, updated_timestamp)
  SELECT
    @ucl,
    resource.id,
    NOW(),
    NOW()
  FROM resource;
