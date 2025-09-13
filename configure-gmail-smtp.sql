-- Script para configurar Gmail como servidor SMTP para testing
-- INSTRUCCIONES:
-- 1. Reemplaza 'TU_EMAIL@gmail.com' con tu email real de Gmail
-- 2. Reemplaza 'TU_APP_PASSWORD' con la contraseña de aplicación de Gmail (16 caracteres)
-- 3. Ejecuta este script en tu base de datos OpenMRS

USE openmrs;

-- Configuración SMTP para Gmail
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

-- CAMBIAR ESTOS VALORES POR LOS REALES:
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.user', 'TU_EMAIL@gmail.com', 'SMTP username', UUID()) 
ON DUPLICATE KEY UPDATE property_value='TU_EMAIL@gmail.com';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.password', 'TU_APP_PASSWORD', 'SMTP password (app password)', UUID()) 
ON DUPLICATE KEY UPDATE property_value='TU_APP_PASSWORD';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.from', 'TU_EMAIL@gmail.com', 'Default from email address', UUID()) 
ON DUPLICATE KEY UPDATE property_value='TU_EMAIL@gmail.com';

-- Verificar configuración
SELECT property, property_value 
FROM global_property 
WHERE property LIKE 'mail.%' OR property LIKE 'appointment.notification.%'
ORDER BY property;