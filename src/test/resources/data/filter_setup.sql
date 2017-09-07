INSERT INTO user (uuid, given_name, surname, email, email_original, password, created_timetamp)
VALUES (UUID(), 'administrator', 'administrator', 'administrator@administrator.com', 'administrator@administrator.com', SHA2('password', 256), NOW()),
  (UUID(), 'author', 'author', 'author@author.com', 'author@author.com', SHA2('password', 256), NOW()),
  (UUID(), 'alastair', 'knowles', 'alastair@knowles.com', 'alastair@knowles.com', SHA2('password', 256), NOW()),
  (UUID(), 'jakub', 'fibinger', 'jakub@fibinger.com', 'jakub@fibinger.com', SHA2('password', 256), NOW()),
  (UUID(), 'juan', 'mingo', 'juan@mingo.com', 'juan@mingo.com', SHA2('password', 256), NOW());

INSERT INTO resource (scope, state, name, summary, description, organization_name, apply_website, created_timestamp)
VALUES ('DEPARTMENT', 'ACCEPTED', 'Computer Science', 'We specialize in machine learning, database theory and big data', NULL, NULL, NULL, NOW()),
  ('BOARD', 'DRAFT', 'Games', 'Games for students to play', NULL, NULL, NULL, NOW()),
  ('BOARD', 'ACCEPTED', 'Opportunities', 'Promote work and work experience opportunities to students', NULL, NULL, NULL, NOW()),
  ('BOARD', 'REJECTED', 'Housing', 'Meet students to share houses with', NULL, NULL, NULL, NOW()),
  ('POST', 'DRAFT', 'Support Engineer', 'Help people to use their computers', 'This will be soul destroying', NULL, NULL, NOW()),
  ('POST', 'SUSPENDED', 'UX Designer', 'Design user-friendly software', 'You will get to analyze requirements and produce screen designs', NULL, NULL, NOW()),
  ('POST', 'PENDING', 'Front-End Developer', 'Writing responsive single page applications in AngularJs',
   'You will spend a lot of time fiddling around with stuff that never works very well', NULL, NULL, NOW()),
  ('POST', 'ACCEPTED', 'Database Engineer', 'Design schemas and optimize queries', 'You will be working primarily with SQL Server', NULL, NULL, NOW()),
  ('POST', 'ACCEPTED', 'Java Web Developer', 'Build fast, scalable backend services', 'You will be implementing business logic in Spring Boot microservices', NULL, NULL, NOW()),
  ('POST', 'ACCEPTED', 'Technical Analyst', 'Work out how to build things', 'Nobody will ever know what you actually do', NULL, NULL, NOW()),
  ('POST', 'EXPIRED', 'Scrum Leader', 'Whip software engineers into shape', 'You will be inculcating agile practice to ensure stuff gets done on time', NULL, NULL, NOW()),
  ('POST', 'REJECTED', 'Product Manager', 'Work with customers to design products', 'You will be annoying software engineers a lot', NULL, NULL, NOW()),
  ('POST', 'WITHDRAWN', 'Test Engineer', 'Anticipate and find bugs before release to production', 'You will be working primarily with Selenium', NULL, NULL, NOW()),
  ('POST', 'ARCHIVED', 'Software Architect', 'Play lego all day', 'You will work out how to put the big bits together', NULL, NULL, NOW());

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp)
  SELECT
    resource.id,
    user.id,
    'ADMINISTRATOR',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope IN ('DEPARTMENT', 'BOARD')
        AND user.email = 'administrator@administrator.com';

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp)
  SELECT
    resource.id,
    user.id,
    'MEMBER',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'DEPARTMENT'
        AND user.email = 'administrator@administrator.com';

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp)
  SELECT
    resource.id,
    user.id,
    'AUTHOR',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'BOARD'
        AND user.email = 'author@author.com';

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp)
  SELECT
    resource.id,
    user.id,
    'ADMINISTRATOR',
    'ACCEPTED',
    NOW()
  FROM resource
    INNER JOIN user
  WHERE resource.scope = 'POST'
        AND user.email = 'member@member.com';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
  SELECT
    resource1.id,
    resource2.id,
    NOW()
  FROM resource AS resource1
    INNER JOIN resource AS resource2
  WHERE resource1.scope = 'DEPARTMENT'
        AND resource2.scope = 'DEPARTMENT';

UPDATE resource AS resource1
  INNER JOIN resource AS resource2
SET resource1.parent_id = resource2.id
WHERE resource1.scope = 'DEPARTMENT'
      AND resource2.scope = 'DEPARTMENT';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
  SELECT
    resource1.id,
    resource2.id,
    NOW()
  FROM resource AS resource1
    INNER JOIN resource AS resource2
  WHERE resource1.scope IN ('DEPARTMENT', 'BOARD')
        AND resource2.scope = 'BOARD';

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
  WHERE (resource1.scope IN ('DEPARTMENT', 'POST')
         OR resource1.scope = 'BOARD' AND resource1.state = 'ACCEPTED')
        AND resource2.scope = 'POST';

INSERT INTO location (name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES ('London, United Kingdom', 'GB', 'code1', 1.00, 1.00, NOW()),
  ('Madrid, Spain', 'ES', 'code2', 2.00, 2.00, NOW()),
  ('Krakow, Poland', 'PL', 'code3', 3.00, 3.00, NOW());

UPDATE resource
  INNER JOIN location
SET resource.location_id = location.id
WHERE resource.state <> 'ACCEPTED'
      AND location.code = 'code1';
