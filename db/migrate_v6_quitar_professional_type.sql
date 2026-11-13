-- Migracion v5 -> v6: elimina professionals.professional_type
--
-- Contexto: la columna professional_type era una desnormalizacion de
-- specialty.name (las semillas de v5 usan 'MEDICO' con specialty 'Medico' y
-- 'TERAPEUTA' con 'Terapeuta'). El commit 1189469 elimino professionalType de
-- todo el codigo (entidad, dominio, DTOs, servicio, controlador, front y
-- tests) pero dejo la columna en la base de datos.
--
-- Efecto del desalineamiento: la columna es NOT NULL sin valor por defecto y
-- ProfessionalEntity ya no la mapea, por lo que todo INSERT en professionals
-- falla. GlobalExceptionHandler lo reportaba como 409 con el mensaje
-- "No se pudo guardar: algun dato ya existe o hace referencia a un registro
-- inexistente.", que no describe la causa real.
--
-- Esta migracion es idempotente: se puede ejecutar mas de una vez.
-- No borra datos de otras columnas. El valor eliminado se puede reconstruir
-- con un JOIN sobre specialty.name si en algun momento hace falta.

SET search_path TO piedrazul;

-- Respaldo opcional del dato antes de eliminarlo.
-- CREATE TABLE IF NOT EXISTS professionals_professional_type_backup AS
-- SELECT p.id AS professional_id, s.name AS professional_type
-- FROM professionals p
-- JOIN specialties s ON s.id = p.specialty_id;

ALTER TABLE piedrazul.professionals
    DROP COLUMN IF EXISTS professional_type;
