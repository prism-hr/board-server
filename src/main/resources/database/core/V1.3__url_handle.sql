ALTER TABLE resource
  ADD COLUMN handle VARCHAR(15)
  AFTER document_logo_id,
  ADD UNIQUE INDEX handle (type, handle);

UPDATE resource
SET handle = CAST(id AS CHAR);

ALTER TABLE resource
  MODIFY COLUMN handle VARCHAR(15) NOT NULL;
