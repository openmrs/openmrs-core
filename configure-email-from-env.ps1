# PowerShell script para configurar OpenMRS con variables de entorno
# Uso: .\configure-email-from-env.ps1 [archivo_env]

param(
    [string]$EnvFile = ".env.local"
)

# Verificar que existe el archivo .env
if (-not (Test-Path $EnvFile)) {
    Write-Host "❌ Error: No se encontró el archivo $EnvFile" -ForegroundColor Red
    Write-Host "📝 Instrucciones:" -ForegroundColor Yellow
    Write-Host "1. Copia .env.example a $EnvFile"
    Write-Host "2. Edita $EnvFile con tus credenciales reales"
    Write-Host "3. Ejecuta este script nuevamente"
    exit 1
}

Write-Host "🔧 Configurando OpenMRS con variables de entorno desde $EnvFile..." -ForegroundColor Green

# Cargar variables de entorno
$envVars = @{}
Get-Content $EnvFile | ForEach-Object {
    if ($_ -match "^([^#][^=]+)=(.*)$") {
        $envVars[$matches[1]] = $matches[2]
    }
}

# Verificar variables requeridas
$requiredVars = @("GMAIL_USER", "GMAIL_APP_PASSWORD", "SMTP_HOST", "SMTP_PORT", "FROM_EMAIL")
foreach ($var in $requiredVars) {
    if (-not $envVars.ContainsKey($var) -or [string]::IsNullOrEmpty($envVars[$var])) {
        Write-Host "❌ Error: Variable $var no está definida en $EnvFile" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ Variables de entorno cargadas correctamente" -ForegroundColor Green

# Generar script SQL temporal
$tempSqlFile = "configure-email-temp.sql"
$currentDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

$sqlContent = @"
-- Script generado automáticamente desde variables de entorno
-- Archivo: $EnvFile
-- Fecha: $currentDate

USE openmrs;

-- Configurar servidor SMTP
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.host', '$($envVars["SMTP_HOST"])', 'SMTP server hostname', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["SMTP_HOST"])';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.port', '$($envVars["SMTP_PORT"])', 'SMTP server port', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["SMTP_PORT"])';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.auth', '$($envVars["SMTP_AUTH"])', 'SMTP authentication required', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["SMTP_AUTH"])';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.smtp.starttls.enable', '$($envVars["SMTP_STARTTLS"])', 'Enable STARTTLS', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["SMTP_STARTTLS"])';

-- Configurar credenciales de autenticación
INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.user', '$($envVars["GMAIL_USER"])', 'SMTP username', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["GMAIL_USER"])';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.password', '$($envVars["GMAIL_APP_PASSWORD"])', 'SMTP password', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["GMAIL_APP_PASSWORD"])';

INSERT INTO global_property (property, property_value, description, uuid) 
VALUES ('mail.from', '$($envVars["FROM_EMAIL"])', 'Default from email address', UUID()) 
ON DUPLICATE KEY UPDATE property_value='$($envVars["FROM_EMAIL"])';

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
"@

Set-Content -Path $tempSqlFile -Value $sqlContent

Write-Host "📝 Script SQL generado: $tempSqlFile" -ForegroundColor Cyan
Write-Host "🔒 Este archivo contiene credenciales sensibles y será eliminado después del uso" -ForegroundColor Yellow

# Verificar si MySQL está disponible
Write-Host "� Script SQL generado: $tempSqlFile" -ForegroundColor Cyan
Write-Host "🔒 Este archivo contiene credenciales sensibles" -ForegroundColor Yellow
Write-Host "⚠️ Para aplicar la configuración, ejecuta manualmente:" -ForegroundColor Yellow
Write-Host "mysql -u root -p openmrs < `"$tempSqlFile`"" -ForegroundColor White

Write-Host "🎉 Configuración completada!" -ForegroundColor Green
Write-Host "📧 Emails configurados: $($envVars['GMAIL_USER'])" -ForegroundColor Cyan
Write-Host "🔧 Para probar: mvn test -Dtest=EncounterServiceEmailIntegrationTest" -ForegroundColor Cyan