ALTER TABLE resource
  ADD FULLTEXT INDEX organization_name_fulltext (organization_name);
