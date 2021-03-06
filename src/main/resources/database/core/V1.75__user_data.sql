ALTER TABLE user
  ADD COLUMN gender VARCHAR(10)
  AFTER document_image_request_state,
  ADD COLUMN age_range VARCHAR(10)
  AFTER gender,
  ADD COLUMN location_nationality_id BIGINT UNSIGNED
  AFTER age_range,
  ADD INDEX (location_nationality_id),
  ADD FOREIGN KEY (location_nationality_id) REFERENCES location (id);

ALTER TABLE user_role
  ADD COLUMN member_category VARCHAR(30)
  AFTER role,
  ADD COLUMN member_program VARCHAR(255)
  AFTER member_category,
  ADD COLUMN member_year INT(2) UNSIGNED
  AFTER member_program,
  ADD COLUMN member_date DATE
  AFTER member_program;

UPDATE user_role
  INNER JOIN user_role_category
    ON user_role.id = user_role_category.user_role_id
SET user_role.member_category = user_role_category.name;

DROP TABLE user_role_category;
