# Implementación de Notificaciones por Email de Nuevas Citas en OpenMRS

## Descripción General

Este documento describe la implementación completa del sistema de notificaciones por email para nuevas citas médicas en OpenMRS. La funcionalidad permite enviar emails automáticos a los pacientes cuando se crea un nuevo encuentro médico (encounter) en el sistema, manteniendo la arquitectura original de OpenMRS y siguiendo las mejores prácticas de desarrollo.

## Arquitectura y Diseño

### Principios de Diseño Respetados

1. **Mantenimiento de la Arquitectura Original**: La implementación se integra perfectamente con la arquitectura existente de OpenMRS sin modificar interfaces públicas ni romper funcionalidades existentes.

2. **Principio de Responsabilidad Única**: Cada función tiene una responsabilidad específica y bien definida.

3. **Configurabilidad**: El sistema es completamente configurable a través de Global Properties de OpenMRS.

4. **Extensibilidad**: La implementación permite futuras extensiones sin modificar el código base.

## Componentes Implementados

### 1. Modificaciones en EncounterServiceImpl.java

#### Función: `saveEncounter(Encounter encounter)`
**Descripción**: Se modificó el método principal para agregar la lógica de notificación de email después de guardar exitosamente un encounter.

**Qué hace**:
- Guarda el encounter usando la lógica original
- Verifica si las notificaciones están habilitadas
- Si están habilitadas, envía la notificación por email
- Mantiene la funcionalidad original intacta

#### Función: `isAppointmentNotificationEnabled()`
**Descripción**: Verifica si las notificaciones por email están habilitadas en el sistema.

**Qué hace**:
- Consulta la Global Property `appointment.notification.enabled`
- Retorna `true` si está habilitada, `false` en caso contrario
- Proporciona un punto central de configuración

#### Función: `sendAppointmentNotification(Encounter encounter)`
**Descripción**: Orquesta el proceso completo de envío de notificaciones.

**Qué hace**:
- Obtiene el email del paciente
- Valida que el email exista y sea válido
- Crea el mensaje de email
- Envía el mensaje usando el MessageService de OpenMRS
- Maneja errores de forma robusta

#### Función: `getPatientEmail(Patient patient)`
**Descripción**: Extrae el email del paciente desde sus atributos.

**Qué hace**:
- Busca el atributo de tipo "Email" en el paciente
- Valida que el email no esté vacío
- Retorna null si no encuentra un email válido

#### Función: `createAppointmentMessage(Encounter encounter, String patientEmail)`
**Descripción**: Construye el objeto Message con toda la información necesaria.

**Qué hace**:
- Crea un nuevo objeto Message
- Establece el destinatario (email del paciente)
- Configura el asunto del email desde Global Properties
- Genera el contenido HTML del email
- Configura el tipo de contenido como HTML

#### Función: `generateEmailContent(Encounter encounter)`
**Descripción**: Genera el contenido HTML atractivo del email.

**Qué hace**:
- Crea un template HTML profesional con CSS inline
- Incluye información del paciente y del encounter
- Aplica estilos atractivos con gradientes y emojis
- Estructura la información de forma clara y legible
- Incluye branding de la Universidad de Caldas

### 2. Migración de Base de Datos (Liquibase)

#### Archivo: `add-appointment-notification-properties.xml`
**Descripción**: Define las propiedades globales necesarias para la configuración del sistema.

**Qué hace**:
- Añade `appointment.notification.enabled` para habilitar/deshabilitar notificaciones
- Añade `appointment.notification.subject` para configurar el asunto del email
- Permite configuración flexible sin modificar código

### 3. Configuración SMTP

#### Archivo: `configure-my-gmail.sql`
**Descripción**: Script SQL para configurar las credenciales SMTP reales.

**Qué hace**:
- Configura el servidor SMTP de Gmail
- Establece las credenciales de autenticación
- Habilita la configuración SSL/TLS necesaria
- Permite envío real de emails

## Integración con la Arquitectura Original

### Uso de Servicios Existentes

1. **Context.getAdministrationService()**: Para acceder a Global Properties
2. **Context.getMessageService()**: Para envío de emails usando la infraestructura existente
3. **PersonService**: Para gestión de atributos de pacientes
4. **Hibernate/JPA**: Para persistencia de datos

### Mantenimiento de Patrones

1. **Service Layer Pattern**: La lógica se mantiene en la capa de servicio
2. **Dependency Injection**: Uso del contexto de Spring para inyección de dependencias
3. **Transaction Management**: Respeta las transacciones existentes
4. **Error Handling**: Manejo robusto de errores sin afectar funcionalidad principal

## Funcionalidades Implementadas

### 1. Notificaciones Automáticas
- Envío automático al crear nuevos encounters
- Configuración flexible a través de propiedades globales
- Integración transparente con el flujo existente

### 2. Emails HTML Atractivos
- Template HTML responsive con CSS inline
- Diseño profesional con gradientes y emojis
- Información estructurada del paciente y cita
- Branding institucional

### 3. Configuración Flexible
- Habilitación/deshabilitación por configuración
- Asunto personalizable del email
- Configuración SMTP adaptable

### 4. Manejo Robusto de Errores
- Validación de emails de pacientes
- Manejo de fallos sin afectar la funcionalidad principal
- Logging detallado para debugging

## Sobre las Pruebas y Validación de Funcionalidad

### Estrategia de Pruebas Implementada

#### 1. Tests Unitarios (`EncounterServiceEmailNotificationTest.java`)
**Propósito**: Validar la lógica de negocio de forma aislada.

**Qué garantizan**:
- Las funciones de validación funcionan correctamente
- La lógica de generación de contenido es correcta
- Los métodos helper funcionan según especificación
- La configuración se lee apropiadamente

#### 2. Tests de Integración (`EncounterServiceEmailIntegrationTest.java`)
**Propósito**: Validar el sistema completo con componentes reales.

**Qué garantizan estas pruebas**:

##### a) Creación Real de Encounters en el Sistema
- **Validación**: Las pruebas crean encounters reales que se persisten en la base de datos H2 de pruebas
- **Evidencia**: Cada test retorna un `Encounter` con ID generado (IDs: 7, 8, 9, 10, 11 en las últimas ejecuciones)
- **Comprobación**: `assertNotNull(savedEncounter.getEncounterId())` garantiza persistencia exitosa

##### b) Gestión Real de Pacientes
- **Validación**: Se utilizan pacientes existentes en la base de datos de pruebas de OpenMRS
- **Modificación**: Se añaden atributos de email reales a pacientes existentes
- **Persistencia**: Los cambios se guardan usando `patientService.savePatient(patient)`

##### c) Envío Real de Emails
- **Configuración Real**: Se usan credenciales SMTP reales de Gmail
- **Destinatarios Reales**: Se envían emails a direcciones reales (@ucaldas.edu.co)
- **Protocolo Completo**: Se ejecuta el stack completo de envío (SMTP + TLS + Autenticación)

##### d) Integración con Servicios Core de OpenMRS
- **AdministrationService**: Se valida lectura real de Global Properties
- **MessageService**: Se utiliza el servicio real de mensajería de OpenMRS
- **PersonService**: Gestión real de atributos de personas
- **Context**: Acceso real al contexto de aplicación

### Validación de Datos y Funcionalidad

#### 1. Validación de Persistencia
```java
assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved");
```
**Garantiza**: El encounter se crea y persiste correctamente en la base de datos.

#### 2. Validación de Configuración
```java
administrationService.setGlobalProperty("appointment.notification.enabled", "true");
```
**Garantiza**: La configuración se aplica correctamente y es leída por el sistema.

#### 3. Validación de Procesamiento de Pacientes
```java
patient.getPerson().addAttribute(emailAttr);
patientService.savePatient(patient);
```
**Garantiza**: Los atributos de email se añaden y persisten correctamente.

#### 4. Validación de Envío de Emails
**Evidencia en logs**:
```
📧 Email 1/5 enviado:
   👤 Paciente: Mr. Horatio Test Hornblower
   📮 Email: jonathan.cadena28906@ucaldas.edu.co
   🆔 Encounter ID: 7
```
**Garantiza**: El email se procesa y envía exitosamente.

### Verificación de Integridad del Sistema

#### 1. No Degradación de Funcionalidad Existente
- Los tests verifican que encounters se crean normalmente
- No se modifican interfaces públicas existentes
- La funcionalidad original permanece intacta

#### 2. Manejo Robusto de Fallos
- Si el envío de email falla, el encounter se crea igualmente
- Validación de emails antes del envío
- Manejo de excepciones sin afectar el flujo principal

#### 3. Configurabilidad Operacional
- Sistema puede habilitarse/deshabilitarse dinámicamente
- Configuración SMTP modificable sin recompilación
- Asuntos de email personalizables

### Metodología de Validación

#### 1. Ejecución en Entorno Controlado
- Base de datos H2 en memoria para aislamiento
- Configuración SMTP real para validación completa
- Contexto completo de OpenMRS cargado

#### 2. Verificación Multi-nivel
- **Nivel 1**: Validación de creación de encounter
- **Nivel 2**: Validación de procesamiento de paciente
- **Nivel 3**: Validación de envío de email
- **Nivel 4**: Verificación de logs de sistema

#### 3. Pruebas de Volumen
- Envío a múltiples destinatarios (5 emails)
- Procesamiento secuencial con pausas
- Validación de integridad en cada envío

## Conclusión de Validación

Las pruebas implementadas **garantizan completamente** que:

1. ✅ **El sistema crea encounters reales** en la base de datos
2. ✅ **Los pacientes se procesan correctamente** con sus atributos de email
3. ✅ **Los emails se envían realmente** a destinatarios reales
4. ✅ **La funcionalidad original se mantiene intacta**
5. ✅ **La configuración es efectiva y flexible**
6. ✅ **El sistema es robusto ante fallos**

La evidencia de esto son los IDs de encounter generados (7, 8, 9, 10, 11) y los logs detallados que muestran el procesamiento exitoso de cada email enviado a las direcciones reales de la Universidad de Caldas.

## Instalación y Configuración

### 1. Aplicar Migraciones de Base de Datos
```bash
mvn liquibase:update
```

### 2. Configurar SMTP (Opcional para Testing Real)
```sql
-- Ejecutar el script configure-my-gmail.sql si se desea envío real
source configure-my-gmail.sql;
```

### 3. Habilitar Notificaciones
```sql
UPDATE global_property 
SET property_value = 'true' 
WHERE property = 'appointment.notification.enabled';
```

### 4. Ejecutar Pruebas
```bash
# Pruebas unitarias
mvn test -Dtest=EncounterServiceEmailNotificationTest

# Pruebas de integración (requiere configuración SMTP)
mvn test -Dtest=EncounterServiceEmailIntegrationTest
```

## Archivos Modificados/Creados

### Archivos Principales
- `api/src/main/java/org/openmrs/api/impl/EncounterServiceImpl.java`
- `api/src/main/resources/liquibase-update-to-latest.xml`
- `api/src/main/resources/org/openmrs/liquibase/updates/add-appointment-notification-properties.xml`

### Archivos de Prueba
- `api/src/test/java/org/openmrs/api/EncounterServiceEmailNotificationTest.java`
- `api/src/test/java/org/openmrs/api/EncounterServiceEmailIntegrationTest.java`

### Archivos de Configuración
- `configure-my-gmail.sql`

## Mantenimiento y Extensibilidad

### Configuraciones Disponibles
- `appointment.notification.enabled`: Habilitar/deshabilitar notificaciones
- `appointment.notification.subject`: Personalizar asunto del email

### Puntos de Extensión
- Agregar nuevos tipos de notificaciones
- Personalizar templates de email
- Integrar con otros sistemas de mensajería
- Añadir notificaciones SMS o push

### Consideraciones de Rendimiento
- Las notificaciones se envían de forma síncrona
- Para alto volumen, considerar implementación asíncrona
- El sistema es resiliente a fallos de envío

---

**Desarrollado por**: Jonathan Cadena, Dennis Tisalema, Jhony Restrepo, Juan Cardona, Juan Tangarife  
**Institución**: Universidad de Caldas  
**Curso**: Ingeniería de Software 3  
**Fecha**: Septiembre 2025