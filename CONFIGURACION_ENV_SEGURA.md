# Configuración Segura de Emails con Variables de Entorno

## 🔒 Configuración Segura sin Credenciales en el Repositorio

### Paso 1: Configurar Variables de Entorno

1. **Copia el archivo de ejemplo**:
   ```bash
   cp .env.example .env.local
   ```

2. **Edita `.env.local` con tus credenciales reales**:
   ```bash
   # Abrir en cualquier editor de texto
   notepad .env.local    # Windows
   nano .env.local       # Linux/Mac
   ```

3. **Configurar las variables necesarias**:
   ```env
   GMAIL_USER=tu-email@gmail.com
   GMAIL_APP_PASSWORD=tu-app-password-16-chars
   FROM_EMAIL=tu-email@gmail.com
   ```

### Paso 2: Obtener Contraseña de Aplicación Gmail

1. **Habilitar verificación en 2 pasos**:
   - Ve a https://myaccount.google.com/security
   - Habilita "Verificación en 2 pasos"

2. **Crear contraseña de aplicación**:
   - Ve a "Contraseñas de aplicaciones"
   - Selecciona "Correo" → "Otro" → "OpenMRS"
   - Copia la contraseña de 16 caracteres generada

### Paso 3: Aplicar Configuración

#### Opción A: Usando script automático (Recomendado)

**Windows (PowerShell)**:
```powershell
.\configure-email-from-env.ps1
```

**Linux/Mac (Bash)**:
```bash
chmod +x configure-email-from-env.sh
./configure-email-from-env.sh
```

#### Opción B: Configuración manual

1. **Ejecutar script generado**:
   ```bash
   # El script genera un SQL temporal que puedes ejecutar manualmente
   mysql -u root -p openmrs < configure-email-temp.sql
   ```

### Paso 4: Verificar Configuración

1. **Ejecutar test de configuración**:
   ```bash
   cd api
   mvn test -Dtest=EncounterServiceEmailIntegrationTest#testEmailsToAllRequiredRecipients
   ```

2. **Verificar en base de datos**:
   ```sql
   SELECT property, property_value 
   FROM global_property 
   WHERE property LIKE 'mail.%' OR property LIKE 'appointment.%';
   ```

## 🔐 Seguridad y Mejores Prácticas

### ✅ **Qué SÍ hacer**:
- ✅ Usar `.env.local` para credenciales reales
- ✅ Commitear `.env.example` como plantilla
- ✅ Añadir archivos sensibles a `.gitignore`
- ✅ Usar contraseñas de aplicación, no contraseñas normales
- ✅ Rotar credenciales regularmente

### ❌ **Qué NO hacer**:
- ❌ Commitear archivos `.env.local` o `.env`
- ❌ Poner credenciales reales en archivos de ejemplo
- ❌ Usar contraseñas de Gmail normales
- ❌ Compartir archivos con credenciales

## 📁 Estructura de Archivos

```
proyecto/
├── .env.example          # ✅ Plantilla (SÍ commitear)
├── .env.local           # ❌ Credenciales reales (NO commitear)
├── configure-email-from-env.sh    # ✅ Script Bash
├── configure-email-from-env.ps1   # ✅ Script PowerShell
├── .gitignore           # ✅ Actualizado con exclusiones
└── configure-my-gmail.sql # ❌ Credenciales reales (NO commitear)
```

## 🌍 Variables de Entorno Disponibles

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `GMAIL_USER` | Email de Gmail | `tu-email@gmail.com` |
| `GMAIL_APP_PASSWORD` | Contraseña de aplicación | `abcd efgh ijkl mnop` |
| `SMTP_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `SMTP_PORT` | Puerto SMTP | `587` |
| `SMTP_AUTH` | Autenticación requerida | `true` |
| `SMTP_STARTTLS` | Habilitar STARTTLS | `true` |
| `FROM_EMAIL` | Email remitente | `tu-email@gmail.com` |
| `FROM_NAME` | Nombre remitente | `Sistema OpenMRS` |

## 🔧 Personalización para Diferentes Entornos

### Desarrollo Local
```bash
# Usar archivo local
./configure-email-from-env.sh .env.local
```

### Testing/Staging
```bash
# Usar archivo de testing
./configure-email-from-env.sh .env.testing
```

### Producción
```bash
# Usar variables de entorno del sistema
export GMAIL_USER="produccion@empresa.com"
export GMAIL_APP_PASSWORD="contraseña-segura"
./configure-email-from-env.sh
```

## 🛠️ Solución de Problemas

### Error: "No se encontró el archivo .env.local"
```bash
# Copiar plantilla y editar
cp .env.example .env.local
# Editar con tus credenciales reales
```

### Error: "Variable no está definida"
- Verificar que todas las variables requeridas están en `.env.local`
- Verificar que no hay espacios extra alrededor del signo `=`

### Error: "SMTP authentication failed"
- Verificar que usas contraseña de aplicación, no contraseña normal
- Verificar que la verificación en 2 pasos está habilitada

## 📝 Para Desarrolladores

### Añadir Nueva Variable
1. Añadir a `.env.example`
2. Actualizar scripts de configuración
3. Documentar en esta guía

### Testing con Diferentes Configuraciones
```bash
# Crear archivo específico para testing
cp .env.example .env.test
# Configurar credenciales de test
./configure-email-from-env.sh .env.test
```

---

🔒 **Recuerda**: Los archivos `.env.local` y con credenciales reales NUNCA deben commitearse al repositorio. Usa siempre variables de entorno para entornos de producción.