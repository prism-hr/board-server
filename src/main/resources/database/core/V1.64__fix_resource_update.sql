UPDATE resource
  INNER JOIN (
               SELECT
                 resource_operation.resource_id            AS resource_id,
                 MAX(resource_operation.created_timestamp) AS updated_timestamp
               FROM resource_operation
               GROUP BY resource_operation.resource_id) AS resource_update
    ON resource.id = resource_update.resource_id
SET resource.updated_timestamp = resource_update.updated_timestamp;
