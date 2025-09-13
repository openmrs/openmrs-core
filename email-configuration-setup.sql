-- Script de configuración para testing de notificaciones por email en OpenMRS
-- Execute este script en tu base de datos OpenMRS para configurar el envío de emails

-- 1. Configuración del servidor SMTP (Gmail como ejemplo)
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.host', 'smtp.gmail.com', 'SMTP server hostname', UUID()) 
ON DUPLICATE KEY UPDATE property_value='smtp.gmail.com';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.port', '587', 'SMTP server port', UUID()) 
ON DUPLICATE KEY UPDATE property_value='587';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.auth', 'true', 'SMTP authentication required', UUID()) 
ON DUPLICATE KEY UPDATE property_value='true';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.starttls.enable', 'true', 'Enable STARTTLS', UUID()) 
ON DUPLICATE KEY UPDATE property_value='true';

-- 2. Credenciales de autenticación (CAMBIAR POR TUS CREDENCIALES REALES)
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.user', 'tu-email@gmail.com', 'SMTP username', UUID()) 
ON DUPLICATE KEY UPDATE property_value='tu-email@gmail.com';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.password', 'tu-app-password', 'SMTP password or app password', UUID()) 
ON DUPLICATE KEY UPDATE property_value='tu-app-password';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.from', 'noreply-openmrs@gmail.com', 'Default from email address', UUID()) 
ON DUPLICATE KEY UPDATE property_value='noreply-openmrs@gmail.com';

-- 3. Configuración de notificaciones de citas
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('appointment.notification.enabled', 'true', 'Enable email notifications for appointments', UUID()) 
ON DUPLICATE KEY UPDATE property_value='true';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('appointment.notification.subject', 'Nueva Cita Médica Programada', 'Subject for appointment notification emails', UUID()) 
ON DUPLICATE KEY UPDATE property_value='Nueva Cita Médica Programada';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('appointment.notification.patient.email.attribute', 'Email', 'Patient attribute name that contains email address', UUID()) 
ON DUPLICATE KEY UPDATE property_value='Email';

-- 4. Verificar si existe el tipo de atributo Email, si no lo crea
INSERT INTO person_attribute_type (name, description, format, searchable, creator, date_created, retired, uuid, sort_weight) 
SELECT 'Email', 'Patient email address', 'java.lang.String', 0, 1, NOW(), 0, UUID(), 1.0
WHERE NOT EXISTS (SELECT 1 FROM person_attribute_type WHERE name = 'Email');

SELECT 'Configuración completada. Verifica las propiedades en Administration > Settings > Global Properties' as Status;