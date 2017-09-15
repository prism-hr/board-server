INSERT INTO user (uuid, given_name, surname, email, password, created_timestamp)
VALUES (UUID(), 'alastair', 'knowles', 'alastair@knowles.com', SHA2('password', 256), NOW()),
  (UUID(), 'jakub', 'fibinger', 'jakub@fibinger.com', SHA2('password', 256), NOW()),
  (UUID(), 'juan', 'mingo', 'juan@mingo.com', SHA2('password', 256), NOW()),
  (UUID(), 'beatriz', 'rodriguez', 'beatriz@rodriguez.com', SHA2('password', 256), NOW()),
  (UUID(), 'felipe', 'ieder', 'felipe@ieder.com', SHA2('password', 256), NOW()),
  (UUID(), 'chris', 'neil', 'chris@neil.com', SHA2('password', 256), NOW()),
  (UUID(), 'andrew', 'marriott', 'andrew@marriott.com', SHA2('password', 256), NOW()),
  (UUID(), 'jon', 'wheatley', 'jon@wheatley.com', SHA2('password', 256), NOW()),
  (UUID(), 'toby', 'godfrey', 'toby@godfrey.com', SHA2('password', 256), NOW());

INSERT INTO resource (scope, state, name, handle, summary, created_timestamp)
VALUES ('DEPARTMENT', 'ACCEPTED', 'Computer Science', 'cs', 'We specialize in machine learning, database theory and big data', NOW()),
  ('BOARD', 'ACCEPTED', 'Opportunities', 'cs/opportunities', 'Promote work and work experience opportunities to students', NOW());

INSERT INTO user_role (uuid, resource_id, user_id, role, state, created_timestamp)
  SELECT
    UUID(),
    resource.id,
    user.id,
    'ADMINISTRATOR',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope IN ('DEPARTMENT', 'BOARD')
        AND user.email IN ('alastair@knowles.com', 'jakub@fibinger.com');

INSERT INTO user_role (uuid, resource_id, user_id, role, state, created_timestamp)
  SELECT
    UUID(),
    resource.id,
    user.id,
    'MEMBER',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'DEPARTMENT'
        AND user.email IN ('juan@mingo.com', 'beatriz@rodriguez.com');

INSERT INTO user_role (uuid, resource_id, user_id, role, state, created_timestamp)
  SELECT
    UUID(),
    resource.id,
    user.id,
    'MEMBER',
    'PENDING',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'DEPARTMENT'
        AND user.email IN ('felipe@ieder.com', 'chris@neil.com');

INSERT INTO user_role (uuid, resource_id, user_id, role, state, created_timestamp)
  SELECT
    UUID(),
    resource.id,
    user.id,
    'MEMBER',
    'REJECTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'DEPARTMENT'
        AND user.email = 'andrew@marriott.com';

INSERT INTO user_role (uuid, resource_id, user_id, role, state, created_timestamp)
  SELECT
    UUID(),
    resource.id,
    user.id,
    'AUTHOR',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'BOARD'
        AND user.email IN ('jon@wheatley.com', 'toby@godfrey.com');

UPDATE resource AS resource1
  INNER JOIN resource AS resource2
SET resource1.parent_id = resource2.id
WHERE resource1.scope = 'DEPARTMENT'
      AND resource2.scope = 'DEPARTMENT';

UPDATE resource AS resource1
  INNER JOIN resource AS resource2
SET resource1.parent_id = resource2.id
WHERE resource1.scope = 'BOARD'
      AND resource2.scope = 'DEPARTMENT';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
  SELECT
    resource1.id,
    resource2.id,
    NOW()
  FROM resource AS resource1
    INNER JOIN resource AS resource2
  WHERE resource1.scope = 'DEPARTMENT';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
  SELECT
    resource.id,
    resource.id,
    NOW()
  FROM resource
  WHERE resource.scope = 'BOARD';

INSERT INTO activity (resource_id, user_role_id, activity, filter_by_category, created_timestamp)
  SELECT
    user_role.resource_id,
    user_role.id,
    'JOIN_DEPARTMENT_REQUEST_ACTIVITY',
    0,
    NOW()
  FROM user_role
  WHERE user_role.state = 'PENDING';
