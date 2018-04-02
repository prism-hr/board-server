UPDATE resource
SET document_logo_id = NULL
WHERE scope = 'BOARD';

DROP TABLE resource_task_completion;
