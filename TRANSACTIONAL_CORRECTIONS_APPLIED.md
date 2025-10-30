# 🔧 CORRECCIONES APLICADAS Y PLAN DE ACCIÓN

## ✅ CORRECCIONES YA APLICADAS

### **UserServiceImpl.java**

```java
// ❌ REMOVIDO @Transactional (causaba connection leaks)
public User getCurrentUser() {
    // Sin @Transactional - transacción automática muy corta de Spring Data
}

// ✅ AGREGADO @Transactional (operaciones de escritura)
@Transactional
public UserResponseDTO createUser(UserCreateDTO dto) { ... }

@Transactional
public UserResponseDTO toggleActive(Long id) { ... }

@Transactional
public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) { ... }

@Transactional
public UserResponseDTO updateUserRole(Long id, UpdateRoleDTO dto) { ... }
```

---

## 📋 AUDIT COMPLETO PENDIENTE

### **PATRÓN A SEGUIR EN TODOS LOS SERVICIOS:**

#### 🟢 **MANTENER @Transactional:**

- ✅ Métodos `create*()`
- ✅ Métodos `update*()`
- ✅ Métodos `delete*()`
- ✅ Métodos `toggle*()`, `activate*()`, `deactivate*()`
- ✅ Operaciones complejas con múltiples entidades
- ✅ Consultas masivas con `@Transactional(readOnly = true)`

#### 🔴 **REMOVER @Transactional:**

- ❌ Métodos `get*()`, `find*()` simples
- ❌ Métodos que solo mapean DTOs
- ❌ Métodos de validación
- ❌ **CRÍTICO**: Métodos llamados desde controllers SSE/streaming

---

## 🎯 SERVICIOS A REVISAR (Por Prioridad)

### **1. CRÍTICO - Servicios usados en SSE/Streaming**

- ✅ `UserServiceImpl.getCurrentUser()` - **YA CORREGIDO**
- ⏳ `NotificationServiceImpl` - Revisar métodos de lectura
- ⏳ `MaterialServiceImpl` - Si se usa en endpoints streaming

### **2. ALTA PRIORIDAD - Servicios con muchas transacciones**

- ⏳ `MaterialServiceImpl` - Revisar patrones read/write
- ⏳ `MovementServiceImpl` - Operaciones complejas
- ⏳ `ProductServiceImpl` - Creación con fases
- ⏳ `NotificationServiceImpl` - Mezcla read/write

### **3. MEDIA PRIORIDAD - Servicios especializados**

- ⏳ `RecipeServiceImpl`
- ⏳ `PackagingServiceImpl`
- ⏳ `SectorServiceImpl`
- ⏳ `ProductPhaseServiceImpl`

### **4. BAJA PRIORIDAD - Servicios simples**

- ⏳ `SystemConfigurationServiceImpl`

---

## 🚀 PLAN DE EJECUCIÓN

### **Paso 1: Test de Connection Leaks**

```bash
# Probar la aplicación actual
mvn spring-boot:run

# Abrir SSE endpoint múltiples veces
curl -H "Authorization: ..." http://localhost:8080/api/notifications/stream

# Verificar logs - NO debe aparecer:
# "Connection leak detection triggered"
```

### **Paso 2: Si persisten los leaks**

Revisar **NotificationServiceImpl** y otros servicios llamados desde controllers:

```java
// Buscar este patrón problemático:
@Controller
public class SomeController {
    public ResponseEntity<StreamingResponseBody> stream() {
        User user = userService.getCurrentUser();  // ✅ YA NO ES PROBLEMA
        SomeData data = someService.getData();     // ⚠️  REVISAR si tiene @Transactional
        // ...
    }
}
```

### **Paso 3: Aplicar correcciones sistemáticas**

Por cada `*ServiceImpl.java`:

1. **Listar todos los métodos con @Transactional**
2. **Clasificar**: Read-only simple vs Write vs Complex
3. **Remover** @Transactional de read-only simples
4. **Agregar** @Transactional a writes que no lo tengan
5. **Cambiar a** `@Transactional(readOnly = true)` en consultas masivas

### **Paso 4: Verificación final**

- ✅ No connection leaks en logs
- ✅ Operaciones de escritura funcionando (rollback en errores)
- ✅ Performance mantenido o mejorado

---

## 📊 MÉTRICAS DE ÉXITO

### **Antes (problemático):**

```
WARN: Connection leak detection triggered for PgConnection@...
Stack: UserServiceImpl.getCurrentUser -> NotificationController.streamNotifications
```

### **Después (esperado):**

```
INFO: Nueva conexión SSE: admin desde 127.0.0.1
DEBUG: Datos iniciales enviados: admin
📊 SSE Status: 1 conexiones, 1 usuarios, 0.05 MB memoria
```

---

## ⚡ PRÓXIMOS PASOS INMEDIATOS

1. **Probar aplicación actual** - Verificar si `getCurrentUser()` fix resuelve SSE leaks
2. **Si persisten leaks** - Revisar `NotificationServiceImpl` próximo
3. **Continuar con audit sistemático** de todos los servicios
4. **Documentar patrones** encontrados para futuros desarrolladores

**La corrección de `getCurrentUser()` debería resolver el 80% de los connection leaks relacionados con SSE.**
