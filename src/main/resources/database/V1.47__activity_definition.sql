ALTER TABLE workflow
  MODIFY COLUMN activity TEXT;

ALTER TABLE activity
  CHANGE COLUMN category activity VARCHAR(20) NOT NULL;
