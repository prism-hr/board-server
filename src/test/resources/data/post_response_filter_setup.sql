INSERT INTO user (uuid, given_name, surname, email, email_display, password, created_timestamp)
VALUES (UUID(), 'alastair', 'knowles', 'alastair@knowles.com', 'a......r@knowles.com', SHA2('password', 256), NOW()),
  (UUID(), 'jakub', 'fibinger', 'jakub@fibinger.com', 'j...b@fibinger.com', SHA2('password', 256), NOW()),
  (UUID(), 'juan', 'mingo', 'juan@mingo.com', 'j..n@mingo.com', SHA2('password', 256), NOW());

INSERT INTO resource (scope, state, name, handle, summary, description, organization_name, apply_website, created_timestamp)
VALUES ('DEPARTMENT', 'ACCEPTED', 'Computer Science', 'cs', 'We specialize in machine learning, database theory and big data', NULL, NULL, NULL, NOW()),
  ('BOARD', 'ACCEPTED', 'Opportunities', 'cs/opportunities', 'Promote work and work experience opportunities to students', NULL, NULL, NULL, NOW()),
  ('POST', 'ACCEPTED', 'Database Engineer', 'cs/opportunities/4', 'Design schemas and optimize queries', 'You will be working primarily with SQL Server', NULL, NULL, NOW());

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
  WHERE resource.scope IN ('DEPARTMENT', 'BOARD', 'POST')
        AND user.email = 'alastair@knowles.com';

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
        AND user.email IN ('jakub@fibinger.com', 'juan@mingo.com');

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

UPDATE resource AS resource1
  INNER JOIN resource AS resource2
SET resource1.parent_id = resource2.id
WHERE resource1.scope = 'POST'
      AND resource2.scope = 'BOARD';

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
    resource1.id,
    resource2.id,
    NOW()
  FROM resource AS resource1
    INNER JOIN resource AS resource2
  WHERE resource1.scope = 'BOARD'
        AND resource2.scope = 'POST';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
  SELECT
    resource.id,
    resource.id,
    NOW()
  FROM resource
  WHERE resource.scope IN ('BOARD', 'POST');

INSERT INTO document (cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES ('jakub', 'jakub/jakub', 'jakub', NOW()),
  ('juan', 'juan/juan', 'juan', NOW());

INSERT INTO resource_event (resource_id, event, user_id, document_resume_id, website_resume, covering_note, visible_to_administrator, created_timestamp)
  SELECT
    resource.id,
    'RESPONSE',
    user.id,
    document.id,
    'http://www.jakub.com',
    'jakub',
    1,
    NOW()
  FROM resource
    INNER JOIN user
    INNER JOIN document
  WHERE resource.scope = 'POST'
        AND user.given_name = 'jakub'
        AND document.cloudinary_id = 'jakub';

INSERT INTO resource_event (resource_id, event, user_id, document_resume_id, website_resume, covering_note, visible_to_administrator, created_timestamp)
  SELECT
    resource.id,
    'RESPONSE',
    user.id,
    document.id,
    'http://www.juan.com',
    'juan',
    1,
    NOW()
  FROM resource
    INNER JOIN user
    INNER JOIN document
  WHERE resource.scope = 'POST'
        AND user.given_name = 'juan'
        AND document.cloudinary_id = 'juan';

INSERT INTO activity (resource_id, resource_event_id, activity, filter_by_category, created_timestamp)
  SELECT
    resource_event.resource_id,
    resource_event.id,
    'RESPOND_POST_ACTIVITY',
    0,
    NOW()
  FROM resource_event;
