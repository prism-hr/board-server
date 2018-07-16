ALTER TABLE resource_event
  ADD COLUMN role VARCHAR(20) AFTER user_id,
  DROP FOREIGN KEY resource_event_ibfk_4,
  DROP INDEX resource_id,
  ADD INDEX (resource_id, event, user_id, ip_address, role),
  ADD FOREIGN KEY (resource_id) REFERENCES  resource (id);

UPDATE resource_event INNER JOIN user_role
  ON resource_event.resource_id = user_role.resource_id
  AND resource_event.user_id = user_role.user_id
  AND user_role.role = 'MEMBER'
SET resource_event.role = 'MEMBER';

UPDATE resource_event
SET role = 'ADMINISTRATOR'
WHERE user_id IS NOT NULL
  AND role IS NULL;
