ALTER TABLE resource_event
  ADD COLUMN referral VARCHAR(64)
  AFTER ip_address,
  ADD UNIQUE INDEX (referral);

ALTER TABLE resource
  DROP COLUMN default_post_visibility;
