# Documentación de Endpoints de Analytics

## 1. **GET /analytics/monthly-production**
### Producción Mensual

**Propósito**: Obtener estadísticas de producción agrupadas por mes.

**Parámetros de Query**:
- `startDate` (opcional): Fecha de inicio del rango (formato: `YYYY-MM-DD`)
- `endDate` (opcional): Fecha de fin del rango (formato: `YYYY-MM-DD`)
- `productId` (opcional): ID del producto específico a filtrar

**Comportamiento por defecto**: Si no se especifican fechas, retorna datos del último año.

**Casos de uso para el frontend**:
- Gráficos de línea/barras mostrando producción mensual total
- Filtrar por producto específico para análisis individual
- Comparativas de producción entre diferentes períodos
- Dashboard de producción con rangos personalizados

**Ejemplo de Request**:
```
GET /analytics/monthly-production?startDate=2025-01-01&endDate=2025-11-21&productId=5
```

---

## 2. **GET /analytics/monthly-material-consumption**
### Consumo de Materiales por Mes

**Propósito**: Obtener el consumo de materiales agrupado por mes.

**Parámetros de Query**:
- `startDate` (opcional): Fecha de inicio del rango
- `endDate` (opcional): Fecha de fin del rango
- `materialId` (opcional): ID del material específico a filtrar

**Comportamiento por defecto**: Retorna consumo del último año si no hay filtros.

**Casos de uso para el frontend**:
- Gráficos de tendencia de consumo de materiales
- Análisis de material específico (ej: cuánto papel film se usa mensualmente)
- Proyecciones de compra de materiales
- Alertas de consumo anormal
- Comparar consumo entre diferentes materiales

**Ejemplo de Request**:
```
GET /analytics/monthly-material-consumption?materialId=12
```

---

## 3. **GET /analytics/monthly-waste**
### Desperdicios Mensuales

**Propósito**: Obtener estadísticas de desperdicios generados por mes.

**Parámetros de Query**:
- `startDate` (opcional): Fecha de inicio del rango
- `endDate` (opcional): Fecha de fin del rango
- `phase` (opcional): Fase específica del proceso (enum: `Phase`)
- `transferOnly` (opcional, default: `false`): Si es `true`, solo muestra desperdicios de transferencias entre fases

**Comportamiento por defecto**: Retorna desperdicios del último año.

**Casos de uso para el frontend**:
- Gráficos de desperdicios por fase del proceso
- Identificar fases con mayor desperdicio
- Filtrar solo desperdicios de transferencias (mermas por traslado)
- Análisis de eficiencia por fase
- Comparativas mes a mes para ver mejoras

**Ejemplo de Request**:
```
GET /analytics/monthly-waste?phase=CONGELADO&transferOnly=true
```

---

## 4. **GET /analytics/dashboard/monthly**
### Resumen del Último Mes

**Propósito**: Obtener un resumen consolidado de métricas del último mes.

**Parámetros**: Ninguno (automático para el último mes)

**Casos de uso para el frontend**:
- Dashboard principal con KPIs del mes actual
- Cards de resumen con datos clave
- Vista rápida sin necesidad de filtros
- Página de inicio con métricas actuales

**Ejemplo de Request**:
```
GET /analytics/dashboard/monthly
```

---

## Estructura de Respuesta Común (MonthlyTotalDTO)

Los endpoints 1, 2 y 3 retornan un array de objetos `MonthlyTotalDTO`:

```json
[
  {
    "year": 2025,
    "month": 11,
    "total": 1250.50,
    "monthName": "Noviembre"
  },
  {
    "year": 2025,
    "month": 10,
    "total": 1180.25,
    "monthName": "Octubre"
  }
]
```

### Campos:
- `year`: Año de la métrica
- `month`: Número del mes (1-12)
- `total`: Valor total acumulado para ese mes
- `monthName`: Nombre del mes (opcional, para facilitar visualización)

---

## Estructura de Respuesta (DashboardStatsDTO)

El endpoint 4 retorna un objeto `DashboardStatsDTO` con estadísticas consolidadas del mes actual.

---

## Recomendaciones de Implementación Frontend

### 1. **Componentes de Filtros Reutilizables**

Crear un componente de filtro de fechas que pueda ser reutilizado en todos los endpoints:

```typescript
// Componente de filtro de fechas para todos los endpoints
<DateRangeFilter 
  onDateChange={(start, end) => fetchData(start, end)}
/>

// Selector de producto/material/fase según el endpoint
<EntitySelector 
  type="product" // o "material", "phase"
  onSelect={(id) => fetchData(null, null, id)}
/>
```

### 2. **Gráficos Sugeridos**

- **Producción (`/monthly-production`)**: 
  - Gráfico de líneas con tendencia
  - Gráfico de barras comparativo entre productos
  
- **Consumo (`/monthly-material-consumption`)**: 
  - Gráfico de barras apiladas por material
  - Gráfico de área para ver tendencias
  
- **Desperdicios (`/monthly-waste`)**: 
  - Gráfico de columnas agrupadas por fase
  - Gráfico de torta para ver distribución por fase
  - Toggle para vista con/sin transferencias

- **Dashboard (`/dashboard/monthly`)**: 
  - Cards con KPIs principales
  - Mini gráficos sparkline para tendencias rápidas

### 3. **Estados de Carga**

Implementar skeleton loaders mientras se cargan los datos de los rangos personalizados:

```jsx
{isLoading ? (
  <SkeletonChart />
) : (
  <Chart data={monthlyData} />
)}
```

### 4. **Caché Inteligente**

Estrategias de caché recomendadas:

- Cachear datos del último año (consulta por defecto) por 5-10 minutos
- Invalidar caché al cambiar filtros manualmente
- Usar React Query o SWR para manejo automático de caché

```typescript
const { data, isLoading } = useQuery(
  ['monthly-production', startDate, endDate, productId],
  () => fetchMonthlyProduction(startDate, endDate, productId),
  { 
    staleTime: 5 * 60 * 1000, // 5 minutos
    cacheTime: 10 * 60 * 1000 // 10 minutos
  }
);
```

### 5. **Comparativas de Períodos**

Usar dos llamadas con diferentes rangos para comparar períodos:

```javascript
// Comparar producción año actual vs año anterior
const currentYear = await getMonthlyProduction('2025-01-01', '2025-12-31');
const previousYear = await getMonthlyProduction('2024-01-01', '2024-12-31');

// Calcular diferencia porcentual
const growthRate = ((currentTotal - previousTotal) / previousTotal) * 100;
```

### 6. **Manejo de Errores**

Implementar manejo de errores consistente:

```typescript
try {
  const data = await fetchAnalytics(params);
  setData(data);
} catch (error) {
  if (error.status === 400) {
    showError('Parámetros inválidos');
  } else if (error.status === 500) {
    showError('Error del servidor, intente más tarde');
  }
}
```

### 7. **Exportación de Datos**

Agregar funcionalidad para exportar datos:

```typescript
const exportToCSV = (data: MonthlyTotalDTO[]) => {
  const csv = convertToCSV(data);
  downloadFile(csv, 'analytics-export.csv');
};
```

### 8. **Filtros Predefinidos**

Ofrecer rangos de tiempo predefinidos para facilitar el uso:

```typescript
const presetRanges = {
  'Este Mes': [startOfMonth, today],
  'Últimos 3 Meses': [threeMonthsAgo, today],
  'Este Año': [startOfYear, today],
  'Año Anterior': [startOfLastYear, endOfLastYear],
  'Últimos 12 Meses': [oneYearAgo, today]
};
```

### 9. **Responsividad**

- En móvil: Mostrar gráficos simplificados y filtros en modal/drawer
- En tablet: Gráficos de tamaño medio con filtros laterales
- En desktop: Dashboard completo con múltiples gráficos simultáneos

### 10. **Optimización de Llamadas**

```typescript
// Debounce para evitar múltiples llamadas al cambiar filtros
const debouncedFetch = useDebouncedCallback(
  (filters) => fetchAnalytics(filters),
  500
);
```

---

## Ejemplos de Integración

### React + TypeScript

```typescript
interface MonthlyTotalDTO {
  year: number;
  month: number;
  total: number;
  monthName?: string;
}

const useMonthlyProduction = (
  startDate?: string, 
  endDate?: string, 
  productId?: number
) => {
  return useQuery<MonthlyTotalDTO[]>(
    ['monthly-production', startDate, endDate, productId],
    async () => {
      const params = new URLSearchParams();
      if (startDate) params.append('startDate', startDate);
      if (endDate) params.append('endDate', endDate);
      if (productId) params.append('productId', productId.toString());
      
      const response = await fetch(
        `/analytics/monthly-production?${params.toString()}`
      );
      
      if (!response.ok) throw new Error('Error fetching data');
      return response.json();
    }
  );
};
```

### Vue 3 + Composition API

```typescript
const monthlyProduction = ref<MonthlyTotalDTO[]>([]);
const isLoading = ref(false);

const fetchMonthlyProduction = async (
  startDate?: string,
  endDate?: string,
  productId?: number
) => {
  isLoading.value = true;
  try {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (productId) params.append('productId', productId.toString());
    
    const response = await axios.get(
      `/analytics/monthly-production?${params.toString()}`
    );
    monthlyProduction.value = response.data;
  } catch (error) {
    console.error('Error fetching analytics:', error);
  } finally {
    isLoading.value = false;
  }
};
```

---

## Notas Adicionales

### Consideraciones de Rendimiento

- Los endpoints pueden devolver hasta 12 meses de datos por defecto
- Considerar paginación o agregación para rangos muy amplios
- Implementar virtualización para listas largas de datos

### Internacionalización (i18n)

- Los nombres de meses pueden venir en español del backend
- Considerar formatear fechas según locale del usuario
- Traducir etiquetas de fases si es necesario

### Accesibilidad

- Asegurar que los gráficos tengan texto alternativo
- Proveer tablas de datos como alternativa a gráficos visuales
- Usar colores con suficiente contraste para usuarios con daltonismo
