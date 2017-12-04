DELETE
FROM test_email;

ALTER TABLE test_email
  DROP FOREIGN KEY test_email_ibfk_1,
  DROP INDEX user_id,
  DROP COLUMN user_id,
  ADD COLUMN email VARCHAR(255) NOT NULL AFTER id,
  ADD INDEX (email),
  ADD COLUMN user TEXT NOT NULL AFTER email;
