ALTER TABLE resource
  ADD COLUMN existing_relation VARCHAR(20),
  ADD COLUMN parent_id BIGINT UNSIGNED
  AFTER id,
  ADD INDEX (parent_id),
  ADD FOREIGN KEY (parent_id) REFERENCES resource (id);

UPDATE resource
  INNER JOIN resource_relation
    ON resource.id = resource_relation.resource2_id
  INNER JOIN resource AS parent
    ON resource_relation.resource1_id = parent.id
SET resource.parent_id = parent.id
WHERE resource.scope = 'POST'
      AND parent.scope = 'BOARD';

UPDATE resource
  INNER JOIN resource_relation
    ON resource.id = resource_relation.resource2_id
  INNER JOIN resource AS parent
    ON resource_relation.resource1_id = parent.id
SET resource.parent_id = parent.id
WHERE resource.scope = 'BOARD'
      AND parent.scope = 'DEPARTMENT';

UPDATE resource
  INNER JOIN resource_relation
    ON resource.id = resource_relation.resource2_id
  INNER JOIN resource AS parent
    ON resource_relation.resource1_id = parent.id
SET resource.parent_id = parent.id
WHERE resource.scope = 'DEPARTMENT'
      AND parent.scope = 'DEPARTMENT';

ALTER TABLE resource
  MODIFY COLUMN parent_id BIGINT UNSIGNED NOT NULL;
