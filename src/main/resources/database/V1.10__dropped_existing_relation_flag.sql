ALTER TABLE resource
  DROP COLUMN existing_relation,
  CHANGE COLUMN existing_relation_description existing_relation TEXT;
