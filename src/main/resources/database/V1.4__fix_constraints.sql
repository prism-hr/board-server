# Index cannot be unique in this model, code has to catch it
ALTER TABLE resource
  DROP INDEX handle,
  ADD INDEX handle (type, handle);
