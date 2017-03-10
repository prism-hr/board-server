ALTER TABLE board
  ADD COLUMN post_categories VARCHAR(1000) NOT NULL
  AFTER purpose;

ALTER TABLE department
  ADD COLUMN member_categories VARCHAR(1000) NOT NULL
  AFTER name;
