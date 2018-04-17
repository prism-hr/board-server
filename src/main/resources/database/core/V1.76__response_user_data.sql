ALTER TABLE user
  MODIFY COLUMN age_range VARCHAR(30);

ALTER TABLE resource_event
  ADD COLUMN gender VARCHAR(10)
  AFTER referral,
  ADD COLUMN age_range VARCHAR(30)
  AFTER gender,
  ADD COLUMN location_nationality_id BIGINT UNSIGNED
  AFTER age_range,
  ADD COLUMN member_category VARCHAR(30)
  AFTER location_nationality_id,
  ADD COLUMN member_program VARCHAR(255)
  AFTER member_category,
  ADD COLUMN member_year INT(2) UNSIGNED
  AFTER member_program,
  ADD COLUMN index_data TEXT,
  ADD INDEX (location_nationality_id),
  ADD FULLTEXT INDEX (index_data),
  ADD FOREIGN KEY (location_nationality_id) REFERENCES defaultLocation (id);
