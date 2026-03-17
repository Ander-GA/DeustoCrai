DROP SCHEMA IF EXISTS deustocrai_db;
DROP USER IF EXISTS 'spq'@'%';
CREATE SCHEMA deustocrai_db;
CREATE USER IF NOT EXISTS 'spq'@'%' IDENTIFIED BY 'spq';
GRANT ALL ON deustocrai_db.* TO 'spq'@'%';

-- datos de prueba:
INSERT INTO deustocrai_db.aula (nombre, capacidad, tiene_proyector) VALUES ('Sala de Juntas', 10, true);
INSERT INTO deustocrai_db.aula (nombre, capacidad, tiene_proyector) VALUES ('Aula 102', 30, false);