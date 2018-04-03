ALTER TABLE activity
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE activity
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE activity_event
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE activity_event
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE activity_role
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE activity_role
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE activity_user
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE activity_user
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE document
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE document
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE location
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE location
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN state_change_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN live_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN dead_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN last_member_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN last_task_creation_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN last_view_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN last_referral_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN last_response_timestamp DATETIME(3);

ALTER TABLE resource
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_category
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_category
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_event
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_event
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_event_search
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_event_search
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_operation
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_operation
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_relation
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_relation
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_search
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_search
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE resource_task
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE resource_task
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE test_email
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE test_email
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE user
	MODIFY COLUMN password_reset_timestamp DATETIME(3);

ALTER TABLE user
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE user
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE user_notification_suppression
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE user_notification_suppression
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE user_role
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE user_role
	MODIFY COLUMN updated_timestamp DATETIME(3);

ALTER TABLE user_search
	MODIFY COLUMN created_timestamp DATETIME(3) NOT NULL;

ALTER TABLE user_search
	MODIFY COLUMN updated_timestamp DATETIME(3);
