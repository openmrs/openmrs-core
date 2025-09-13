#!/bin/bash

# Script para configurar OpenMRS con variables de entorno
# Uso: ./configure-email-from-env.sh [archivo_env]

ENV_FILE="${1:-.env.local}"

# Verificar que existe el archivo .env
if [[ ! -f "$ENV_FILE" ]]; then
    echo "❌ Error: No se encontró el archivo $ENV_FILE"
    echo "📝 Instrucciones:"
    echo "1. Copia .env.example a $ENV_FILE"
    echo "2. Edita $ENV_FILE con tus credenciales reales"
    echo "3. Ejecuta este script nuevamente"
    exit 1
fi

echo "🔧 Configurando OpenMRS con variables de entorno desde $ENV_FILE..."

# Cargar variables de entorno
source "$ENV_FILE"

# Verificar variables requeridas
required_vars=("GMAIL_USER" "GMAIL_APP_PASSWORD" "SMTP_HOST" "SMTP_PORT" "FROM_EMAIL")
for var in "${required_vars[@]}"; do
    if [[ -z "${!var}" ]]; then
        echo "❌ Error: Variable $var no está definida en $ENV_FILE"
        exit 1
    fi
done

echo "✅ Variables de entorno cargadas correctamente"

# Generar script SQL temporal
TEMP_SQL_FILE="configure-email-temp.sql"

cat > "$TEMP_SQL_FILE" << EOF
-- Script generado automáticamente desde variables de entorno
-- Archivo: $ENV_FILE
-- Fecha: $(date)

USE openmrs;

-- Configurar servidor SMTP
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.host', '$SMTP_HOST', 'SMTP server hostname', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$SMTP_HOST';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.port', '$SMTP_PORT', 'SMTP server port', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$SMTP_PORT';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.auth', '$SMTP_AUTH', 'SMTP authentication required', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$SMTP_AUTH';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.starttls.enable', '$SMTP_STARTTLS', 'Enable STARTTLS', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$SMTP_STARTTLS';

-- Configurar credenciales de autenticación
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.user', '$GMAIL_USER', 'SMTP username', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$GMAIL_USER';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.password', '$GMAIL_APP_PASSWORD', 'SMTP password', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$GMAIL_APP_PASSWORD';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.from', '$FROM_EMAIL', 'Default from email address', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$FROM_EMAIL';

-- Configurar propiedades de notificación
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('appointment.notification.enabled', 'true', 'Enable appointment email notifications', UUID()) 
ON DUPLICATE KEY UPDATE property_value='true';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('appointment.notification.subject', '🏥 Nueva Cita Médica Programada - OpenMRS', 'Subject for appointment emails', UUID()) 
ON DUPLICATE KEY UPDATE property_value='🏥 Nueva Cita Médica Programada - OpenMRS';

-- Verificar configuración
SELECT 'Configuración SMTP completada' as status;
SELECT property, property_value FROM global_property WHERE property LIKE 'mail.%' OR property LIKE 'appointment.%';
EOF

echo "📝 Script SQL generado: $TEMP_SQL_FILE"
echo "🔒 Este archivo contiene credenciales sensibles y será eliminado después del uso"

# Ejecutar el script SQL si MySQL está disponible
if command -v mysql &> /dev/null; then
    echo "🗄️ Aplicando configuración a la base de datos..."
    mysql -u root -p openmrs < "$TEMP_SQL_FILE"
    if [[ $? -eq 0 ]]; then
        echo "✅ Configuración aplicada exitosamente"
    else
        echo "❌ Error al aplicar la configuración. Ejecuta manualmente:"
        echo "mysql -u root -p openmrs < $TEMP_SQL_FILE"
    fi
else
    echo "⚠️ MySQL CLI no encontrado. Ejecuta manualmente:"
    echo "mysql -u root -p openmrs < $TEMP_SQL_FILE"
fi

# Limpiar archivo temporal por seguridad
echo "🧹 Limpiando archivos temporales..."
rm -f "$TEMP_SQL_FILE"

echo "🎉 Configuración completada!"
echo "📧 Emails configurados: $GMAIL_USER"
echo "🔧 Para probar: mvn test -Dtest=EncounterServiceEmailIntegrationTest"