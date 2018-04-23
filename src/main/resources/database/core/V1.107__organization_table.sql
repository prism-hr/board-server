CREATE TABLE organization (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  logo VARCHAR(255),
  creator_id BIGINT UNSIGNED,
  created_timestamp DATETIME(3) NOT NULL,
  updated_timestamp DATETIME(3),
  PRIMARY KEY (id),
  UNIQUE INDEX (name),
  INDEX (creator_id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp))
  COLLATE = utf8_general_ci
  ENGINE = innodb;

INSERT INTO organization (name, logo, creator_id, created_timestamp)
  SELECT organization_name, organization_logo, creator_id, MIN(created_timestamp)
  FROM resource
  WHERE scope = 'POST'
  GROUP BY organization_name;

ALTER TABLE resource
  ADD COLUMN organization_id BIGINT UNSIGNED AFTER handle,
  ADD INDEX (organization_id),
  ADD FOREIGN KEY (organization_id) REFERENCES organization (id);

UPDATE resource
  INNER JOIN organization
    ON resource.organization_name = organization.name
SET resource.organization_id = organization.id;

ALTER TABLE resource
  DROP COLUMN organization_name,
  DROP COLUMN organization_logo;

ALTER TABLE user
  ADD COLUMN default_organization_id BIGINT UNSIGNED AFTER website_resume,
  ADD COLUMN default_location_id BIGINT UNSIGNED AFTER default_organization_id,
  ADD INDEX (default_organization_id),
  ADD INDEX (default_location_id),
  ADD FOREIGN KEY (default_organization_id) REFERENCES organization (id),
  ADD FOREIGN KEY (default_location_id) REFERENCES location (id);

UPDATE user
  INNER JOIN (
    SELECT user_role.user_id AS user_id,
      max(resource.id) AS resource_id
    FROM user_role
      INNER JOIN resource
        ON user_role.resource_id = resource.id
    WHERE user_role.role = 'ADMINISTRATOR'
      AND resource.scope = 'POST'
    GROUP BY user_id) latest_post
  ON user.id = latest_post.user_id
 INNER JOIN resource
  ON latest_post.resource_id = resource.id
SET user.default_organization_id = resource.organization_id,
  user.default_location_id = resource.location_id;
