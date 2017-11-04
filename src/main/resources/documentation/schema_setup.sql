CREATE SCHEMA IF NOT EXISTS board;
CREATE SCHEMA IF NOT EXISTS board_test;
CREATE USER 'prism'@'%' IDENTIFIED BY 'pgadmissions';
GRANT ALL ON board.* TO 'prism'@'%' IDENTIFIED BY 'pgadmissions';
GRANT ALL ON board_test.* TO 'prism'@'%' IDENTIFIED BY 'pgadmissions';
