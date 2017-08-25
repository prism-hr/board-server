ALTER TABLE resource
  DROP COLUMN forward_candidates;

DELETE
FROM user_role_category
WHERE name IN ('ACADEMIC_STAFF', 'PROFESSIONAL_STAFF');

DELETE
FROM resource_category
WHERE type = 'MEMBER'
      AND name IN ('ACADEMIC_STAFF', 'PROFESSIONAL_STAFF');
