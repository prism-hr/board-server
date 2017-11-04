UPDATE user_role
  INNER JOIN user
    ON user_role.user_id = user.id
SET user_role.email = user.email
WHERE user_role.email IS NULL
      AND user_role.role = 'MEMBER'
      AND user.email LIKE '%@ucl.ac.uk';
