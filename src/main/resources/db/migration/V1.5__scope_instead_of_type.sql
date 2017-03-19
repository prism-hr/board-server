ALTER TABLE resource
  CHANGE COLUMN type scope VARCHAR(20) NOT NULL,
  MODIFY COLUMN handle VARCHAR(255) NOT NULL;

UPDATE resource
  INNER JOIN resource_relation
    ON resource.id = resource_relation.resource1_id
  INNER JOIN resource AS child_resource
    ON resource_relation.resource2_id = child_resource.id
SET child_resource.handle = concat(resource.handle, "/", child_resource.handle);

ALTER TABLE resource
  DROP INDEX handle,
  ADD UNIQUE INDEX (handle);
