DROP SCHEMA IF EXISTS deustocrai_db;
DROP USER IF EXISTS 'spq'@'%';
CREATE SCHEMA deustocrai_db;
CREATE USER IF NOT EXISTS 'spq'@'%' IDENTIFIED BY 'spq';
GRANT ALL ON deustocrai_db.* TO 'spq'@'%';

