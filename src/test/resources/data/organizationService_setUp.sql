SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM organization;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO organization(id, name, logo, created_timestamp)
VALUES(1, 'organization', 'organization logo', NOW());
