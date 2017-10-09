ALTER TABLE resource
  ADD COLUMN state VARCHAR(10) NOT NULL
  AFTER scope;

UPDATE resource
SET state = 'ACCEPTED'
