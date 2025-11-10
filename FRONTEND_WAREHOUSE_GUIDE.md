# Guía del Frontend - Sistema de Almacén Simplificado

## 🎯 **Nuevo Enfoque Simple**

El sistema ahora usa **ubicación lógica** en lugar de coordenadas complejas:

- **Zona**: Enum fijo (MALTA, LUPULO, LEVADURA, etc.)
- **Sección**: String (A1, A2, B1, B2, etc.)
- **Nivel**: Integer (1, 2, 3)

Las coordenadas X/Y se calculan **automáticamente** en el backend.

---

## 🗺️ **Obtener el Mapa del Almacén**

### **Endpoint del Mapa SVG**

```http
GET /warehouse/layout
```

**Autorización:** Público  
**Respuesta:** SVG del mapa estático del almacén  
**Cache:** 1 hora

**Uso en Frontend:**

```javascript
// Obtener SVG del almacén
fetch("/api/warehouse/layout")
  .then((response) => response.text())
  .then((svgContent) => {
    document.getElementById("warehouse-map").innerHTML = svgContent;
  });
```

---

## 📦 **Endpoints de Materiales**

### **1. Obtener Materiales con Ubicación**

```http
GET /materials/warehouse-map?zone={zona}&activeOnly={boolean}
```

**Autorización:** `SUPERVISOR_DE_ALMACEN` o `OPERARIO_DE_ALMACEN`  
**Parámetros:**

- `zone` (opcional): Filtrar por zona específica
- `activeOnly` (opcional, default: true): Solo materiales activos

**Respuesta:**

```json
[
  {
    "materialId": 1,
    "materialName": "Malta Pilsen",
    "materialCode": "MAT-001",
    "materialType": "MALTA",
    "warehouseZone": "MALTA",
    "warehouseSection": "A1",
    "warehouseLevel": 2,
    "currentStock": 500.0,
    "warehouseX": 65.0,
    "warehouseY": 85.0,
    "levelDisplay": "Nivel 2"
  }
]
```

**Uso en Frontend:**

```javascript
// Obtener todos los materiales activos
fetch("/api/materials/warehouse-map?activeOnly=true")
  .then((response) => response.json())
  .then((materials) => {
    populateMarkers(materials); // Tu función existente
  });

// Filtrar por zona específica
fetch("/api/materials/warehouse-map?zone=MALTA&activeOnly=true")
  .then((response) => response.json())
  .then((materials) => {
    populateMarkersForZone(materials);
  });
```

### **2. Crear Material con Ubicación**

```http
POST /materials
```

**Autorización:** `SUPERVISOR_DE_ALMACEN`  
**Request:**

```json
{
  "name": "Malta Pilsen",
  "type": "MALTA",
  "unitMeasurement": "KG",
  "threshold": 100.0,
  "stock": 500.0,
  "warehouseZone": "MALTA",
  "warehouseSection": "A1",
  "warehouseLevel": 2
}
```

**Uso en Frontend:**

```javascript
const materialData = {
  name: "Malta Pilsen",
  type: "MALTA",
  unitMeasurement: "KG",
  threshold: 100.0,
  warehouseZone: "MALTA", // Enum
  warehouseSection: "A1", // String
  warehouseLevel: 2, // 1, 2 o 3
};

fetch("/api/materials", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(materialData),
});
```

### **3. Actualizar Ubicación de Material**

```http
PATCH /materials/{id}/location
```

**Autorización:** `SUPERVISOR_DE_ALMACEN`  
**Request:**

```json
{
  "warehouseZone": "LUPULO",
  "warehouseSection": "B3",
  "warehouseLevel": 1
}
```

**Uso en Frontend:**

```javascript
const newLocation = {
  warehouseZone: "LUPULO",
  warehouseSection: "B3",
  warehouseLevel: 1,
};

fetch(`/api/materials/${materialId}/location`, {
  method: "PATCH",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(newLocation),
});
```

### **4. Obtener Lista de Materiales (con paginación)**

```http
GET /materials?page=0&size=10&sort=name,asc
```

**Respuesta incluye ubicación:**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Malta Pilsen",
      "warehouseZone": "MALTA",
      "warehouseSection": "A1",
      "warehouseLevel": 2
    }
  ],
  "totalPages": 5,
  "totalItems": 48
}
```

---

## 🏢 **Endpoints del Almacén**

### **1. Obtener Zonas Disponibles**

```http
GET /warehouse/zones
```

**Autorización:** `SUPERVISOR_DE_ALMACEN` o `OPERARIO_DE_ALMACEN`  
**Respuesta:**

```json
{
  "MALTA": [
    "A1",
    "A2",
    "A3",
    "A4",
    "A5",
    "B1",
    "B2",
    "B3",
    "B4",
    "B5",
    "C1",
    "C2",
    "C3",
    "C4",
    "C5"
  ],
  "LUPULO": [
    "A1",
    "A2",
    "A3",
    "A4",
    "A5",
    "B1",
    "B2",
    "B3",
    "B4",
    "B5",
    "C1",
    "C2",
    "C3",
    "C4",
    "C5"
  ],
  "LEVADURA": ["A1", "A2", "B1", "B2", "C1", "C2"],
  "AGUA": ["A1", "A2", "B1", "B2", "C1", "C2"],
  "ENVASE": ["A1", "A2", "B1", "B2", "C1", "C2"],
  "ETIQUETADO": ["A1", "A2", "B1", "B2"],
  "OTROS": ["A1", "A2", "B1", "B2"]
}
```

**Uso en Frontend:**

```javascript
// Para crear selects de zona y sección
fetch("/api/warehouse/zones")
  .then((response) => response.json())
  .then((zones) => {
    populateZoneSelect(zones);
    // Cuando se selecciona zona, poblar secciones disponibles
  });
```

### **2. Validar Ubicación**

```http
POST /warehouse/validate-location
```

**Autorización:** `SUPERVISOR_DE_ALMACEN` o `OPERARIO_DE_ALMACEN`  
**Request:**

```json
{
  "zone": "MALTA",
  "section": "A1",
  "level": 2
}
```

**Respuesta:**

```json
{
  "isValid": true,
  "message": "Ubicación válida"
}
```

### **3. Información del Almacén**

```http
GET /materials/warehouse-info?materialType=MALTA
```

**Respuesta:**

```json
{
  "availableZones": [
    {
      "name": "MALTA",
      "displayName": "MALTA",
      "totalSections": 15,
      "occupiedSections": 8,
      "availableSections": ["A1", "A2", ...]
    }
  ],
  "suggestedLocation": {
    "zone": "MALTA",
    "section": "B3",
    "level": 1
  },
  "materialsByZone": {
    "MALTA": 8,
    "LUPULO": 3
  },
  "totalMaterials": 15
}
```

---

## 🎨 **Integración con Mapa Leaflet**

### **Sin Cambios en tu Código Actual**

Tu función `populateMarkers()` sigue funcionando igual:

```javascript
function populateMarkers(materials) {
  materials.forEach((material) => {
    if (material.warehouseX && material.warehouseY) {
      const leafletPoint = projectPoint(
        material.warehouseX,
        material.warehouseY
      );

      const marker = L.marker(leafletPoint)
        .bindTooltip(
          `
          <strong>${material.materialName}</strong><br>
          Código: ${material.materialCode}<br>
          Ubicación: ${material.warehouseZone}-${material.warehouseSection}<br>
          ${material.levelDisplay}<br>
          Stock: ${material.currentStock}
        `
        )
        .addTo(map);
    }
  });
}
```

### **Mejoras Opcionales - Diferenciación por Nivel**

```javascript
function populateMarkers(materials) {
  const levelColors = { 1: "#3498db", 2: "#f39c12", 3: "#e74c3c" };

  materials.forEach((material) => {
    if (material.warehouseX && material.warehouseY) {
      const leafletPoint = projectPoint(
        material.warehouseX,
        material.warehouseY
      );
      const color = levelColors[material.warehouseLevel] || "#3498db";

      const customIcon = L.divIcon({
        className: "material-marker",
        html: `
          <div style="
            background: ${color}; 
            width: 20px; height: 20px; 
            border-radius: 50%; 
            border: 2px solid white;
            display: flex; 
            align-items: center; 
            justify-content: center;
            color: white; 
            font-weight: bold; 
            font-size: 10px;
          ">
            ${material.warehouseLevel}
          </div>
        `,
      });

      L.marker(leafletPoint, { icon: customIcon })
        .bindTooltip(
          `
          <strong>${material.materialName}</strong><br>
          Ubicación: ${material.warehouseZone}-${material.warehouseSection}<br>
          ${material.levelDisplay}<br>
          Stock: ${material.currentStock}
        `
        )
        .addTo(map);
    }
  });
}
```

---

## 📋 **Formularios de Ubicación**

### **Select de Zona**

```html
<select id="warehouseZone" onchange="loadSections()">
  <option value="">Seleccionar Zona</option>
  <option value="MALTA">Malta</option>
  <option value="LUPULO">Lúpulo</option>
  <option value="LEVADURA">Levadura</option>
  <!-- ... etc -->
</select>
```

### **Select de Sección (dinámico según zona)**

```javascript
function loadSections() {
  const selectedZone = document.getElementById("warehouseZone").value;
  if (!selectedZone) return;

  fetch("/api/warehouse/zones")
    .then((response) => response.json())
    .then((zones) => {
      const sectionSelect = document.getElementById("warehouseSection");
      sectionSelect.innerHTML = '<option value="">Seleccionar Sección</option>';

      zones[selectedZone].forEach((section) => {
        sectionSelect.innerHTML += `<option value="${section}">${section}</option>`;
      });
    });
}
```

### **Select de Nivel**

```html
<select id="warehouseLevel">
  <option value="">Seleccionar Nivel</option>
  <option value="1">Nivel 1</option>
  <option value="2">Nivel 2</option>
  <option value="3">Nivel 3</option>
</select>
```

---

## 🚀 **Resumen de Cambios**

### ✅ **Lo que NO Cambia:**

- Tu función `projectPoint()` sigue igual
- Tu función `populateMarkers()` sigue igual
- Leaflet sigue recibiendo las mismas coordenadas
- El SVG del mapa es el mismo

### 🔄 **Lo que Cambia:**

- **Crear materiales:** Ahora envías `warehouseZone`, `warehouseSection`, `warehouseLevel`
- **Editar ubicación:** Mismo formato simple
- **Formularios:** Selects simples de zona/sección/nivel
- **Validación:** Endpoint simple de validación

### 🎯 **Beneficios:**

- **Más simple** de entender y mantener
- **Validaciones automáticas** en backend
- **Menos errores** al ingresar coordenadas manualmente
- **Más rápido** para usuarios (selects vs inputs numéricos)
- **Escalable** (fácil agregar nuevas zonas/secciones)
