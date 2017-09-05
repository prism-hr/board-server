CREATE TEMPORARY TABLE user_role_retain (
  resource_id BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  role        VARCHAR(50)     NOT NULL,
  PRIMARY KEY (resource_id, user_id)
)
  COLLATE = utf8_general_ci
  ENGINE = MEMORY;

CREATE TEMPORARY TABLE user_role_delete (
  id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id)
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
FROM activity_event
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

INSERT INTO user_role_retain (resource_id, user_id, role)
  SELECT
    resource_id,
    user_id,
    role
  FROM user_role
  WHERE user_role.role = 'ADMINISTRATOR';

INSERT IGNORE INTO user_role_retain (resource_id, user_id, role)
  SELECT
    resource_id,
    user_id,
    role
  FROM user_role
  WHERE user_role.role = 'AUTHOR';

INSERT IGNORE INTO user_role_retain (resource_id, user_id, role)
  SELECT
    resource_id,
    user_id,
    role
  FROM user_role
  WHERE user_role.role = 'MEMBER';

INSERT INTO user_role_delete
  SELECT user_role.id
  FROM user_role
    LEFT JOIN user_role_retain
      ON user_role.resource_id = user_role_retain.resource_id
         AND user_role.user_id = user_role_retain.user_id
         AND user_role.role = user_role_retain.role
  WHERE user_role_retain.resource_id IS NULL;

DELETE
FROM activity_role
WHERE activity_id IN (
  SELECT id
  FROM activity
  WHERE activity.user_role_id IN (
    SELECT id
    FROM user_role_delete));

DELETE
FROM activity_event
WHERE activity_id IN (
  SELECT id
  FROM activity
  WHERE activity.user_role_id IN (
    SELECT id
    FROM user_role_delete));

DELETE
FROM activity
WHERE activity.user_role_id IN (
  SELECT id
  FROM user_role_delete);

DELETE
FROM user_role_category
WHERE user_role_category.user_role_id IN (
  SELECT id
  FROM user_role_delete);

DELETE
FROM user_role
WHERE id IN (
  SELECT id
  FROM user_role_delete);

COMMIT;

DROP TEMPORARY TABLE user_role_retain;
DROP TEMPORARY TABLE user_role_delete;
