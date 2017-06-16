DELIMITER //

CREATE PROCEDURE remap_resource_category()
  BEGIN
    CREATE TEMPORARY TABLE department_remap (
      id BIGINT UNSIGNED NOT NULL,
      PRIMARY KEY (id))
    ENGINE = MEMORY;

    INSERT INTO department_remap(id)
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
      SELECT id
      FROM department_remap);

    DROP TABLE department_remap;
  END //

DELIMITER ;

CALL remap_resource_category();

DROP PROCEDURE remap_resource_category;
