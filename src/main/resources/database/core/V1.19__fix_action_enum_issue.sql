UPDATE resource_operation
SET action = 'VIEW'
WHERE action = '0';

UPDATE resource_operation
SET action = 'EDIT'
WHERE action = '1';

UPDATE resource_operation
SET action = 'EXTEND'
WHERE action = '2';

UPDATE resource_operation
SET action = 'ACCEPT'
WHERE action = '3';

UPDATE resource_operation
SET action = 'SUSPEND'
WHERE action = '4';

UPDATE resource_operation
SET action = 'CORRECT'
WHERE action = '5';

UPDATE resource_operation
SET action = 'REJECT'
WHERE action = '6';

UPDATE resource_operation
SET action = 'PUBLISH'
WHERE action = '7';

UPDATE resource_operation
SET action = 'RETIRE'
WHERE action = '8';

UPDATE resource_operation
SET action = 'RESTORE'
WHERE action = '9';

UPDATE resource_operation
SET action = 'WITHDRAW'
WHERE action = '10';
