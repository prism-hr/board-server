ALTER TABLE permission
  DROP COLUMN role2,
  MODIFY COLUMN notification TEXT;

RENAME TABLE permission TO workflow;
