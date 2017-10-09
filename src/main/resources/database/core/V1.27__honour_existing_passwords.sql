UPDATE user
SET password = SHA2('password', 256);
