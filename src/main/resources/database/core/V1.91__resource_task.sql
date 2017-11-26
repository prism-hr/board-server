UPDATE resource_task
SET resource_task.task = 'CREATE_POST'
WHERE resource_task.task = 'CREATE_INTERNAL_POST';
