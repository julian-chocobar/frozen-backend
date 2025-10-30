# 📢 Sistema de Notificaciones - Documentación Técnica

## 🎯 **Resumen del Sistema**

Sistema de notificaciones moderno que utiliza **Server-Sent Events (SSE)** para notificaciones en tiempo real y **notificaciones persistidas** para diferentes flujos de trabajo del sistema. Soporta múltiples tipos de notificaciones automáticas basadas en eventos del sistema.

### **Características Principales:**

- ✅ **Notificaciones en tiempo real** vía Server-Sent Events (SSE)
- ✅ **Notificaciones persistidas** en base de datos con historial completo
- ✅ **Múltiples tipos de notificación** según eventos del sistema
- ✅ **Limpieza automática** de notificaciones antiguas (configurable)
- ✅ **Notificaciones dirigidas por rol** de usuario
- ✅ **Logging completo** para monitoreo y debugging

---

## 🏗️ **Arquitectura del Sistema**

### **1. Tipos de Notificación Soportados**

```java
public enum NotificationType {
    PRODUCTION_ORDER_PENDING,   // Nueva orden de producción pendiente (→ GERENTE_DE_PLANTA)
    PRODUCTION_ORDER_APPROVED,  // Orden de producción aprobada
    PRODUCTION_ORDER_REJECTED,  // Orden de producción rechazada
    SYSTEM_REMINDER,           // Recordatorio del sistema
    PENDING_MOVEMENT,          // Movimiento pendiente (→ OPERARIO_DE_ALMACEN)
    LOW_STOCK_ALERT           // Alerta de stock bajo (→ SUPERVISOR_DE_ALMACEN)
}
```

### **2. Modelo de Datos**

#### **Notificación**

```java
@Entity
public class Notification {
    private Long id;                    // ID único
    private Long userId;                // Usuario destinatario
    private NotificationType type;      // Tipo de notificación
    private String message;             // Mensaje descriptivo
    private Long relatedEntityId;       // ID de entidad relacionada
    private Boolean isRead;             // Estado de lectura
    private OffsetDateTime createdAt;   // Fecha de creación
    private OffsetDateTime readAt;      // Fecha de lectura
}
```

#### **Estados de Movimiento (Nuevos)**

```java
public enum MovementStatus {
    PENDIENTE,    // Movimiento creado pero no ejecutado
    COMPLETADO    // Movimiento ejecutado por operario
}

@Entity
public class Movement {
    // Campos existentes...
    private MovementStatus status;           // Estado del movimiento
    private Long createdByUserId;           // Usuario que creó el movimiento
    private Long completedByUserId;         // Usuario que completó el movimiento
    private OffsetDateTime creationDate;    // Fecha de creación
    private OffsetDateTime realizationDate; // Fecha de completado
}
```

---

## 🔄 **Flujos Automatizados**

### **1. Flujo de Órdenes de Producción** _(Existente - Mejorado)_

```
Usuario crea orden → ProductionOrderService.create()
                  ↓
Orden guardada → notificationService.createProductionOrderNotification()
              ↓
Notifica a GERENTE_DE_PLANTA → SSE + Persistencia
                             ↓
Gerente recibe notificación en tiempo real
```

### **2. NUEVO: Flujo de Movimientos Pendientes**

```
Usuario crea movimiento → MovementService.createMovement()
                        ↓
Movimiento en estado PENDIENTE → notificationService.createPendingMovementNotification()
                               ↓
Notifica a OPERARIO_DE_ALMACEN → SSE + Persistencia
                               ↓
Operario toma tarea → MovementService.completeMovement()
                    ↓
Movimiento ejecutado + stock actualizado
```

### **3. NUEVO: Flujo de Alertas de Stock Bajo**

```
Operario completa egreso → MovementService.completeMovement()
                         ↓
Verifica stock vs umbral → Si stock < threshold
                         ↓
notificationService.createLowStockNotification()
                         ↓
Notifica a SUPERVISOR_DE_ALMACEN → SSE + Persistencia
```

---

## 🌐 **API Endpoints**

### **📡 Server-Sent Events (SSE)**

#### **Conectar a notificaciones en tiempo real**

```http
GET /api/notifications/stream
Accept: text/event-stream
Authorization: Bearer {token}

Response: EventStream
```

**Eventos SSE emitidos:**

- `connected`: Confirmación de conexión establecida
- `notification`: Nueva notificación recibida
- `stats-update`: Actualización de estadísticas de notificaciones

#### **Información de conexiones**

```http
GET /api/notifications/connections

Response:
{
  "activeConnections": 2,
  "totalSystemConnections": 15
}
```

### **📋 Gestión de Notificaciones**

#### **Obtener notificaciones del usuario**

```http
GET /api/notifications?page=0&size=10&unreadOnly=false

Response:
{
  "notifications": [...],
  "currentPage": 0,
  "totalItems": 25,
  "totalPages": 3,
  "size": 10,
  "isFirst": true,
  "isLast": false,
  "hasNext": true,
  "hasPrevious": false
}
```

#### **Obtener solo notificaciones no leídas**

```http
GET /api/notifications?unreadOnly=true
```

#### **Marcar notificación como leída**

```http
PATCH /api/notifications/{id}/read

Response: NotificationResponseDTO
```

#### **Marcar todas como leídas**

```http
PATCH /api/notifications/read-all

Response: 200 OK
```

#### **Estadísticas de notificaciones**

```http
GET /api/notifications/stats

Response:
{
  "unreadCount": 5,
  "totalCount": 25
}
```

### **📦 NUEVOS: Gestión de Movimientos**

#### **Crear movimiento (ahora PENDIENTE)**

```http
POST /api/movements
Content-Type: application/json

{
  "materialId": 1,
  "type": "EGRESO",
  "stock": 50.0,
  "reason": "Consumo en producción"
}

Response: MovementResponseDTO (con status: "PENDIENTE")
```

#### **Completar movimiento pendiente**

```http
PATCH /api/movements/{id}/complete

Response: MovementResponseDTO (con status: "COMPLETADO")
```

#### **Obtener movimientos pendientes**

```http
GET /api/movements/pending?page=0&size=10

Response:
{
  "content": [...],
  "currentPage": 0,
  "totalItems": 8,
  "totalPages": 1
}
```

---

## 📨 **Estructura de DTOs**

### **NotificationResponseDTO**

```json
{
  "id": 123,
  "type": "PENDING_MOVEMENT",
  "message": "Nuevo movimiento de egreso pendiente para material: Harina",
  "relatedEntityId": 456,
  "isRead": false,
  "createdAt": "2025-10-29T10:30:00Z",
  "readAt": null
}
```

### **MovementResponseDTO** _(Actualizado)_

```json
{
  "id": 456,
  "type": "EGRESO",
  "status": "PENDIENTE",
  "creationDate": "2025-10-29T10:30:00Z",
  "realizationDate": null,
  "createdByUserId": 12,
  "completedByUserId": null,
  "stock": 50.0,
  "reason": "Consumo en producción",
  "materialName": "Harina",
  "materialType": "MATERIA_PRIMA",
  "unitMeasurement": "KILOGRAMO"
}
```

---

## 🎯 **Flujos por Rol de Usuario**

### **GERENTE_DE_PLANTA**

- ✅ Recibe notificaciones de órdenes de producción pendientes
- Notificaciones en tiempo real vía SSE

### **OPERARIO_DE_ALMACEN**

- 🆕 Recibe notificaciones de movimientos pendientes
- 🆕 Puede completar movimientos desde `/api/movements/{id}/complete`
- 📱 Notificaciones en tiempo real vía SSE

### **SUPERVISOR_DE_ALMACEN**

- 🆕 Recibe alertas de stock bajo
- 🆕 Monitorea materiales que quedan bajo umbral mínimo
- 📱 Notificaciones en tiempo real vía SSE

---

## ⚙️ **Configuración del Sistema**

### **Propiedades de aplicación**

```properties
# Limpieza automática de notificaciones (días)
app.notification.cleanup.days=30
```

### **Tareas programadas**

- **Limpieza de notificaciones**: Diariamente a las 2:00 AM
- **Elimina notificaciones**: Anteriores a 30 días (configurable)

---

## 🔧 **Integración Frontend**

### **1. Conexión SSE**

```javascript
// Establecer conexión SSE
const eventSource = new EventSource("/api/notifications/stream", {
  headers: { Authorization: "Bearer " + token },
});

// Escuchar eventos
eventSource.addEventListener("notification", (event) => {
  const notification = JSON.parse(event.data);
  showNotificationToUser(notification);
});

eventSource.addEventListener("stats-update", (event) => {
  const stats = JSON.parse(event.data);
  updateNotificationCounter(stats.unreadCount);
});
```

### **2. Gestión de movimientos pendientes**

```javascript
// Obtener movimientos pendientes
fetch("/api/movements/pending")
  .then((response) => response.json())
  .then((data) => {
    displayPendingMovements(data.content);
  });

// Completar movimiento
fetch(`/api/movements/${movementId}/complete`, { method: "PATCH" })
  .then((response) => response.json())
  .then((completedMovement) => {
    updateMovementStatus(completedMovement);
  });
```

---

## 🚀 **Estados de Implementación**

### **✅ Completamente implementado:**

- Server-Sent Events (SSE)
- Notificaciones de movimientos pendientes
- Notificaciones de stock bajo
- Limpieza automática de notificaciones
- Estados de movimiento (PENDIENTE/COMPLETADO)
- APIs REST para gestión completa

### **🔄 Mantenido del sistema anterior:**

- Notificaciones de órdenes de producción
- Logging y monitoreo

---

## 📊 **Monitoreo y Logs**

El sistema genera logs completos para:

- Conexiones SSE establecidas/cerradas
- Notificaciones creadas y enviadas
- Movimientos completados
- Alertas de stock bajo generadas
- Limpieza automática de datos

**Ejemplo de logs:**

```
INFO  - Nueva conexión SSE creada para usuario: operario123
INFO  - Movimiento 456 creado en estado PENDIENTE para material: Harina
INFO  - Notificación creada para movimiento pendiente 456 para 3 operarios de almacén
WARN  - Material Harina quedó por debajo del umbral. Stock actual: 15.0, Umbral: 20.0
INFO  - Movimiento 456 completado por usuario: operario456
```
