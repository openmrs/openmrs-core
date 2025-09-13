# Guía de Configuración y Testing de Notificaciones por Email

## 📧 Configuración paso a paso para probar el envío de emails

### Paso 1: Configurar cuenta de Gmail para envío de emails

1. **Habilitar verificación en 2 pasos** en tu cuenta de Gmail:
   - Ve a https://myaccount.google.com/security
   - Habilita "Verificación en 2 pasos"

2. **Crear una contraseña de aplicación**:
   - Ve a "Verificación en 2 pasos" → "Contraseñas de aplicaciones"
   - Selecciona "Correo" y "Otro"
   - Nombra la aplicación "OpenMRS"
   - Guarda la contraseña generada (16 caracteres)

### Paso 2: Configurar OpenMRS Database

1. **Ejecutar el script SQL**:
   ```bash
   # Conectarte a tu base de datos OpenMRS y ejecutar:
   mysql -u root -p openmrs < email-configuration-setup.sql
   ```

2. **Actualizar credenciales reales**:
   ```sql
   UPDATE global_property SET property_value = 'tu-email@gmail.com' WHERE property = 'mail.user';
   UPDATE global_property SET property_value = 'tu-app-password-16-chars' WHERE property = 'mail.password';
   UPDATE global_property SET property_value = 'tu-email@gmail.com' WHERE property = 'mail.from';
   ```

### Paso 3: Verificar configuración

en el archivo 'configure-my-gmail.sql' cambia las credenciales

1. **Ejecutar test de configuración**:
   ```bash
   cd api
   mvn test -Dtest=EncounterServiceEmailIntegrationTest#testEmailConfigurationIsSet
   ```

### Paso 4: Probar envío real de emails

1. **Actualizar email de prueba** en `EncounterServiceEmailIntegrationTest.java`:
   ```java
   private static final String TEST_EMAIL = "tu-email-de-prueba@gmail.com";
   ```

2. **Habilitar el test real**:
   - Comenta o elimina la línea `@Disabled` del método `testRealEmailNotification()`

3. **Ejecutar test de envío real**:
   ```bash
   mvn test -Dtest=EncounterServiceEmailIntegrationTest#testRealEmailNotification
   ```


## 🛠️ Solución de problemas

### Error: "Unable to send email"
- Verifica que las credenciales de Gmail sean correctas
- Asegúrate de usar la contraseña de aplicación, no tu contraseña normal
- Verifica que la verificación en 2 pasos esté habilitada

### Error: "PersonAttributeType not found"
- Ejecuta nuevamente el script SQL
- Verifica que el tipo de atributo "Email" se creó correctamente

### Error: "SMTP connection failed"
- Verifica que tu firewall/antivirus no esté bloqueando el puerto 587
- Prueba cambiar el puerto a 465 y usar SSL:
  ```sql
  UPDATE global_property SET property_value = '465' WHERE property = 'mail.smtp.port';
  INSERT INTO global_property (property, property_value, description, uuid) 
  VALUES ('mail.smtp.ssl.enable', 'true', 'Enable SSL', UUID());
  ```

## 📊 Monitoreo y logs

Para ver los logs de envío de emails, revisa:
- Console output durante los tests
- Log files de OpenMRS en `target/` o `logs/`
- Busca mensajes con "Email notification" o "MailMessageSender"

## 🔒 Seguridad

⚠️ **IMPORTANTE**: Nunca commitees credenciales reales al repositorio. 
- Usa variables de entorno para credenciales en producción
- El script SQL incluye credenciales de ejemplo que deben cambiarse

## 📝 Personalización

Para personalizar el contenido del email, modifica el método `generateEmailContent()` en `EncounterServiceImpl.java`:

```java
private String generateEmailContent(Patient patient, Encounter encounter) {
    return "Estimado/a " + patient.getPersonName().getFullName() + ",\n\n" +
           "Se ha programado una nueva cita médica para el " + 
           new SimpleDateFormat("dd/MM/yyyy HH:mm").format(encounter.getEncounterDatetime()) + ".\n\n" +
           "Ubicación: " + encounter.getLocation().getName() + "\n" +
           "Tipo de consulta: " + encounter.getEncounterType().getName() + "\n\n" +
           "Por favor, confirme su asistencia.\n\n" +
           "Saludos cordiales,\nSistema OpenMRS";
}
```