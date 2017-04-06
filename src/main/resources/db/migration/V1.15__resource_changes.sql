ALTER TABLE resource
  MODIFY COLUMN existing_relation VARCHAR(20),
  ADD COLUMN existing_relation_explanation TEXT
  AFTER existing_relation,
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
  MODIFY COLUMN parent_id BIGINT UNSIGNED NOT NULL,
  MODIFY COLUMN state VARCHAR(20),
  MODIFY COLUMN previous_state VARCHAR(20);

# We need to be able to change board categories without deleting post categories
INSERT INTO resource_category (resource_id, name, type, active, created_timestamp, updated_timestamp)
  SELECT
    post_category.post_id,
    reference_category.name,
    reference_category.type,
    1,
    reference_category.created_timestamp,
    reference_category.updated_timestamp
  FROM post_category
    INNER JOIN resource_category AS reference_category
      ON post_category.category_id = reference_category.id;

DROP TABLE post_category;

ALTER TABLE resource
  MODIFY COLUMN parent_id BIGINT UNSIGNED;
