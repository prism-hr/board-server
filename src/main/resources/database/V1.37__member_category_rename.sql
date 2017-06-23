UPDATE resource_category
SET name = concat(name, '_STUDENT')
WHERE name IN ('UNDERGRADUATE', 'MASTER', 'POSTGRADUATE');

UPDATE user_role_category
SET name = concat(name, '_STUDENT')
WHERE name IN ('UNDERGRADUATE', 'MASTER', 'POSTGRADUATE');
