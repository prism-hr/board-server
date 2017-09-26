INSERT INTO user (uuid, given_name, surname, email, password, created_timestamp)
VALUES (UUID(), 'department', 'administrator', 'department@administrator.com', SHA2('password', 256), NOW()),
  (UUID(), 'department', 'member', 'department@member.com', SHA2('password', 256), NOW()),
  (UUID(), 'board', 'administrator', 'board@administrator.com', SHA2('password', 256), NOW()),
  (UUID(), 'board', 'author', 'board@author.com', SHA2('password', 256), NOW()),
  (UUID(), 'post', 'administrator', 'post@administrator.com', SHA2('password', 256), NOW());

INSERT INTO resource (scope, state, name, handle, summary, description, organization_name, apply_website, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'University of Edinburgh', 'ed', NULL, NULL, NULL, NULL, NOW(), NULL),
  ('DEPARTMENT', 'ACCEPTED', 'Computer Science', 'ed/cs', 'We specialize in machine learning, database theory and big data', NULL, NULL, NULL, NOW(), NULL),
  ('BOARD', 'DRAFT', 'Games', 'ed/cs/games', 'Games for students to play', NULL, NULL, NULL, NOW(), NULL),
  ('BOARD', 'ACCEPTED', 'Opportunities', 'ed/cs/opportunities', 'Promote work and work experience opportunities to students', NULL, NULL, NULL, NOW(), NULL),
  ('BOARD', 'REJECTED', 'Housing', 'ed/cs/housing', 'Meet students to share houses with', NULL, NULL, NULL, NOW(), NULL),
  ('POST', 'DRAFT', 'Support Engineer', 'ed/cs/opportunities/1', 'Help people to use their computers', 'This will be soul destroying', NULL, NULL, NOW(), NULL),
  ('POST', 'SUSPENDED', 'UX Designer', 'ed/cs/opportunities/2', 'Design user-friendly software',
   'You will get to analyze requirements and produce screen designs', NULL, NULL, NOW(), NULL),
  ('POST', 'PENDING', 'Front-End Developer', 'ed/cs/opportunities/3', 'Writing responsive single page applications in AngularJs',
   'You will spend a lot of time fiddling around with stuff that never works very well', NULL, NULL, NOW(), NULL),
  ('POST', 'ACCEPTED', 'Database Engineer', 'ed/cs/opportunities/4', 'Design schemas and optimize queries', 'You will be working primarily with SQL Server', NULL, NULL, NOW(), NULL),
  ('POST', 'ACCEPTED', 'Java Web Developer', 'ed/cs/opportunities/5', 'Build fast, scalable backend services',
   'You will be implementing business logic in Spring Boot microservices', NULL, NULL, NOW(), NULL),
  ('POST', 'ACCEPTED', 'Technical Analyst', 'ed/cs/opportunities/6', 'Work out how to build things', 'Nobody will ever know what you actually do', NULL, NULL, NOW(), NULL),
  ('POST', 'EXPIRED', 'Scrum Leader', 'ed/cs/opportunities/7', 'Whip software engineers into shape',
   'You will be inculcating agile practice to ensure stuff gets done on time', NULL, NULL, NOW(), NULL),
  ('POST', 'REJECTED', 'Product Manager', 'ed/cs/opportunities/8', 'Work with customers to design products',
   'You will be annoying software engineers a lot', NULL, NULL, NOW(), NULL),
  ('POST', 'WITHDRAWN', 'Test Engineer', 'ed/cs/opportunities/9', 'Anticipate and find bugs before release to production',
   'You will be working primarily with Selenium', NULL, NULL, NOW(), NULL),
  ('POST', 'ARCHIVED', 'Software Architect', 'ed/cs/opportunities/10', 'Play lego all day',
   'You will work out how to put the big bits together', NULL, NULL, '2016-10-01 00:00:00', '2017-01-01 00:00:00'),
  ('POST', 'ARCHIVED', 'Business Analyst', 'ed/cs/opportunities/11', 'Solve the world', 'You will be the nuts in visio', NULL, NULL, '2017-01-01 00:00:00', '2017-04-01 00:00:00');

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
        AND user.email = 'department@administrator.com';

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
        AND user.email = 'department@member.com';

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
  WHERE resource.scope = 'BOARD'
        AND resource.state = 'ACCEPTED'
        AND user.email = 'board@administrator.com';

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
        AND user.email = 'board@author.com';

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
  WHERE resource.scope = 'POST'
        AND resource.name IN ('Database Engineer', 'Java Web Developer')
        AND user.email = 'board@author.com';

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
  WHERE resource.scope = 'POST'
        AND resource.name NOT IN ('Database Engineer', 'Java Web Developer')
        AND user.email = 'post@administrator.com';

SET @edId = (
  SELECT resource.id
  FROM resource
  WHERE resource.scope = 'UNIVERSITY'
        AND resource.handle = 'ed');

UPDATE resource
SET index_data = CONCAT(SOUNDEX('university'), ' ', CONCAT('of'), ' ', CONCAT('edinburgh'))
WHERE resource.id = @edId;

UPDATE resource
SET resource.parent_id = @edId
WHERE resource.scope IN ('UNIVERSITY', 'DEPARTMENT');

UPDATE resource AS resource1
  INNER JOIN resource AS resource2
SET resource1.parent_id = resource2.id
WHERE resource1.scope = 'BOARD'
      AND resource2.scope = 'DEPARTMENT';

UPDATE resource AS resource1
  INNER JOIN resource AS resource2
SET resource1.parent_id = resource2.id
WHERE resource1.scope = 'POST'
      AND resource2.scope = 'BOARD'
      AND resource2.state = 'ACCEPTED';

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
        AND resource1.state = 'ACCEPTED'
        AND resource2.scope = 'POST';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp)
  SELECT
    resource.id,
    resource.id,
    NOW()
  FROM resource
  WHERE resource.scope IN ('BOARD', 'POST');

INSERT INTO location (name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES ('London, United Kingdom', 'GB', 'code1', 1.00, 1.00, NOW()),
  ('Madrid, Spain', 'ES', 'code2', 2.00, 2.00, NOW()),
  ('Krakow, Poland', 'PL', 'code3', 3.00, 3.00, NOW());

UPDATE resource
  INNER JOIN location
SET resource.location_id = location.id
WHERE resource.scope = 'POST'
      AND resource.state <> 'ACCEPTED'
      AND location.google_id = 'code1';

UPDATE resource
  INNER JOIN location
SET resource.location_id = location.id
WHERE resource.scope = 'POST'
      AND resource.name = 'Database Engineer'
      AND location.google_id = 'code1';

UPDATE resource
  INNER JOIN location
SET resource.location_id = location.id
WHERE resource.scope = 'POST'
      AND resource.name = 'Java Web Developer'
      AND location.google_id = 'code2';

UPDATE resource
  INNER JOIN location
SET resource.location_id = location.id
WHERE resource.scope = 'POST'
      AND resource.name = 'Technical Analyst'
      AND location.google_id = 'code3';
