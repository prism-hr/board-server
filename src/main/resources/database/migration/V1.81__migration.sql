UPDATE resource
  INNER JOIN (
               SELECT
                 resource.parent_id AS id,
                 count(resource.id) AS board_count
               FROM resource
               WHERE resource.scope = 'BOARD'
               GROUP BY resource.parent_id) AS board_count
    ON resource.id = board_count.id
SET resource.board_count = board_count.board_count;
