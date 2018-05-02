SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM document;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO document (id, cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES (1, 'cloudinary id', 'cloudinary url', 'file name', NOW());
