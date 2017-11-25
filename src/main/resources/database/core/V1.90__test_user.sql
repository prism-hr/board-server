ALTER TABLE activity
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER filter_by_category,
  ADD INDEX (creator_id);

ALTER TABLE activity_event
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER event_count,
  ADD INDEX (creator_id);

ALTER TABLE activity_role
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER role,
  ADD INDEX (creator_id);

ALTER TABLE activity_user
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER user_id,
  ADD INDEX (creator_id);

ALTER TABLE document
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER file_name,
  ADD INDEX (creator_id);

ALTER TABLE location
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER longitude,
  ADD INDEX (creator_id);

ALTER TABLE resource
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER quarter,
  ADD INDEX (creator_id);

ALTER TABLE resource_category
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER type,
  ADD INDEX (creator_id);

ALTER TABLE resource_event
  DROP COLUMN visible_to_administrator,
  MODIFY COLUMN index_data TEXT AFTER covering_note,
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER index_data,
  ADD INDEX (creator_id);

ALTER TABLE resource_event_search
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER search,
  ADD INDEX (creator_id);

ALTER TABLE resource_operation
  DROP COLUMN notified_timestamp,
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER comment,
  ADD INDEX (creator_id);

ALTER TABLE resource_relation
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER resource2_id,
  ADD INDEX (creator_id);

ALTER TABLE resource_search
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER search,
  ADD INDEX (creator_id);

ALTER TABLE resource_task
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER notified_count,
  ADD INDEX (creator_id);

ALTER TABLE resource_task_suppression
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER user_id,
  ADD INDEX (creator_id);

ALTER TABLE test_email
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER message,
  ADD INDEX (creator_id);

ALTER TABLE user
  ADD COLUMN test_user INT(1) UNSIGNED AFTER oauth_account_id,
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER index_data,
  ADD INDEX (test_user),
  ADD INDEX (creator_id);

ALTER TABLE user_notification_suppression
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER resource_id,
  ADD INDEX (creator_id);

ALTER TABLE user_role
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER expiry_date,
  ADD INDEX (creator_id);

ALTER TABLE user_search
  ADD COLUMN creator_id BIGINT UNSIGNED AFTER search,
  ADD INDEX (creator_id);
