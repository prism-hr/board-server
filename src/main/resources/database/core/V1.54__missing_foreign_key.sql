ALTER TABLE activity
  ADD INDEX (resource_event_id),
  ADD FOREIGN KEY (resource_event_id) REFERENCES resource_event (id);
