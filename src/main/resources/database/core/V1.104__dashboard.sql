ALTER TABLE resource
  DROP COLUMN board_count,
  DROP COLUMN author_count,
  ADD COLUMN post_count_all_time BIGINT(20) UNSIGNED AFTER post_count,
  ADD COLUMN member_count_all_time BIGINT(20) UNSIGNED AFTER member_count;

UPDATE resource
INNER JOIN (
  SELECT
    department.id AS department_id,
    COUNT(post.id) AS post_count
  FROM resource as department
  INNER JOIN resource_relation
    ON department.id = resource_relation.resource1_id
  INNER JOIN resource AS post
    ON resource_relation.resource2_id = post.id
  WHERE department.scope = 'DEPARTMENT'
    AND post.scope = 'POST'
  GROUP BY department.id) AS post_count
    ON resource.id = post_count.department_id
SET resource.post_count_all_time = post_count.post_count;
