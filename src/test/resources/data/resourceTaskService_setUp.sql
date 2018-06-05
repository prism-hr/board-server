INSERT INTO resource (id, scope, name, state, created_timestamp)
VALUES (1, 'DEPARTMENT', 'department1', 'DRAFT', NOW()),
  (2, 'DEPARTMENT', 'department2', 'DRAFT', NOW());

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES (1, UUID(), 'alastair', 'knowles', 'alastair@prism.hr', 'alastair@prism.hr', NOW());

INSERT INTO resource_task (id, resource_id, task, created_timestamp)
VALUES (1, 2, 'CREATE_MEMBER', NOW()),
  (2, 2, 'CREATE_POST', NOW());
