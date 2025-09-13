#!/bin/bash

# Script para ejecutar tests con variables de entorno
# Uso: ./run-tests-with-env.sh [archivo_env]

ENV_FILE="${1:-.env.local}"

# Verificar que existe el archivo .env
if [[ ! -f "$ENV_FILE" ]]; then
    echo "❌ Error: No se encontró el archivo $ENV_FILE"
    echo "📝 Copia .env.example a $ENV_FILE y edítalo con credenciales reales"
    exit 1
fi

echo "🔧 Cargando variables de entorno desde $ENV_FILE..."

# Cargar variables de entorno
source "$ENV_FILE"

# Exportar variables para que estén disponibles en el proceso Maven
export GMAIL_USER
export GMAIL_APP_PASSWORD
export SMTP_HOST
export SMTP_PORT
export SMTP_AUTH
export SMTP_STARTTLS
export FROM_EMAIL
export FROM_NAME
export TEST_EMAIL_1
export TEST_EMAIL_2
export TEST_EMAIL_3
export TEST_EMAIL_4
export TEST_EMAIL_5

echo "✅ Variables exportadas:"
echo "   📧 GMAIL_USER: $GMAIL_USER"
echo "   🏠 SMTP_HOST: $SMTP_HOST"
echo "   🔌 SMTP_PORT: $SMTP_PORT"
echo "   📮 TEST_EMAIL_1: $TEST_EMAIL_1"

echo "🚀 Ejecutando tests con variables de entorno..."

# Cambiar al directorio api y ejecutar tests
cd api

# Ejecutar el test específico con variables de entorno
mvn test -Dtest=EncounterServiceEmailIntegrationTest#testEmailsToAllRequiredRecipients \
    -DGMAIL_USER="$GMAIL_USER" \
    -DGMAIL_APP_PASSWORD="$GMAIL_APP_PASSWORD" \
    -DSMTP_HOST="$SMTP_HOST" \
    -DSMTP_PORT="$SMTP_PORT" \
    -DSMTP_AUTH="$SMTP_AUTH" \
    -DSMTP_STARTTLS="$SMTP_STARTTLS" \
    -DFROM_EMAIL="$FROM_EMAIL" \
    -DTEST_EMAIL_1="$TEST_EMAIL_1" \
    -DTEST_EMAIL_2="$TEST_EMAIL_2" \
    -DTEST_EMAIL_3="$TEST_EMAIL_3" \
    -DTEST_EMAIL_4="$TEST_EMAIL_4" \
    -DTEST_EMAIL_5="$TEST_EMAIL_5"

echo "🎉 Tests completados!"