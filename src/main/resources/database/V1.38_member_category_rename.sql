UPDATE resource_category
SET name = concat(name, '_STUDENT')
WHERE name = 'RESEARCH';

UPDATE user_role_category
SET name = concat(name, '_STUDENT')
WHERE name = 'RESEARCH';
