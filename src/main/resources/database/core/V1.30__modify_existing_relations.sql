UPDATE resource
SET existing_relation_explanation = REPLACE(existing_relation_explanation, 'PREVIOUS_', '')
WHERE scope = 'POST'
      AND existing_relation_explanation IS NOT NULL;
