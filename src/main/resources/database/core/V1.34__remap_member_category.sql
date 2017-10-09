DELIMITER //

CREATE PROCEDURE remap_resource_category()
  BEGIN
    CREATE TABLE remap (
      id BIGINT UNSIGNED NOT NULL,
      PRIMARY KEY (id))
    ENGINE = MEMORY;

    INSERT INTO remap(id)
      SELECT resource_category.resource_id
      FROM resource_category
      INNER JOIN resource
        ON resource_category.resource_id = resource.id
      WHERE resource.scope = 'DEPARTMENT'
        AND resource_category.type = 'MEMBER'
      GROUP BY resource_category.resource_id;

    DELETE
    FROM resource_category
    WHERE resource_category.resource_id IN(
      SELECT remap.id
      FROM remap)
      AND resource_category.type = 'MEMBER';

    INSERT INTO resource_category(resource_id, name, type, ordinal, created_timestamp, updated_timestamp)
      SELECT remap.id, 'UNDERGRADUATE', 'MEMBER', 0, now(), now()
      FROM remap
        UNION
      SELECT remap.id, 'MASTER', 'MEMBER', 1, now(), now()
      FROM remap
        UNION
      SELECT remap.id, 'RESEARCH', 'MEMBER', 2, now(), now()
      FROM remap;

    DELETE
    FROM resource_category
    WHERE resource_id IN(
      SELECT resource.id
      FROM resource
      INNER JOIN resource_relation
        ON resource.id = resource_relation.resource2_id
      WHERE resource.scope = 'POST'
        AND resource_relation.resource1_id IN(
          SELECT remap.id
          FROM remap))
      AND resource_category.type = 'MEMBER';

    INSERT INTO resource_category(resource_id, name, type, ordinal, created_timestamp, updated_timestamp)
      SELECT resource.id, 'UNDERGRADUATE', 'MEMBER', 0, now(), now()
      FROM resource
      INNER JOIN resource_relation
        ON resource.id = resource_relation.resource2_id
      WHERE resource.scope = 'POST'
        AND resource_relation.resource1_id IN(
          SELECT remap.id
          FROM remap)
        UNION
      SELECT resource.id, 'MASTER', 'MEMBER', 1, now(), now()
      FROM resource
        INNER JOIN resource_relation
          ON resource.id = resource_relation.resource2_id
      WHERE resource.scope = 'POST'
            AND resource_relation.resource1_id IN(
        SELECT remap.id
        FROM remap);

    DELETE
    FROM remap;

    INSERT INTO remap(id)
      SELECT user_role_category.user_role_id
      FROM user_role_category
      GROUP BY user_role_category.user_role_id;

    DELETE
    FROM user_role_category;

    INSERT INTO user_role_category(user_role_id, name, ordinal, created_timestamp, updated_timestamp)
      SELECT remap.id, 'UNDERGRADUATE', 0, now(), now()
      FROM remap;

    DROP TABLE remap;
  END //

DELIMITER ;

CALL remap_resource_category();

DROP PROCEDURE remap_resource_category;
