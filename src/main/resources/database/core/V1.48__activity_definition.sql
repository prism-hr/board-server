ALTER TABLE activity
  ADD COLUMN filter_by_category TINYINT(1) AFTER activity;

UPDATE activity
SET filter_by_category = 0;

ALTER TABLE activity
  MODIFY COLUMN filter_by_category TINYINT(1) NOT NULL AFTER activity;
