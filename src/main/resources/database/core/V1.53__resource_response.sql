CREATE TABLE resource_event (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  resource_id        BIGINT UNSIGNED NOT NULL,
  event              VARCHAR(20)     NOT NULL,
  user_id            BIGINT UNSIGNED,
  ip_address         VARCHAR(50),
  document_resume_id BIGINT UNSIGNED,
  website_resume     VARCHAR(2048),
  covering_note      VARCHAR(1000),
  created_timestamp  DATETIME        NOT NULL,
  updated_timestamp  DATETIME,
  PRIMARY KEY (id),
  INDEX (resource_id, event, user_id, ip_address),
  INDEX (user_id),
  INDEX (document_resume_id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp),
  FOREIGN KEY (resource_id) REFERENCES resource (id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (document_resume_id) REFERENCES document (id)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

ALTER TABLE user
  ADD COLUMN document_resume_id BIGINT UNSIGNED
  AFTER document_image_request_state,
  ADD INDEX (document_resume_id),
  ADD FOREIGN KEY (document_resume_id) REFERENCES document (id),
  ADD COLUMN website_resume VARCHAR(2048)
  AFTER document_resume_id;

ALTER TABLE resource
  ADD COLUMN forward_candidates INT(1) UNSIGNED
  AFTER apply_email,
  MODIFY COLUMN apply_email VARCHAR(255),
  ADD COLUMN board_count BIGINT UNSIGNED
  AFTER dead_timestamp,
  ADD COLUMN post_count BIGINT UNSIGNED
  AFTER board_count,
  ADD COLUMN author_count BIGINT UNSIGNED
  AFTER post_count,
  ADD COLUMN member_count BIGINT UNSIGNED
  AFTER author_count,
  ADD COLUMN view_count BIGINT UNSIGNED
  AFTER member_count,
  ADD COLUMN referral_count BIGINT UNSIGNED
  AFTER view_count,
  ADD COLUMN response_count BIGINT UNSIGNED
  AFTER referral_count,
  ADD COLUMN last_view_timestamp DATETIME
  AFTER response_count,
  ADD COLUMN last_referral_timestamp DATETIME
  AFTER last_view_timestamp,
  ADD COLUMN last_response_timestamp DATETIME
  AFTER last_referral_timestamp;

ALTER TABLE activity
  DROP FOREIGN KEY activity_ibfk_3,
  DROP INDEX resource_id,
  ADD COLUMN resource_event_id BIGINT UNSIGNED
  AFTER user_role_id,
  ADD UNIQUE INDEX (resource_id, user_role_id, resource_event_id, activity),
  ADD FOREIGN KEY (resource_id) REFERENCES resource (id);

CREATE TABLE activity_event (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  activity_id       BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  event             VARCHAR(20)     NOT NULL,
  event_count       BIGINT          NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX (activity_id, user_id, event),
  INDEX (user_id),
  FOREIGN KEY (activity_id) REFERENCES activity (id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  INDEX (created_timestamp),
  INDEX (updated_timestamp)
)
  COLLATE = utf8_general_ci
  ENGINE = innodb;

INSERT INTO activity_event (activity_id, user_id, event, event_count, created_timestamp, updated_timestamp)
  SELECT
    activity_id,
    user_id,
    'DISMISSAL',
    1,
    created_timestamp,
    updated_timestamp
  FROM activity_dismissal;

DROP TABLE activity_dismissal;
