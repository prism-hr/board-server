ALTER TABLE resource_event
  ADD COLUMN visible_to_administrator INT(1) UNSIGNED
  AFTER covering_note,
  DROP FOREIGN KEY resource_event_ibfk_1,
  DROP INDEX resource_id,
  ADD INDEX (resource_id, event, user_id, ip_address, visible_to_administrator),
  ADD FOREIGN KEY (resource_id) REFERENCES resource (id);
