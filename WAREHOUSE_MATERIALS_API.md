# API de Materiales con Gestión de Almacén

## Visión General

Esta API proporciona funcionalidad completa para la gestión de materiales en el sistema, incluyendo operaciones CRUD básicas y funcionalidades avanzadas de ubicación en el almacén.

## Autenticación y Autorización

La API utiliza Spring Security con roles específicos:

- **ADMIN**: Acceso completo al sistema
- **SUPERVISOR_DE_ALMACEN**: Gestión completa de materiales y ubicaciones
- **OPERARIO_DE_ALMACEN**: Consulta de información del almacén

## Endpoints de Materiales

### 1. Crear Material

**POST** `/materials`

**Autorización:** `SUPERVISOR_DE_ALMACEN`

**Descripción:** Registra un nuevo material en la base de datos asignándole un código único.

**Request Body:**

```json
{
  "name": "Malta Pilsen",
  "description": "Malta base para cerveza tipo Pilsen",
  "type": "MALTA",
  "phase": "MATERIA_PRIMA",
  "unitOfMeasure": "KG",
  "minimumStock": 100.0,
  "maximumStock": 1000.0,
  "warehouseZone": "MALTA",
  "warehouseSection": 1,
  "warehouseX": 125.0,
  "warehouseY": 75.0
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "code": "MAT-001",
  "name": "Malta Pilsen",
  "description": "Malta base para cerveza tipo Pilsen",
  "type": "MALTA",
  "phase": "MATERIA_PRIMA",
  "unitOfMeasure": "KG",
  "minimumStock": 100.0,
  "maximumStock": 1000.0,
  "currentStock": 0.0,
  "active": true,
  "creationDate": "2025-11-07T10:30:00",
  "warehouseZone": "MALTA",
  "warehouseSection": 1,
  "warehouseX": 125.0,
  "warehouseY": 75.0
}
```

### 2. Actualizar Material

**PATCH** `/materials/{id}`

**Autorización:** `SUPERVISOR_DE_ALMACEN`

**Descripción:** Permite modificar ciertos campos de un material registrado.

**Request Body:**

```json
{
  "name": "Malta Pilsen Premium",
  "description": "Malta base premium para cerveza tipo Pilsen",
  "minimumStock": 150.0,
  "maximumStock": 1200.0,
  "warehouseZone": "MALTA",
  "warehouseSection": 2,
  "warehouseX": 175.0,
  "warehouseY": 125.0
}
```

**Response (200 OK):** Mismo formato que la respuesta de creación.

### 3. Cambiar Estado de Material

**PATCH** `/materials/{id}/toggle-active`

**Autorización:** `SUPERVISOR_DE_ALMACEN`

**Descripción:** Cambia el estado del material (activo/inactivo).

**Response (200 OK):** Mismo formato que la respuesta de creación.

### 4. Obtener Materiales con Paginación

**GET** `/materials`

**Autorización:** Público

**Parámetros de Consulta:**

- `name` (opcional): Filtro por nombre
- `type` (opcional): Filtro por tipo de material
- `phase` (opcional): Filtro por fase
- `active` (opcional): Filtro por estado activo
- `warehouseZone` (opcional): Filtro por zona del almacén
- `page` (opcional, default: 0): Número de página
- `size` (opcional, default: 10): Tamaño de página
- `sort` (opcional, default: creationDate,desc): Ordenamiento

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "code": "MAT-001",
      "name": "Malta Pilsen",
      "description": "Malta base para cerveza tipo Pilsen",
      "type": "MALTA",
      "phase": "MATERIA_PRIMA",
      "unitOfMeasure": "KG",
      "minimumStock": 100.0,
      "maximumStock": 1000.0,
      "currentStock": 500.0,
      "active": true,
      "creationDate": "2025-11-07T10:30:00",
      "warehouseZone": "MALTA",
      "warehouseSection": 1,
      "warehouseX": 125.0,
      "warehouseY": 75.0
    }
  ],
  "currentPage": 0,
  "totalItems": 25,
  "totalPages": 3,
  "size": 10,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

### 5. Lista Simple de Materiales

**GET** `/materials/id-name-list`

**Autorización:** Público

**Parámetros de Consulta:**

- `name` (opcional): Filtro por nombre
- `active` (opcional): Filtro por estado activo
- `phase` (opcional): Filtro por fase
- `type` (opcional): Filtro por tipo

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "name": "Malta Pilsen",
    "code": "MAT-001"
  },
  {
    "id": 2,
    "name": "Lúpulo Cascade",
    "code": "MAT-002"
  }
]
```

### 6. Obtener Detalle de Material

**GET** `/materials/{id}`

**Autorización:** Público

**Response (200 OK):**

```json
{
  "id": 1,
  "code": "MAT-001",
  "name": "Malta Pilsen",
  "description": "Malta base para cerveza tipo Pilsen",
  "type": "MALTA",
  "phase": "MATERIA_PRIMA",
  "unitOfMeasure": "KG",
  "minimumStock": 100.0,
  "maximumStock": 1000.0,
  "currentStock": 500.0,
  "active": true,
  "creationDate": "2025-11-07T10:30:00",
  "lastUpdateDate": "2025-11-07T14:20:00",
  "warehouseZone": "MALTA",
  "warehouseSection": 1,
  "warehouseX": 125.0,
  "warehouseY": 75.0,
  "movements": [
    {
      "id": 10,
      "type": "ENTRADA",
      "quantity": 500.0,
      "date": "2025-11-07T12:00:00",
      "reason": "Compra inicial"
    }
  ]
}
```

## Endpoints de Gestión del Almacén

### 7. Obtener Mapa del Almacén

**GET** `/materials/warehouse-map`

**Autorización:** `SUPERVISOR_DE_ALMACEN` o `OPERARIO_DE_ALMACEN`

**Parámetros de Consulta:**

- `zone` (opcional): Filtrar por zona específica
- `activeOnly` (opcional, default: true): Solo materiales activos

**Response (200 OK):**

```json
[
  {
    "materialId": 1,
    "materialName": "Malta Pilsen",
    "materialCode": "MAT-001",
    "materialType": "MALTA",
    "warehouseZone": "MALTA",
    "warehouseSection": 1,
    "warehouseX": 125.0,
    "warehouseY": 75.0,
    "currentStock": 500.0,
    "unitOfMeasure": "KG"
  },
  {
    "materialId": 2,
    "materialName": "Lúpulo Cascade",
    "materialCode": "MAT-002",
    "materialType": "LUPULO",
    "warehouseZone": "LUPULO",
    "warehouseSection": 3,
    "warehouseX": 575.0,
    "warehouseY": 125.0,
    "currentStock": 25.0,
    "unitOfMeasure": "KG"
  }
]
```

### 8. Actualizar Ubicación de Material

**PATCH** `/materials/{id}/location`

**Autorización:** `SUPERVISOR_DE_ALMACEN`

**Request Body:**

```json
{
  "warehouseZone": "MALTA",
  "warehouseSection": 2,
  "warehouseX": 175.0,
  "warehouseY": 125.0
}
```

**Response (200 OK):** Mismo formato que la respuesta de creación de material.

### 9. Obtener Información del Almacén

**GET** `/materials/warehouse-info`

**Autorización:** `SUPERVISOR_DE_ALMACEN` o `OPERARIO_DE_ALMACEN`

**Parámetros de Consulta:**

- `materialType` (opcional): Tipo de material para sugerir zona

**Response (200 OK):**

```json
{
  "availableZones": [
    {
      "name": "MALTA",
      "totalSections": 4,
      "occupiedSections": 2,
      "recommendedForTypes": ["MALTA"]
    },
    {
      "name": "LUPULO",
      "totalSections": 4,
      "occupiedSections": 1,
      "recommendedForTypes": ["LUPULO"]
    }
  ],
  "suggestedLocation": {
    "warehouseZone": "MALTA",
    "warehouseSection": 3,
    "warehouseX": 125.0,
    "warehouseY": 175.0
  },
  "totalMaterials": 15,
  "materialsByZone": {
    "MALTA": 5,
    "LUPULO": 3,
    "LEVADURA": 2,
    "AGUA": 1,
    "ENVASE": 2,
    "ETIQUETADO": 1,
    "OTROS": 1
  }
}
```

## Tipos de Datos

### MaterialType (Enum)

- `MALTA`
- `LUPULO`
- `LEVADURA`
- `AGUA`
- `ENVASE`
- `ETIQUETA`
- `OTROS`

### Phase (Enum)

- `MATERIA_PRIMA`
- `PROCESO`
- `PRODUCTO_TERMINADO`

### Zonas del Almacén

- `MALTA`: Zona para almacenamiento de malta
- `LUPULO`: Zona para almacenamiento de lúpulo
- `LEVADURA`: Zona para levaduras
- `AGUA`: Zona para tratamiento de agua
- `ENVASE`: Zona de envases
- `ETIQUETADO`: Zona de etiquetado
- `OTROS`: Zona para otros materiales

## Validaciones

### Campos Obligatorios en Creación

- `name`: Nombre del material (máximo 100 caracteres)
- `type`: Tipo de material
- `phase`: Fase del material
- `unitOfMeasure`: Unidad de medida

### Campos Opcionales en Creación

- `description`: Descripción del material (máximo 500 caracteres)
- `minimumStock`: Stock mínimo (debe ser >= 0)
- `maximumStock`: Stock máximo (debe ser > minimumStock)
- `warehouseZone`: Zona del almacén
- `warehouseSection`: Sección dentro de la zona (1-4)
- `warehouseX`: Coordenada X en el almacén
- `warehouseY`: Coordenada Y en el almacén

### Validaciones de Ubicación

- Las coordenadas X e Y deben estar dentro de los límites de la zona especificada
- La sección debe existir dentro de la zona seleccionada
- No puede haber dos materiales en las mismas coordenadas exactas

## Códigos de Error

### 400 Bad Request

```json
{
  "timestamp": "2025-11-07T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "name": "El nombre es obligatorio",
    "warehouseX": "Las coordenadas X deben estar dentro de los límites de la zona"
  }
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-11-07T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Material no encontrado con id: 999"
}
```

### 409 Conflict

```json
{
  "timestamp": "2025-11-07T15:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un material en esas coordenadas del almacén"
}
```

## Notas de Implementación

1. **Coordenadas del Almacén**: El sistema utiliza un sistema de coordenadas basado en píxeles donde (0,0) está en la esquina superior izquierda del almacén.

2. **Sugerencias Automáticas**: El sistema puede sugerir ubicaciones automáticamente basándose en el tipo de material y las zonas disponibles.

3. **Validación en Tiempo Real**: Las ubicaciones se validan contra la configuración actual del almacén para evitar conflictos.

4. **Historial de Movimientos**: Cada cambio de ubicación se registra en el historial de movimientos del material.

5. **Sincronización Visual**: Las coordenadas se sincronizan automáticamente con la visualización SVG del almacén.
