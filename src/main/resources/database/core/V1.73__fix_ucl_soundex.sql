UPDATE resource
SET resource.index_data = CONCAT(SOUNDEX('university'), ' ', CONCAT('college'), ' ', CONCAT('london'))
WHERE resource.scope = 'INSTITUTION';
