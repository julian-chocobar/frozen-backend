# 📋 ANÁLISIS COMPLETO: CUÁNDO USAR @Transactional

## 🎯 REGLAS DE ORO PARA @Transactional

### ✅ **CUÁNDO SÍ USAR @Transactional**

#### 1. **OPERACIONES DE ESCRITURA (CUD - Create, Update, Delete)**

```java
@Transactional
public MaterialResponseDTO createMaterial(MaterialCreateDTO dto) {
    Material material = materialMapper.toEntity(dto);
    material.setCreationDate(OffsetDateTime.now());
    Material saved = materialRepository.save(material);  // ✅ ESCRITURA
    return materialMapper.toResponseDTO(saved);
}

@Transactional
public void deleteMaterial(Long id) {
    materialRepository.deleteById(id);  // ✅ ESCRITURA
}

@Transactional
public MaterialResponseDTO updateMaterial(Long id, MaterialCreateDTO dto) {
    Material material = materialRepository.findById(id).orElseThrow();
    material.setName(dto.getName());
    return materialMapper.toResponseDTO(materialRepository.save(material));  // ✅ ESCRITURA
}
```

#### 2. **OPERACIONES MÚLTIPLES QUE DEBEN SER ATÓMICAS**

```java
@Transactional
public void processMovement(MovementCreateDTO dto) {
    // ✅ MÚLTIPLES OPERACIONES que deben ser ATÓMICAS
    Movement movement = movementRepository.save(createMovement(dto));
    updateMaterialStock(dto.getMaterialId(), dto.getQuantity());
    notificationService.createMovementNotification(movement);
    // Si cualquiera falla, TODO se revierte
}

@Transactional
public ProductResponseDTO createProductWithPhases(ProductCreateDTO dto) {
    // ✅ MÚLTIPLES ENTIDADES relacionadas
    Product product = productRepository.save(mapToEntity(dto));
    createDefaultPhases(product);  // Crear fases automáticas
    return mapToResponseDTO(product);
}
```

#### 3. **CONSULTAS DE SOLO LECTURA (con @Transactional(readOnly = true))**

```java
@Transactional(readOnly = true)
public List<MaterialResponseDTO> getAllMaterials() {
    // ✅ Solo lectura - optimiza performance
    return materialRepository.findAll().stream()
            .map(materialMapper::toResponseDTO)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable) {
    // ✅ Solo lectura con paginación
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(notificationMapper::toResponseDTO);
}
```

---

### ❌ **CUÁNDO NO USAR @Transactional**

#### 1. **CONSULTAS SIMPLES DE UNA SOLA ENTIDAD**

```java
// ❌ NO NECESARIO - Spring Data maneja automáticamente
public MaterialResponseDTO findById(Long id) {
    Material material = materialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
    return materialMapper.toResponseDTO(material);
}

// ❌ NO NECESARIO - consulta simple
public User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
}
```

#### 2. **MÉTODOS QUE SOLO HACEN MAPEO/LÓGICA DE NEGOCIO**

```java
// ❌ NO NECESARIO - solo mapeo
public MaterialResponseDTO mapToResponseDTO(Material material) {
    return materialMapper.toResponseDTO(material);
}

// ❌ NO NECESARIO - solo validación
public void validateMaterialData(MaterialCreateDTO dto) {
    if (dto.getStock() < 0) {
        throw new ValidationException("Stock no puede ser negativo");
    }
}

// ❌ NO NECESARIO - solo cálculos
public Double calculateTotalCost(List<Material> materials) {
    return materials.stream()
            .mapToDouble(m -> m.getValue() * m.getStock())
            .sum();
}
```

#### 3. **MÉTODOS LLAMADOS DESDE CONTROLLERS (ESPECIAL CUIDADO)**

```java
// 🚨 PROBLEMÁTICO en Controllers que devuelven SseEmitter o streaming
@Transactional(readOnly = true)  // ← PUEDE CAUSAR CONNECTION LEAK
public User getCurrentUser() {
    // Si el controller nunca termina (ej: SSE), la transacción nunca se cierra
    return userRepository.findByUsername(getCurrentUsername())
            .orElseThrow();
}

// ✅ MEJOR ALTERNATIVA para controllers
public User getCurrentUser() {
    // Sin @Transactional - Spring Data maneja automáticamente
    // La transacción se abre y cierra inmediatamente
    return userRepository.findByUsername(getCurrentUsername())
            .orElseThrow();
}
```

---

## 🔧 PROBLEMAS EN NUESTRO CÓDIGO ACTUAL

### ❌ **CASOS PROBLEMÁTICOS IDENTIFICADOS:**

#### 1. **UserServiceImpl.getCurrentUser()**

```java
// 🚨 PROBLEMÁTICO - usado en SSE endpoints
@Transactional(readOnly = true)
public User getCurrentUser() {
    // La transacción se mantiene abierta si el controller no termina
    return userRepository.findByUsername(auth.getName()).orElseThrow();
}

// ✅ SOLUCIÓN
public User getCurrentUser() {
    // Sin @Transactional - transacción automática muy corta
    return userRepository.findByUsername(auth.getName()).orElseThrow();
}
```

#### 2. **Métodos de consulta simple con @Transactional innecesario**

```java
// ❌ INNECESARIO en UserServiceImpl
public UserDetailDTO getUserByUsername(String username) {
    // Spring Data ya maneja la transacción automáticamente
    User user = userRepository.findByUsername(username).orElseThrow();
    return userMapper.toUserDetailDTO(user);
}

public UserDetailDTO getUserById(Long id) {
    // Spring Data ya maneja la transacción automáticamente
    User user = userRepository.findById(id).orElseThrow();
    return userMapper.toUserDetailDTO(user);
}
```

---

## 📊 CLASIFICACIÓN DE MÉTODOS POR CATEGORÍA

### 🟢 **CORRECTOS (mantener @Transactional)**

- `createMaterial()` - Escritura + lógica de negocio
- `updateMaterial()` - Escritura
- `deleteMaterial()` - Escritura
- `processMovement()` - Múltiples operaciones atómicas
- `getAllMaterials()` - Lectura masiva con `readOnly=true`

### 🟡 **REVISAR (posiblemente innecesario)**

- Métodos de consulta simple de una sola entidad
- Métodos que solo mapean DTOs
- Métodos de validación pura

### 🔴 **PROBLEMÁTICOS (remover @Transactional)**

- `getCurrentUser()` - Usado en SSE endpoints
- Métodos de consulta simple llamados desde controllers de streaming
- Cualquier método llamado en endpoints que no terminan inmediatamente

---

## 🎯 PLAN DE ACCIÓN RECOMENDADO

### 1. **REMOVER @Transactional DE CONSULTAS SIMPLES**

```java
// UserServiceImpl - remover de métodos de lectura simple
getCurrentUser()           // ← CRÍTICO para SSE
getUserByUsername()        // ← Innecesario
getUserById()             // ← Innecesario
```

### 2. **MANTENER @Transactional EN ESCRITURAS**

```java
// Mantener en todos los métodos que:
createUser()              // ✅ Escritura + hash password
updateUser()              // ✅ Escritura
deleteUser()              // ✅ Escritura
```

### 3. **USAR readOnly=true SOLO EN CONSULTAS MASIVAS**

```java
@Transactional(readOnly = true)
public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
    // ✅ Consulta masiva se beneficia de readOnly
}
```

### 4. **ALTERNATIVA PARA CONTROLLERS CRÍTICOS**

```java
// En lugar de inyectar UserService en SSE controllers
@Autowired SecurityContextHolder securityContext;

private String getCurrentUsername() {
    // ✅ Sin DB, sin transacciones
    return securityContext.getContext().getAuthentication().getName();
}
```

---

## ⚡ REGLA FINAL PARA CONNECTION LEAKS

> **NUNCA usar @Transactional en métodos que puedan ser llamados desde:**
>
> - Endpoints SSE (`/stream`)
> - Controllers que devuelven streaming responses
> - Operaciones de larga duración
> - Endpoints que mantienen conexiones abiertas

La transacción debe **abrirse, ejecutarse y cerrarse inmediatamente**. Si el contexto de ejecución (como un controller) se mantiene vivo, la transacción nunca se cierra → Connection Leak.
