CREATE TEMPORARY TABLE max_user_role (
  resource_id BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  role        VARCHAR(50)     NOT NULL,
  PRIMARY KEY (resource_id, user_id)
)
  COLLATE = utf8_general_ci
  ENGINE = MEMORY;

START TRANSACTION;

DELETE
FROM activity_role
WHERE activity_id IN (
  SELECT id
  FROM activity
  WHERE activity.user_role_id IN (
    SELECT id
    FROM user_role
    WHERE user_role.state = 'REJECTED'));

DELETE
FROM activity_category
WHERE activity_id IN (
  SELECT id
  FROM activity
  WHERE activity.user_role_id IN (
    SELECT id
    FROM user_role
    WHERE user_role.state = 'REJECTED'));

DELETE
FROM activity
WHERE activity.user_role_id IN (
  SELECT id
  FROM user_role
  WHERE user_role.state = 'REJECTED');

DELETE
FROM user_role_category
WHERE user_role_category.user_role_id IN (
  SELECT id
  FROM user_role
  WHERE user_role.state = 'REJECTED');

DELETE
FROM user_role
WHERE user_role.state = 'REJECTED';

INSERT INTO max_user_role (resource_id, user_id, role)
  SELECT
    resource_id,
    user_id,
    role
  FROM user_role
  WHERE user_role.role = 'ADMINISTRATOR';

INSERT IGNORE INTO max_user_role (resource_id, user_id, role)
  SELECT
    resource_id,
    user_id,
    role
  FROM user_role
  WHERE user_role.role = 'AUTHOR';

INSERT IGNORE INTO max_user_role (resource_id, user_id, role)
  SELECT
    resource_id,
    user_id,
    role
  FROM user_role
  WHERE user_role.role = 'MEMBER';

DELETE
FROM activity_role
WHERE activity_id IN (
  SELECT id
  FROM activity
  WHERE activity.user_role_id IN (
    SELECT user_role.id
    FROM user_role
      LEFT JOIN max_user_role
        ON user_role.resource_id = max_user_role.resource_id
           AND user_role.user_id = max_user_role.user_id
           AND user_role.role = max_user_role.role
    WHERE max_user_role.id IS NULL));

DELETE
FROM activity_category
WHERE activity_id IN (
  SELECT id
  FROM activity
  WHERE activity.user_role_id IN (
    SELECT user_role.id
    FROM user_role
      LEFT JOIN max_user_role
        ON user_role.resource_id = max_user_role.resource_id
           AND user_role.user_id = max_user_role.user_id
           AND user_role.role = max_user_role.role
    WHERE max_user_role.id IS NULL));

DELETE
FROM activity
WHERE activity.user_role_id IN (
  SELECT user_role.id
  FROM user_role
    LEFT JOIN max_user_role
      ON user_role.resource_id = max_user_role.resource_id
         AND user_role.user_id = max_user_role.user_id
         AND user_role.role = max_user_role.role
  WHERE max_user_role.id IS NULL);

DELETE
FROM user_role_category
WHERE user_role_category.user_role_id IN (
  SELECT user_role.id
  FROM user_role
    LEFT JOIN max_user_role
      ON user_role.resource_id = max_user_role.resource_id
         AND user_role.user_id = max_user_role.user_id
         AND user_role.role = max_user_role.role
  WHERE max_user_role.id IS NULL);

DELETE
FROM user_role
WHERE id IN (
  SELECT user_role.id
  FROM user_role
    LEFT JOIN max_user_role
      ON user_role.resource_id = max_user_role.resource_id
         AND user_role.user_id = max_user_role.user_id
         AND user_role.role = max_user_role.role
  WHERE max_user_role.id IS NULL);

COMMIT;

DROP TEMPORARY TABLE max_user_role;
