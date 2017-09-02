ALTER TABLE resource
  ADD COLUMN index_data TEXT
  AFTER last_referral_timestamp,
  ADD FULLTEXT INDEX (index_data);

ALTER TABLE user
  MODIFY COLUMN uuid VARCHAR(40) NOT NULL
  AFTER id,
  ADD COLUMN index_data TEXT
  AFTER website_resume,
  ADD FULLTEXT INDEX (index_data);

UPDATE resource
SET index_data = CONCAT_WS(" ", SOUNDEX(name), SOUNDEX(summary), SOUNDEX(description), SOUNDEX(organization_name));

UPDATE user
SET index_data = CONCAT_WS(" ", SOUNDEX(given_name), SOUNDEX(surname), SOUNDEX(email));
