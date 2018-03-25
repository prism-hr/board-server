ALTER TABLE resource
  DROP COLUMN internal,
  ADD COLUMN organization_logo VARCHAR(255) AFTER organization_name;
