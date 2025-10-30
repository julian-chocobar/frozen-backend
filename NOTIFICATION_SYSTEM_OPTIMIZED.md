# 📢 Sistema de Notificaciones - Documentación Actualizada

## 🎯 **Resumen del Sistema**

Sistema de notificaciones optimizado para **polling en ventanas específicas** que notifica automáticamente a usuarios con rol `GERENTE_DE_PLANTA` cuando hay órdenes de producción pendientes de aprobación.

### **Características Principales:**

- ✅ **Notificaciones automáticas** al crear órdenes de producción
- ✅ **Polling optimizado** en ventanas de tiempo específicas (09:00-10:00 y 17:00-18:00)
- ✅ **Cache inteligente** para optimizar rendimiento
- ✅ **Logging completo** para monitoreo y debugging
- ✅ **Sin endpoints de creación pública** - solo generación automática interna

---

## 🏗️ **Arquitectura del Backend**

### **1. Entidad Principal**

```java
// Notification.java - Entidad JPA optimizada
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_read_created", columnList = "user_id, is_read, created_at"),
    @Index(name = "idx_type_entity", columnList = "type, related_entity_id")
})
public class Notification {
    private Long id;
    private Long userId;           // FK al usuario
    private NotificationType type; // PRODUCTION_ORDER_PENDING, etc.
    private String message;        // Mensaje descriptivo
    private Long relatedEntityId;  // ID de la orden de producción
    private Boolean isRead;        // Estado de lectura
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;

    public void markAsRead() { ... } // Método helper
}
```

### **2. Repositorio Optimizado**

```java
// NotificationRepository.java - Consultas específicas para gerentes
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Consultas básicas por usuario
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Long countByUserIdAndIsReadFalse(Long userId);

    // Consultas optimizadas para gerentes de planta
    @Query("SELECT n FROM Notification n JOIN User u ON n.userId = u.id " +
           "WHERE u.role = :role AND n.isRead = false")
    List<Notification> findUnreadNotificationsByUserRole(@Param("role") Role role);

    // Evitar duplicados
    List<Notification> findByUserIdAndTypeAndRelatedEntityId(Long userId, NotificationType type, Long relatedEntityId);
}
```

### **3. Servicio Simplificado**

```java
// NotificationService.java - Solo métodos necesarios
public interface NotificationService {

    // 🔒 INTERNO: Creación automática (no expuesto en API)
    NotificationResponseDTO createNotification(Long userId, NotificationType type, String message, Long relatedEntityId);

    // 📖 PÚBLICO: Lectura y gestión
    Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable);
    Page<NotificationResponseDTO> getUserUnreadNotifications(Long userId, Pageable pageable);
    NotificationResponseDTO markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    NotificationStatsDTO getUserNotificationStats(Long userId);

    // 🎯 AUTOMÁTICO: Disparador para órdenes de producción
    void createProductionOrderNotification(Long orderId, String productName);
}
```

### **4. Controller con Cache y Logging**

```java
// NotificationController.java - API optimizada para polling
@RestController
@RequestMapping("/api/notifications")
@Cacheable // Cache habilitado globalmente
@Slf4j
public class NotificationController {

    // 📄 Obtener notificaciones (con cache por usuario/página)
    @GetMapping
    @Cacheable(value = "userNotifications", key = "#root.target.getUserCacheKey(unreadOnly, pageable)")
    public ResponseEntity<Page<NotificationResponseDTO>> getUserNotifications(...);

    // 📊 Estadísticas de notificaciones (cached por usuario)
    @GetMapping("/stats")
    @Cacheable(value = "notificationStats", key = "#root.target.getStatsKey()")
    public ResponseEntity<NotificationStatsDTO> getNotificationStats(...);

    // ⏰ Configuración de ventanas de polling
    @GetMapping("/polling-windows")
    @Cacheable(value = "pollingWindows")
    public ResponseEntity<List<PollingWindowDTO>> getPollingWindows(...);

    // ✅ Marcar como leída (invalida cache automáticamente)
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(...);
}
```

---

## 🔄 **Flujo Automatizado**

### **1. Generación Automática de Notificaciones**

```java
// En ProductionOrderServiceImpl.java - Al crear una nueva orden
@Override
@Transactional
public ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO createDTO) {
    // ... lógica de creación de la orden ...

    ProductionOrder savedOrder = productionOrderRepository.save(order);

    // 🚨 AUTOMÁTICO: Crear notificación para gerentes
    notificationService.createProductionOrderNotification(
        savedOrder.getId(),
        product.getName()
    );

    return mapper.toResponseDTO(savedOrder);
}
```

### **2. Proceso de Notificación Interna**

```java
// NotificationServiceImpl.java
public void createProductionOrderNotification(Long orderId, String productName) {
    // 1. Buscar todos los gerentes de planta
    List<Long> plantManagerIds = userRepository.findIdsByRole(Role.GERENTE_DE_PLANTA);

    // 2. Crear mensaje descriptivo
    String message = String.format("Nueva orden de producción pendiente de aprobación para producto: %s", productName);

    // 3. Crear notificación para cada gerente (evitando duplicados)
    for (Long userId : plantManagerIds) {
        List<Notification> existing = notificationRepository
            .findByUserIdAndTypeAndRelatedEntityId(userId, NotificationType.PRODUCTION_ORDER_PENDING, orderId);

        if (existing.isEmpty()) {
            createNotification(userId, NotificationType.PRODUCTION_ORDER_PENDING, message, orderId);
            log.info("Notificación creada para gerente {} sobre orden {}", userId, orderId);
        }
    }
}
```

---

## 🕒 **Sistema de Polling Optimizado**

### **Configuración de Ventanas**

```java
// PollingWindowDTO.java - Configuración predeterminada
public static List<PollingWindowDTO> getDefaultWindows() {
    return List.of(
        // Ventana matutina: 09:00 - 10:00
        PollingWindowDTO.builder()
            .name("morning")
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .pollingIntervalMinutes(10)  // Polling cada 10 min
            .isActive(false)  // Se calcula dinámicamente
            .build(),

        // Ventana vespertina: 17:00 - 18:00
        PollingWindowDTO.builder()
            .name("afternoon")
            .startTime(LocalTime.of(17, 0))
            .endTime(LocalTime.of(18, 0))
            .pollingIntervalMinutes(10)  // Polling cada 10 min
            .isActive(false)  // Se calcula dinámicamente
            .build()
    );
}
```

### **Endpoint de Configuración**

```http
GET /api/notifications/polling-windows

Response:
[
  {
    "name": "morning",
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "pollingIntervalMinutes": 10,
    "isActive": true  // ← Se calcula según hora actual
  },
  {
    "name": "afternoon",
    "startTime": "17:00:00",
    "endTime": "18:00:00",
    "pollingIntervalMinutes": 10,
    "isActive": false
  }
]
```

---

## ⚡ **Optimizaciones de Rendimiento**

### **1. Configuración de Cache**

```java
// NotificationCacheConfig.java
@Configuration
@EnableCaching
public class NotificationCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            "userNotifications",    // Cache por usuario/página
            "notificationStats",    // Cache de estadísticas
            "pollingWindows"        // Cache de configuración
        ));
        return cacheManager;
    }
}
```

### **2. Logging y Monitoreo**

```java
// En NotificationController.java - Logging automático
@GetMapping
public ResponseEntity<Page<NotificationResponseDTO>> getUserNotifications(...) {
    String clientIp = getClientIpAddress(request);
    log.debug("Solicitud de notificaciones - Usuario: {}, IP: {}", currentUser.getUsername(), clientIp);

    long startTime = System.currentTimeMillis();
    // ... procesamiento ...
    long endTime = System.currentTimeMillis();

    log.info("Notificaciones procesadas en {}ms para usuario: {}", (endTime - startTime), currentUser.getId());
    return ResponseEntity.ok(notifications);
}
```

---

## 📡 **API Endpoints Disponibles**

### **🔓 Endpoints Públicos (para frontend)**

```http
# Obtener notificaciones del usuario actual
GET /api/notifications?page=0&size=10&unreadOnly=true

# Obtener estadísticas de notificaciones
GET /api/notifications/stats

# Obtener configuración de ventanas de polling
GET /api/notifications/polling-windows

# Marcar notificación como leída
PATCH /api/notifications/{id}/read

# Marcar todas como leídas
PATCH /api/notifications/read-all
```

### **🔒 No Hay Endpoints de Creación**

- ❌ `POST /api/notifications` - **NO EXISTE**
- ❌ `PUT /api/notifications` - **NO EXISTE**
- ✅ Las notificaciones se crean **automáticamente** cuando ocurren eventos del sistema

---

## 🎯 **Tipos de Notificación**

```java
// NotificationType.enum
public enum NotificationType {
    PRODUCTION_ORDER_PENDING("Orden de Producción Pendiente"),
    PRODUCTION_ORDER_APPROVED("Orden de Producción Aprobada"),
    PRODUCTION_ORDER_REJECTED("Orden de Producción Rechazada"),
    SYSTEM_MAINTENANCE("Mantenimiento del Sistema"),
    STOCK_ALERT("Alerta de Stock");

    private final String displayName;
}
```

---

## 🔄 **Estados y Flujos**

### **Estado de Notificaciones**

```
📨 CREADA → 👁️ LEÍDA → 🗑️ (soft delete - mantenemos historial)
```

### **Flujo de Orden de Producción**

```
1. Usuario crea orden → ProductionOrderService.create()
2. Orden guardada → notificationService.createProductionOrderNotification()
3. Buscar gerentes → userRepository.findIdsByRole(GERENTE_DE_PLANTA)
4. Crear notificación → Notification.save() para cada gerente
5. Frontend polling → GET /api/notifications (solo durante ventanas activas)
6. Gerente lee → PATCH /api/notifications/{id}/read
7. Gerente aprueba/rechaza orden → (proceso separado)
```

---

## 📋 **Siguientes Pasos para Frontend**

El backend está **100% completo y optimizado**. Ver prompt específico para desarrollador frontend al final de esta documentación.
