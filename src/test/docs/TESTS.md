# Documentación de Tests - Frozen Backend

Este documento describe todos los tests del proyecto organizados por módulo funcional.

---

## E2E (End-to-End)

### ProductionFlowE2ETest

Tests end-to-end que validan flujos completos del sistema de producción cervecera atravesando múltiples módulos.

- **`completeProductionFlow_happyPath()`**: Testea la creación exitosa de una orden de producción con validación de batch automático, reserva de materiales (malta 100kg, lúpulo 3kg, levadura 2kg, agua 400L), decrementación de stock disponible y generación de movimientos tipo RESERVA.

- **`completeProductionFlow_multipleOrdersWithStockReservation()`**: Testea la creación de 3 órdenes de producción consecutivas (500L, 1000L, 500L) validando acumulación de stock reservado (malta: 100+200+100=400kg, lúpulo: 3+6+3=12kg), decrementación progresiva del stock disponible y generación de 12+ movimientos tipo RESERVA.

- **`multipleOrders_stockCompetition()`**: Testea la competencia por stock limitado entre órdenes concurrentes donde Orden 1 reserva 80kg de 100kg disponibles exitosamente y Orden 2 que necesita 40kg es rechazada por stock insuficiente (solo quedan 20kg), validando rollback correcto sin modificación de stock y creación únicamente de Orden 1.

- **`orderCreation_failsWhenInsufficientStock()`**: Testea el rechazo de orden de producción cuando no hay stock suficiente (requiere 1000kg con solo 10kg disponibles) validando que no se creó orden ni se reservó stock con rollback transaccional completo.

---

## Analytics

### Service Tests

#### AnalyticsServiceImplTest

Tests unitarios del servicio de analytics validando lógica de agregaciones mensuales y estadísticas del dashboard.

- **`getMonthlyProductionWithDatesAndProductId()`**: Testea la consulta de producción mensual con rango de fechas y filtro por producto verificando llamadas a repositorio con parámetros correctos.
- **`getMonthlyProductionDefaultsToLastYearWhenDatesNull()`**: Testea el uso de último año por defecto cuando las fechas son null.
- **`getMonthlyMaterialConsumptionWithDatesAndMaterialId()`**: Testea la consulta de consumo mensual de materiales con filtro opcional por material.
- **`getMonthlyMaterialConsumptionDefaultsToLastYearWhenDatesNull()`**: Testea el uso de rango por defecto del último año cuando las fechas son null.
- **`getMonthlyWasteWithPhase()`**: Testea la consulta de desperdicios mensuales filtrando por fase específica (productWaste + movementWaste).
- **`getMonthlyWasteMovementOnly()`**: Testea la consulta de desperdicios solo de movimientos ignorando fase.
- **`getDashboardStats()`**: Testea la obtención de estadísticas agregadas del último mes (producción total, desperdicios, materiales usados, lotes en progreso/completados/cancelados, órdenes rechazadas).

### Repository Tests

#### AnalyticsRepositoryTest

Tests de consultas JPA complejas para métricas agregadas del dashboard.

- **`getDashboardDataReturnsAggregatedMetrics()`**: Testea el cálculo de 7 métricas (totalProduced, totalWaste, totalMaterialsUsed, batchesInProgress, batchesCompleted, batchesCancelled, ordersRejected) con fixtures completas de materiales, packaging, lotes, fases de producción y órdenes.

### Controller Tests

#### AnalyticsControllerTest

Tests del controlador REST de analytics validando endpoints GET.

- **`getMonthlyProductionReturnsOk()`**: Testea la obtención exitosa de producción mensual retornando 200 y array de MonthlyTotalDTO con parámetros startDate, endDate y productId.
- **`getMonthlyMaterialConsumptionReturnsOk()`**: Testea la obtención exitosa de consumo mensual de materiales retornando 200 con filtros opcionales.
- **`getMonthlyWasteReturnsOk()`**: Testea la obtención exitosa de desperdicios mensuales retornando 200 con parámetros phase y transferOnly.
- **`getMonthlyDashboardReturnsOk()`**: Testea la obtención exitosa de estadísticas del dashboard retornando 200 con DashboardStatsDTO completo.

#### AnalyticsControllerSecurityTest

Tests de seguridad del controlador de analytics.

- **`getMonthlyProduction_unauthenticated_returns401()`**: Testea el retorno de 401 sin autenticación al consultar producción mensual.
- **`getMonthlyProduction_authenticated_returns200()`**: Testea el retorno de 200 con autenticación válida al consultar producción mensual.
- **`getMonthlyMaterialConsumption_unauthenticated_returns401()`**: Testea el retorno de 401 sin autenticación al consultar consumo de materiales.
- **`getMonthlyMaterialConsumption_authenticated_returns200()`**: Testea el retorno de 200 con autenticación válida al consultar consumo de materiales.
- **`getMonthlyWaste_unauthenticated_returns401()`**: Testea el retorno de 401 sin autenticación al consultar desperdicios.
- **`getMonthlyWaste_authenticated_returns200()`**: Testea el retorno de 200 con autenticación válida al consultar desperdicios.
- **`getDashboardMonthly_unauthenticated_returns401()`**: Testea el retorno de 401 sin autenticación al consultar dashboard.
- **`getDashboardMonthly_authenticated_returns200()`**: Testea el retorno de 200 con autenticación válida al consultar dashboard.

---

## Batches

### Service Tests

#### BatchServiceImplTest

Tests unitarios de la lógica de negocio del servicio de lotes.

- **`canChangeBatchStatus()`**: Testea la modificación exitosa del estado de un lote.
- **`batchCodeShouldNotBeNull()`**: Testea la validación de campo obligatorio para el código de lote.
- **`cancelBatch_byId_success()`**: Testea la cancelación exitosa de un lote por ID actualizando estado a CANCELADO y suspendiendo fases pendientes.
- **`cancelBatch_byId_notFound_throws()`**: Testea el lanzamiento de ResourceNotFoundException cuando el lote a cancelar no existe.
- **`cancelBatch_byEntity_withPendingPhases_suspendsThem()`**: Testea la cancelación de lote usando entidad directamente suspendiendo todas las fases pendientes.
- **`cancelBatch_noMaterials_createsEmptyMovementsList()`**: Testea la creación de lista vacía de movimientos cuando no hay materiales asociados a las fases.
- **`processBatchesForToday_nonWorkingDay_postponesAll()`**: Testea el aplazamiento de lotes cuando el día no es laborable ajustando startDate.
- **`processBatchesForToday_limitedCapacity_postponesRemaining()`**: Testea el inicio solo de los lotes que caben en capacidad disponible de sectores.
- **`processBatchesForToday_noSectors_postponesAll()`**: Testea el aplazamiento de todos los lotes cuando no hay sectores disponibles.
- **`startNextPhase_success()`**: Testea el avance exitoso a la siguiente fase pendiente asignando sector y actualizando estado a EN_PROCESO.
- **`startNextPhase_lastPhase_completesBatch()`**: Testea el comportamiento al completar última fase lanzando NoSuchElementException por Optional mal gestionado.
- **`startNextPhase_noSector_throws()`**: Testea el lanzamiento de BadRequestException cuando no hay sectores aptos para la fase siguiente.
- **`createBatch_missingProduct_throws()`**: Testea el lanzamiento de excepción cuando el input está incompleto.
- **`createBatch_withPackaging_calculatesQuantityAndSaves()`**: Testea el cálculo de cantidad y persistencia mockeando repositorios necesarios.
- **`createBatch_withProductPhases_createsProductionPhases()`**: Testea la generación de ProductionPhase a partir de Product.phases calculando standardInput/standardOutput.
- **`startNextPhase_noSectors_throws()`**: Testea el lanzamiento de BadRequestException cuando no hay sectores disponibles.
- **`startNextPhase_withSector_updatesPhaseAndNotifies()`**: Testea la asignación de sector actualizando estado a EN_PROCESO y persistiendo la fase.
- **`completeBatch_setsFinalQuantityAndStatus()`**: Testea la actualización correcta de finalQuantity y status.
- **`calculateBatchQuantity_validInputs_returnsExpected()`**: Testea el cálculo correcto de cantidad de lote.
- **`calculateBatchQuantity_invalid_throws()`**: Testea el lanzamiento de BadRequestException cuando inputs son nulos.
- **`roundToDecimals_roundsCorrectly()`**: Testea el redondeo correcto a 3 decimales.

#### BatchServiceEstimationTest

Tests de integración del servicio de estimación de tiempo de producción.

- **`estimateEndDate_withActivePhases()`**: Testea el cálculo de fecha de fin para fases activas cortas dentro del mismo día laborable.
- **`estimateEndDate_realBeerProduction()`**: Testea el escenario completo de producción de cerveza con múltiples fases.
- **`estimateEndDate_startingWeekend()`**: Testea el movimiento de fecha al siguiente lunes cuando el inicio es fin de semana.
- **`estimateEndDate_withDecimalHours()`**: Testea los cálculos con horas decimales para mayor precisión.
- **`estimateEndDate_multipleWeeks()`**: Testea la validación de fases que abarcan múltiples semanas con periodos de fermentación largos.

#### BatchTraceabilityServiceTest

Tests unitarios del servicio de trazabilidad de lotes.

- **`getBatchTraceabilityData_batchNotFound_throws()`**: Testea el lanzamiento de ResourceNotFoundException cuando el lote no existe.
- **`getBatchTraceabilityData_noProductionOrder_throws()`**: Testea el lanzamiento de ResourceNotFoundException cuando el lote no tiene orden de producción asociada.
- **`generateTraceabilityPDF_withMinimalData_returnsBytes()`**: Testea la generación de PDF con datos mínimos retornando bytes válidos.

### Repository Tests

#### BatchRepositoryTest

Tests de consultas JPA personalizadas del repositorio de lotes.

- **Query `findAllStartingToday`**: Testea la consulta de lotes que inician hoy con fixtures mínimas en H2.

### Mapper Tests

#### BatchMapperTest

Tests de transformación MapStruct entre entidades Batch y DTOs.

- **`mapsBatchToResponseDTO()`**: Testea el mapeo correcto de todos los campos de la entidad Batch al DTO de respuesta.

### Controller Tests

#### BatchControllerTest

Tests del controlador de lotes validando endpoints REST.

- **`getAllBatchesReturnsOk()`**: Testea la obtención exitosa de listado de lotes retornando status 200.
- **`getBatchByIdReturnsOk()`**: Testea la obtención exitosa de lote por ID retornando 200.
- **`cancelBatch_success_returns200()`**: Testea la cancelación exitosa de lote retornando 200 y DTO con estado CANCELADO.
- **`cancelBatch_notFound_returns404()`**: Testea el retorno de 404 cuando el lote a cancelar no existe.
- **`processBatchesForToday_success_returns204()`**: Testea el disparo de procesamiento diario retornando 204.
- **`getBatches_withFilters_returns200()`**: Testea la obtención de lotes con filtros de estado y paginación retornando 200 con metadata correcta.

#### BatchControllerSecurityTest

Tests de seguridad del controlador de lotes.

- **`getAllBatchesRequiresAuth()`**: Testea el retorno de 401 Unauthorized cuando no hay autenticación.
- **`getAllBatchesWithValidAuthReturnsOk()`**: Testea la obtención exitosa con autenticación válida retornando 200 OK.

### Integration Tests

#### BatchIntegrationTest

Tests de integración completos con base de datos.

- **`getBatchById_happyPath()`**: Testea el flujo completo de crear lote, recuperarlo por ID y verificar datos.
- **`listBatches_happyPath()`**: Testea la creación de múltiples lotes y obtención de listado paginado.
- **`listBatches_withPagination()`**: Testea el funcionamiento correcto de parámetros de paginación (page, size, sort).
- **`cancelBatch_fullFlow_suspendsPhasesAndReturnsMaterials()`**: Testea la cancelación de lote suspendiendo fases pendientes y generando movimientos INGRESO de devolución.
- **`processBatchesForToday_manual_startsScheduledBatches()`**: Testea la accesibilidad del endpoint de procesamiento manual.
- **`getBatches_withStatusFilter_filtersCorrectly()`**: Testea el filtrado exitoso por estado excluyendo lotes que no coinciden con el criterio.

---

## Common

### Validation Tests

#### FutureOrPresentDateValidatorTest

Tests unitarios del validador personalizado @FutureOrPresentDate para validación de fechas.

- **`isValid_nullValue_returnsTrue()`**: Testea la validación exitosa cuando el valor es null (campo opcional).
- **`isValid_todayDate_returnsTrue()`**: Testea la validación exitosa de fecha de hoy.
- **`isValid_futureDate_returnsTrue()`**: Testea la validación exitosa de fecha futura (5 días adelante).
- **`isValid_pastDate_returnsFalse()`**: Testea el rechazo de fecha pasada (1 día atrás).
- **`isValid_yesterdayDate_returnsFalse()`**: Testea el rechazo de fecha de ayer.
- **`isValid_tomorrowDate_returnsTrue()`**: Testea la validación exitosa de fecha de mañana.
- **`isValid_startOfToday_returnsTrue()`**: Testea la validación exitosa del inicio del día actual (00:00:00).
- **`isValid_endOfToday_returnsTrue()`**: Testea la validación exitosa del fin del día actual (23:59:59).
- **`isValid_differentTimezone_handlesCorrectly()`**: Testea el manejo correcto de fechas en diferentes zonas horarias (UTC).
- **`isValid_farPastDate_returnsFalse()`**: Testea el rechazo de fecha muy antigua (1 año atrás).
- **`isValid_farFutureDate_returnsTrue()`**: Testea la validación exitosa de fecha muy futura (1 año adelante).

### Utils Tests

#### DateUtilTest

Tests exhaustivos del utilitario DateUtil para estimación de fechas de fin de producción.

- **`estimateEndDate_singleActivePhase_withinSameDay()`**: Testea el cálculo de fase activa de 4h iniciando a las 10:00 terminando el mismo día a las 14:00.
- **`estimateEndDate_singleActivePhase_spansMultipleDays()`**: Testea el cálculo de fase activa de 20h que cruza múltiples días excluyendo noches y fines de semana.
- **`estimateEndDate_singlePassivePhase_ignoresWorkingHours()`**: Testea el cálculo de fase pasiva de 48h funcionando 24/7 (fermenta durante noche y fin de semana).
- **`estimateEndDate_multiplePhasesActiveAndPassive()`**: Testea la combinación correcta de fases activas y pasivas.
- **`estimateEndDate_startBeforeWorkingHours_alignsToOpeningTime()`**: Testea la alineación a 08:00 (apertura) cuando inicio es 06:00.
- **`estimateEndDate_startAfterWorkingHours_movesToNextDay()`**: Testea el movimiento al siguiente día 08:00 cuando inicio es 19:00 (después de cierre).
- **`estimateEndDate_startOnWeekend_movesToMonday()`**: Testea el movimiento al lunes 08:00 cuando inicio es sábado/domingo.
- **`estimateEndDate_phaseSpansWeekend_skipsWeekend()`**: Testea el salto al lunes de fase activa de 15h que cae en fin de semana.
- **`estimateEndDate_phaseWithDecimalHours()`**: Testea el cálculo correcto de minutos con fases de 2.5h (2h 30min).
- **`estimateEndDate_phaseEndsExactlyAtClosingTime()`**: Testea que fase que termina justo a las 17:00 no se extiende al día siguiente.
- **`estimateEndDate_realWorldScenario_beerProduction()`**: Testea el escenario completo de producción de cerveza con 7 fases calculando ~21 días.
- **`estimateEndDate_emptyPhaseList_returnsStartDate()`**: Testea el retorno de la misma fecha de inicio cuando no hay fases.
- **`estimateEndDate_phaseSpansMultipleWeeks()`**: Testea el abarcamiento de múltiples semanas laborables con fase larga de 168h (7 días).
- **`estimateEndDate_startAtMiddayWithPartialHours()`**: Testea el cálculo de inicio a las 12:00 con fase de 3.5h terminando a las 15:30.

#### DataLoaderServiceTest

Tests unitarios de servicio de carga de datos de muestra.

- **`loadSampleDataIfEmpty_invokesCreationFlow_andSkipsSqlExecutionWhenProductionOrdersExist()`**: Testea la invocación del flujo de creación simulando repositorios vacíos excepto productionOrderRepository asegurando que no se intenta ejecutar scripts SQL cuando ya existen órdenes.

#### LoginAttemptServiceTest

Tests unitarios de servicio de intentos de login.

- **`loginAttempts_blocking_and_reset_behaviour()`**: Testea el bloqueo temporal tras fallos de login comprobando isBlocked(), getRemainingAttempts() y getBlockedMessage() validando que loginSuccess() resetea el estado.

### Exception Handling Tests

#### GlobalExceptionHandlerTest

Tests del manejador global de excepciones para mapeo a códigos HTTP.

- **`resourceNotFound_mapsTo404()`**: Testea el mapeo de ResourceNotFoundException a 404 con cuerpo estructurado.
- **`dataIntegrity_mapsToConflict()`**: Testea el mapeo de DataIntegrityViolationException a 409.
- **`blockedUser_mapsTo429_withAuthBody()`**: Testea el retorno de AuthResponseDTO con token=BLOCKED y message cuando BlockedUserException es lanzada mapeando a 429.
- **`genericException_mapsTo500()`**: Testea el mapeo de RuntimeException no gestionada a 500 con estructura de error.

### Security Tests

#### HttpsRedirectFilterTest

Tests unitarios del filtro de redirección HTTPS.

- **`localHost_doesNotRedirect()`**: Testea que localhost no provoca redirect ejecutando el FilterChain.
- **`nonHttps_redirectsToHttps()`**: Testea la redirección de petición no segura a https:// con la misma ruta y query.

#### SecurityServiceTest

Tests unitarios del servicio de seguridad.

- **`isSupervisorOfPhase_ReturnsTrue_WhenExistsRelation()`**: Testea el retorno de true cuando el repositorio confirma la relación fase-supervisor.
- **`isSupervisorOfPhase_ReturnsFalse_WhenRelationDoesNotExist()`**: Testea el retorno de false cuando no existe la relación fase-supervisor.
- **`isSupervisorOfPhase_ThrowsClassCast_WhenPrincipalNotUser()`**: Testea el lanzamiento de ClassCastException cuando el principal no es del tipo User.

### Mapper Tests

#### DoubleRoundingUtilTest

Tests del helper MapStruct para redondeo de decimales.

- **`round2_handlesNullAndRounding()`**: Testea el manejo de null y varios casos de redondeo (1.234 → 1.23, 1.235 → 1.24, 2.0 → 2.00).

---

## Materials

### Service Tests

#### MaterialServiceImplTest

Tests unitarios de la lógica de negocio del servicio de materiales.

- **`testSaveMaterial()`**: Testea la creación exitosa de un material con todos sus campos (nombre, tipo, stock, umbral).
- **`testUpdateMaterial_Success()`**: Testea la actualización parcial exitosa de material existente.
- **`testUpdateMaterial_NotFound()`**: Testea el lanzamiento de ResourceNotFoundException al actualizar material inexistente.
- **`testToggleActive_Success()`**: Testea la activación/desactivación exitosa de material.
- **`testGetMaterial_Success()`**: Testea la obtención de material por ID retornando DTO mapeado correctamente.
- **`testFindAll_ReturnsPage()`**: Testea el listado de materiales con paginación retornando Page correctamente.
- **`testGetMaterialSimpleList()`**: Testea la obtención de lista simple de materiales activos para selectores.
- **`createMaterial_invalidUnitForType_throwsBadRequest()`**: Testea el lanzamiento de BadRequestException al crear material con type=ENVASE y unitMeasurement!=UNIDAD.
- **`updateMaterialLocation_invalidLayout_throwsBadRequest()`**: Testea el lanzamiento de BadRequestException cuando la ubicación del almacén es inválida.
- **`getMaterialSimpleList_withPhase_usesPhaseMapping()`**: Testea la búsqueda de materiales por fase utilizando el mapeo de tipos válidos.
- **`createMaterial_invalidSectionProvided_throwsBadRequest()`**: Testea el lanzamiento de BadRequestException al crear material con warehouseSection inválida para la warehouseZone.
- **`getWarehouseInfo_withMaterialType_returnsSuggestedLocation()`**: Testea la sugerencia de ubicación coherente cuando se pasa un MaterialType.

### Mapper Tests

#### MaterialMapperTest

Tests de transformación MapStruct para conversiones Material entity ↔ DTOs.

- **`testToResponseDto_isBelowThresholdTrue()`**: Testea el mapeo a DTO cuando stock está por debajo del umbral (campo calculado isBelowThreshold=true).
- **`testToResponseDto_isBelowThresholdFalse()`**: Testea el mapeo cuando stock es suficiente (isBelowThreshold=false).
- **`testToEntityFromCreateDTO()`**: Testea la creación de entidad desde CreateDTO con validaciones.
- **`testToDetailDto_isBelowThresholdTrue()`**: Testea el mapeo a DTO detallado con cálculo de umbral.
- **`testPartialUpdate_IgnoresNullsAndUpdatesNonNulls()`**: Testea la actualización parcial ignorando campos null y actualizando solo los provistos.

### Repository Tests

#### MaterialRepositoryTest

Tests de consultas JPA personalizadas del repositorio de materiales.

- **`findTop10ByNameContainingIgnoreCase_returnsResults()`**: Testea la consulta por prefijo insensible a mayúsculas retornando resultados.
- **`findWarehouseSectionsByZone_and_countByWarehouseZone()`**: Testea la obtención de secciones ocupadas y conteo correcto por zona.
- **`existsByCode_returnsTrueWhenPresent()`**: Testea la verificación de existencia de material por código.

#### MaterialSpecificationTest

Tests de filtros compuestos con especificaciones.

- **`filterByName_supplier_type_and_isActive()`**: Testea la combinación correcta de filtros por nombre, proveedor, tipo e isActive.

### Controller Tests

#### MaterialControllerTest

Tests del controlador REST de materiales.

- **`testGetMaterial_NotFound_ShouldReturn404()`**: Testea el retorno de 404 al obtener material inexistente.
- **`testCreateMaterial_BadRequest_ShouldReturn409()`**: Testea el retorno de 409 Conflict al crear material con datos inválidos.
- **`testCreateMaterial()`**: Testea la creación exitosa de material retornando 201 Created.
- **`testCreateMaterial_MissingName_ShouldReturnBadRequest()`**: Testea el retorno de 400 cuando falta el campo obligatorio name.
- **`testCreateMaterial_NegativeStock_ShouldReturnBadRequest()`**: Testea el retorno de 400 cuando stock es negativo.
- **`testCreateMaterial_InvalidType_ShouldReturnBadRequest()`**: Testea el retorno de 400 con tipo de material inválido.
- **`testUpdateMaterial()`**: Testea la actualización correcta de material.
- **`testToggleActive()`**: Testea el cambio de estado activo de material.
- **`testGetMaterials()`**: Testea la obtención de listado paginado de materiales.
- **`testGetMaterial()`**: Testea la obtención de material específico por ID.

#### MaterialControllerSecurityTest

Tests de seguridad del controlador de materiales.

- **`getMaterials_unauthenticated_returns401()`**: Testea el retorno de 401 sin autenticación.
- **`getMaterials_authenticated_returns200()`**: Testea el retorno de 200 con autenticación válida.

#### MaterialControllerAccessTest

Tests de control de acceso del controlador de materiales.

- **Tests de matriz de acceso por roles**: Testea los permisos de acceso a endpoints de materiales según diferentes roles de usuario.

### Integration Tests

#### MaterialIntegrationTest

Tests de integración end-to-end para materiales.

- **`createMaterial_andGetById_happyPath()`**: Testea la creación de material y recuperación exitosa verificando persistencia.
- **`listMaterials_happyPath()`**: Testea el listado de materiales con paginación y filtros.
- **`updateMaterial_happyPath()`**: Testea la actualización completa exitosa de material existente.
- **`toggleMaterialActive_happyPath()`**: Testea la activación/desactivación de material verificando cambio.

---

## Movements

### Service Tests

#### MovementServiceImplTest

Tests unitarios del servicio de movimientos de materiales.

- **`testCreateMovement_Ingreso_Success()`**: Testea la creación exitosa de un movimiento de ingreso de material.
- **`testCreateMovement_Egreso_Success()`**: Testea la creación exitosa de un movimiento de egreso de material.
- **`testCreateMovement_Egreso_StockInsuficiente()`**: Testea el fallo de egreso cuando el stock es insuficiente.
- **`testCreateMovement_MaterialNoEncontrado()`**: Testea el fallo cuando el material no existe.
- **`testGetMovement_Success()`**: Testea la obtención exitosa de un movimiento por ID.
- **`testGetMovement_NotFound()`**: Testea el fallo cuando el movimiento no existe.
- **`testFindAll_ReturnsPage()`**: Testea la paginación y búsqueda de movimientos.
- **`testCreateReserveOrReturn_Reserva_Success()`**: Testea la reserva exitosa de material.
- **`testCreateReserveOrReturn_Reserva_StockInsuficiente()`**: Testea el fallo de reserva cuando el stock es insuficiente.
- **`testCreateReserveOrReturn_Devuelto_Success()`**: Testea la devolución exitosa de material reservado.
- **`testCreateReserveOrReturn_Devuelto_ReservedStockInsuficiente()`**: Testea el fallo de devolución cuando el stock reservado es insuficiente.
- **`testCreateReserveOrReturn_TipoInvalido()`**: Testea el fallo cuando el tipo de movimiento es inválido.
- **`testConfirmReservation_Success()`**: Testea la confirmación exitosa de una reserva.
- **`testConfirmReservation_ReservedStockInsuficiente()`**: Testea el fallo de confirmación cuando el stock reservado es insuficiente.
- **`testCompleteMovement_Ingreso_Success()`**: Testea la finalización exitosa de movimiento de ingreso aumentando el stock.
- **`testCompleteMovement_Egreso_TriggersLowStockNotification()`**: Testea la generación de notificación de bajo stock cuando egreso deja stock por debajo del umbral.
- **`testCompleteMovement_Egreso_StockInsuficienteAtCompletion()`**: Testea el lanzamiento de BadRequestException cuando al completar egreso el stock no alcanza.
- **`testToggleInProgress_FromPendienteToEnProceso()`**: Testea el cambio de estado de PENDIENTE a EN_PROCESO asignando inProgressByUserId y takenAt.
- **`testToggleInProgress_FromEnProcesoToPendiente_SameUser()`**: Testea la reversión a PENDIENTE por el mismo usuario limpiando campos.
- **`testToggleInProgress_CompletedMovementThrows()`**: Testea el rechazo de togglear un movimiento completado.
- **`testToggleInProgress_RevertByDifferentUserThrows()`**: Testea el impedimento de revertir EN_PROCESO si el usuario actual no es quien lo tomó.
- **`testCreateMovements_bulkCreate_assignsCreatedBy()`**: Testea la creación en lote de movimientos delegando en saveAll.
- **`testCompleteMovement_EnProceso_OtherUserThrows()`**: Testea el lanzamiento de BadRequestException cuando un usuario distinto intenta completar un movimiento EN_PROCESO.

### Mapper Tests

#### MovementMapperTest

Tests de transformación MapStruct para movimientos.

- **`testToResponseDto()`**: Testea el mapeo de entidad a DTO de respuesta.
- **`testToDetailDTO()`**: Testea el mapeo a DTO con detalles completos del movimiento.

### Repository Tests

#### MovementRepositoryTest

Tests de consultas JPA del repositorio de movimientos.

- **`findByStatusOrderByCreationDateAsc_returnsOrdered()`**: Testea el retorno ordenado ascendentemente de movimientos por fecha de creación.

#### MovementSpecificationTest

Tests de filtros compuestos para movimientos.

- **`createFilter_filtersByMaterialAndType()`**: Testea el filtrado correcto por materialId y type.

### Controller Tests

#### MovementControllerTest

Tests del controlador REST de movimientos.

- **`testCreateMovement()`**: Testea la creación de movimiento vía API retornando 201.
- **`testCreateMovement_BadRequest_ShouldReturn400()`**: Testea el retorno de 400 con datos inválidos.
- **`testGetMovement()`**: Testea la obtención de un movimiento por ID retornando 200.
- **`testGetMovement_NotFound_ShouldReturn404()`**: Testea el retorno de 404 cuando el movimiento no existe.
- **`testCreateMovement_InvalidType_ShouldReturn400()`**: Testea el retorno de 400 con tipo de movimiento inválido.
- **`testToggleInProgress_Patch_Success()`**: Testea el cambio de estado en progreso retornando 200 y DTO mapeado.
- **`testToggleInProgress_Patch_BadRequest()`**: Testea el mapeo de BadRequestException a 400 en toggle in progress.
- **`testCompleteMovement_Patch_Success()`**: Testea la finalización exitosa de movimiento retornando 200 y DTO mapeado.
- **`testCompleteMovement_Patch_NotFound()`**: Testea el mapeo de ResourceNotFoundException a 404 en complete movement.

#### MovementControllerSecurityTest

Tests de seguridad del controlador de movimientos.

- **`getMovements_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan acceder.
- **`getMovements_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados acceden.

### Integration Tests

#### MovementIntegrationTest

Tests de integración end-to-end de movimientos.

- **`createMovement_andGetById_happyPath()`**: Testea el flujo completo de crear y obtener un movimiento.
- **`createMovement_updatesStockCorrectly()`**: Testea la actualización correcta del stock al crear movimientos.
- **`listMovements_happyPath()`**: Testea el listado de movimientos con paginación.

---

## Notifications

### Service Tests

#### NotificationServiceImplTest

Tests unitarios del servicio de notificaciones.

- **`createNotification_success_sendsSseAndReturnsDto()`**: Testea la creación exitosa de notificación persistiéndola y enviando eventos SSE (sendNotificationToUser y sendStatsUpdate).
- **`createNotification_userNotFound_throws()`**: Testea el lanzamiento de ResourceNotFoundException cuando el usuario destino no existe sin guardar la notificación.
- **`markAsRead_happyPath_marksAndSendsStats()`**: Testea el marcado de notificación como leída persistiendo el cambio y enviando actualización de estadísticas vía SSE.
- **`markAsRead_wrongUser_throws()`**: Testea el lanzamiento de BadRequestException cuando un usuario intenta marcar como leída una notificación de otro usuario.
- **`markAllAsRead_savesAll()`**: Testea el marcado de todas las notificaciones no leídas de un usuario como leídas persistiendo los cambios.
- **`getUserNotificationStats_returnsCounts()`**: Testea el retorno de conteos de notificaciones no leídas y totales basándose en consultas al repositorio.
- **`createProductionOrderNotification_createsForManagers_whenNotExisting()`**: Testea la creación de notificaciones para gerentes de planta evitando duplicados.
- **`getUserNotifications_returnsPage()`**: Testea la obtención paginada de notificaciones del usuario.
- **`getUserUnreadNotifications_returnsPage()`**: Testea la obtención paginada de notificaciones no leídas del usuario.
- **`createLowStockNotification_noSupervisors_noSave()`**: Testea que no se guarda notificación cuando no hay supervisores encontrados.
- **`createLowStockNotification_createsForSupervisors_whenNotRecent()`**: Testea la creación de notificaciones de bajo stock para supervisores cuando no existen notificaciones recientes.
- **`createPendingMovementNotification_noOperators_noSave()`**: Testea que no se guarda notificación cuando no hay operadores encontrados.
- **`createPendingMovementNotification_createsForOperators_whenNotExisting()`**: Testea la creación de notificaciones de movimientos pendientes para operadores cuando no existen.
- **`cleanupOldNotifications_deletesWhenOldExist()`**: Testea la eliminación de notificaciones antiguas cuando existen.

#### SseMonitoringServiceTest

Tests unitarios del servicio de monitoreo SSE.

- **`getSseMetrics_and_isHealthy_normalScenario()`**: Testea el cálculo de métricas y healthy=true para conteos bajos de conexiones.
- **`getSseMetrics_and_isHealthy_overloadScenario()`**: Testea el cálculo de métricas y healthy=false para condiciones de sobrecarga.

#### SseNotificationServiceTest

Tests unitarios del servicio de notificaciones SSE.

- **`registerAndCreateConnectionByUsername_and_counts()`**: Testea el registro de username en cache, creación de conexión por username y validación de conteos de conexiones activas con comportamiento de remoción de cache.
- **`createConnection_respects_max_connections_and_eviction()`**: Testea la creación de más conexiones que el límite permitido verificando que la evicción mantiene conexiones <= límite.
- **`sendNotificationToUser_noConnections_noThrow()`**: Testea el envío de notificación a usuario sin conexiones activas asegurando que no lanza excepciones y mantiene estado estable.

### Entity Tests

#### NotificationEntityTest

Tests de la entidad Notification.

- **`markAsRead_setsFlagAndReadAt()`**: Testea que markAsRead() establece isRead=true y popula readAt.

### Mapper Tests

#### NotificationMapperTest

Tests de transformación MapStruct para notificaciones.

- **`toResponseDTO_mapsFieldsCorrectly()`**: Testea el mapeo correcto de campos de entidad Notification a NotificationResponseDTO.

### Controller Tests

#### NotificationControllerTest

Tests del controlador REST de notificaciones.

- **`getConnections_returnsInfo()`**: Testea el retorno de información de conexiones GET /notifications/connections.
- **`testConnectivity_returnsInfo()`**: Testea el retorno de detalles de conectividad incluyendo sseConnections GET /notifications/test.

#### SseMonitoringControllerTest

Tests del controlador de monitoreo SSE.

- **`getSseMetrics_and_getSseHealth_returnOk()`**: Testea el retorno de métricas y estado de salud del sistema SSE.

---

## Packagings

### Service Tests

#### PackagingServiceImplTest

Tests unitarios del servicio de envases/packaging.

- **`testCreatePackaging_Success()`**: Testea la creación exitosa de un envase.
- **`testToggleActive_Success()`**: Testea la activación/desactivación exitosa de un envase.
- **`testToggleActive_NotFound()`**: Testea el fallo al activar/desactivar envase inexistente.
- **`testFindAll_ReturnsPage()`**: Testea la paginación de envases.
- **`testGetPackaging_Success()`**: Testea la obtención exitosa de un envase por ID.
- **`testGetPackaging_NotFound()`**: Testea el fallo cuando el envase no existe.
- **`testGetPackagingList_FiltersActivePackagings()`**: Testea el filtrado de envases activos.
- **`testUpdatePackaging_Success()`**: Testea la actualización exitosa de un envase.
- **`testUpdatePackaging_NotFound()`**: Testea el fallo al actualizar envase inexistente.
- **`testCreatePackaging_PackagingMaterialWrongType_ThrowsBadRequest()`**: Testea el lanzamiento de BadRequestException al crear con packagingMaterial de tipo distinto a ENVASE.
- **`testCreatePackaging_LabelingMaterialWrongType_ThrowsBadRequest()`**: Testea el lanzamiento de BadRequestException al crear con labelingMaterial de tipo distinto a ETIQUETADO.
- **`testCreatePackaging_UnitMeasurementUnidad_ThrowsBadRequest()`**: Testea el lanzamiento de BadRequestException al crear con unitMeasurement == UNIDAD.
- **`testGetPackagingList_NameNull_ReturnsEmpty()`**: Testea el retorno de lista vacía cuando getPackagingList recibe name null.
- **`testGetPackagingList_WithProductId_UsesProductUnitMeasurement()`**: Testea el uso de unitMeasurement del producto al pasar productId llamando los repositorios adecuados.
- **`testUpdatePackaging_PackagingMaterialChangeToNonEnvase_ThrowsBadRequest()`**: Testea el lanzamiento de BadRequestException al cambiar packagingMaterialId a uno no ENVASE.
- **`testUpdatePackaging_LabelingMaterialChangeToNonEtiquetado_ThrowsBadRequest()`**: Testea el lanzamiento de BadRequestException al cambiar labelingMaterialId a uno no ETIQUETADO.
- **`testUpdatePackaging_UnitMeasurementUnidad_ThrowsBadRequest()`**: Testea el lanzamiento de BadRequestException al actualizar con unitMeasurement == UNIDAD.

### Mapper Tests

#### PackagingMapperTest

Tests de transformación MapStruct para packagings.

- **`testToEntity()`**: Testea el mapeo de DTO a entidad.
- **`testToResponseDto()`**: Testea el mapeo de entidad a DTO de respuesta con todos los campos.
- **`testToSimpleResponseDTO()`**: Testea el mapeo a DTO simplificado.
- **`testPartialUpdate()`**: Testea la actualización parcial de un envase existente.

### Controller Tests

#### PackagingControllerTest

Tests del controlador REST de packagings.

- **`testCreatePackaging()`**: Testea la creación de envase vía API retornando 201.
- **`testCreatePackaging_BadRequest_ShouldReturn400()`**: Testea el retorno de 400 con datos inválidos.
- **`testUpdatePackaging()`**: Testea la actualización de envase retornando 200.
- **`testToggleActive()`**: Testea la activación/desactivación de envase retornando 200.
- **`testGetPackaging()`**: Testea la obtención de envase por ID retornando 200.
- **`testGetPackaging_NotFound_ShouldReturn404()`**: Testea el retorno de 404 cuando no existe.
- **`testGetPackagings()`**: Testea el listado de envases retornando 200.
- **`testGetPackagings_PaginationMetadata_ShouldReturnFields()`**: Testea el retorno de metadatos correctos de paginación.

#### PackagingControllerSecurityTest

Tests de seguridad del controlador de packagings.

- **`getPackagings_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan acceder.
- **`getPackagings_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados acceden.

### Integration Tests

#### PackagingIntegrationTest

Tests de integración end-to-end de packagings.

- **`packagingCrudHappyPath()`**: Testea el flujo completo CRUD de envases.

---

## Product Phases

### Service Tests

#### ProductPhaseServiceImplTest

Tests unitarios del servicio de fases de producto.

- **`testUpdateProductPhase_Success()`**: Testea la actualización exitosa de fase existente (duración, materiales).
- **`testUpdateProductPhase_NotFound()`**: Testea el lanzamiento de ResourceNotFoundException al actualizar fase inexistente.
- **`testFindAll_Success()`**: Testea el listado de todas las fases con paginación.
- **`testGetProductPhase_Success()`**: Testea la obtención exitosa de fase por ID.
- **`testGetProductPhase_NotFound()`**: Testea el lanzamiento de excepción cuando fase no existe.
- **`testGetByProduct_Success()`**: Testea la obtención de fases de un producto específico ordenadas por phaseOrder.
- **`testGetByProduct_EmptyAndProductNotExists()`**: Testea el retorno de lista vacía cuando producto no tiene fases.
- **`testMarkAsReady_Success()`**: Testea el marcado exitoso de fase como lista validando que materiales y duración están completos.
- **`testMarkAsReady_NotFound()`**: Testea el lanzamiento de excepción al marcar como lista fase inexistente.
- **`testToggleReady_IncompletePhase()`**: Testea el rechazo de marcar como lista fase incompleta (sin duración).
- **`testMarkAsReady_MissingMaterials()`**: Testea el rechazo de marcar como lista fase sin materiales asignados.
- **`testReviewIsReady_noRequiredMaterials_unsetsReady()`**: Testea que reviewIsReady desmarca isReady en fase y producto cuando no hay materiales requeridos.
- **`testReviewIsReady_missingMaterial_unsetsReady()`**: Testea que reviewIsReady desmarca isReady en fase y producto cuando falta al menos un material requerido.
- **`testToggleReady_turnsOff_whenAlreadyReady()`**: Testea que toggleReady apaga isReady y persiste producto y fase cuando ya estaba listo.

### Mapper Tests

#### ProductPhaseMapperTest

Tests de transformación MapStruct para fases de producto.

- **`testToResponseDto()`**: Testea el mapeo de entidad a DTO de respuesta con todos los campos.
- **`testPartialUpdate()`**: Testea la actualización parcial ignorando nulls actualizando solo campos provistos.
- **`testSetPhaseOrder_prePersist()`**: Testea que callback @PrePersist establece phaseOrder desde phase.getOrder() automáticamente.
- **`testSetPhaseOrder_withDifferentPhases()`**: Testea que diferentes fases (MOLIENDA, FERMENTACION) obtienen orders correctos (1, 4).
- **`testSetPhaseOrder_whenPhaseIsNull()`**: Testea que cuando phase es null phaseOrder permanece 0 (manejo seguro de null).
- **`testSetPhaseOrder_preUpdate()`**: Testea que callback @PreUpdate actualiza phaseOrder cuando cambia la fase.

### Model Tests

#### PhaseTest

Tests del enum Phase validando propiedades y métodos.

- **`allPhases_haveUniqueOrders()`**: Testea que todos los valores del enum Phase tienen orders únicos.
- **`activePhases_areCorrectlyIdentified()`**: Testea la identificación correcta de 7 fases activas (isTimeActive=true).
- **`passivePhases_areCorrectlyIdentified()`**: Testea la identificación correcta de 2 fases pasivas (isTimeActive=false).
- **`phaseOrder_isCorrect()`**: Testea el orden secuencial correcto MOLIENDA(1) → MACERACION(2) → ... → ETIQUETADO(9).
- **`comesBefore_returnsTrue_whenPhaseIsEarlier()`**: Testea que MOLIENDA.comesBefore(FERMENTACION) retorna true.
- **`comesBefore_returnsFalse_whenPhaseIsLater()`**: Testea que ENVASADO.comesBefore(MOLIENDA) retorna false.
- **`comesBefore_returnsFalse_whenPhasesAreSame()`**: Testea que COCCION.comesBefore(COCCION) retorna false.
- **`next_returnsNextPhase_whenExists()`**: Testea que MOLIENDA.next() retorna Optional[MACERACION].
- **`next_returnsEmpty_forLastPhase()`**: Testea que ETIQUETADO.next() retorna Optional.empty().
- **`next_chainingMultipleTimes()`**: Testea que encadenar next() múltiples veces funciona correctamente.
- **`phaseSequence_isLogical()`**: Testea que la secuencia completa refleja el flujo de producción real de cerveza.
- **`activeVsPassivePhases_distribution()`**: Testea el balance entre fases activas (7) y pasivas (2).
- **`getOrder_isImmutable()`**: Testea que el valor de order no cambia (propiedad inmutable del enum).
- **`getIsTimeActive_isImmutable()`**: Testea que el valor de isTimeActive es inmutable.

### Controller Tests

#### ProductPhaseControllerTest

Tests del controlador REST de fases de producto.

- **`testUpdateProductPhase()`**: Testea la actualización exitosa de fase PATCH /product-phases/{id}.
- **`testGetProductPhases()`**: Testea la obtención de listado paginado GET /product-phases.
- **`testGetProductPhase()`**: Testea la obtención de fase específica GET /product-phases/{id}.
- **`testGetProductPhasesByProduct()`**: Testea la obtención de fases de un producto GET /product-phases/product/{productId}.

#### ProductPhaseControllerSecurityTest

Tests de seguridad del controlador de fases de producto.

- **`getProductPhases_unauthenticated_returns401()`**: Testea el retorno de 401 sin autenticación GET /product-phases.
- **`getProductPhases_authenticated_returns200()`**: Testea el retorno de 200 con autenticación GET /product-phases.

### Integration Tests

#### ProductPhaseIntegrationTest

Tests de integración end-to-end para fases de producto.

- **`productPhasesHappyPath()`**: Testea el flujo completo de crear producto, obtener fases, actualizar fase y marcar como lista.

---

## Production Materials

### Service Tests

#### ProductionMaterialServiceImplTest

Tests unitarios del servicio de materiales de producción.

- **`getProductionMaterial_notFound_throws()`**: Testea el lanzamiento de ResourceNotFoundException cuando el ID no existe.
- **`getProductionMaterialByPhase_success()`**: Testea la obtención exitosa de lista de materiales asociados a una fase de producción específica.
- **`getProductionMaterialByPhase_empty_returnsEmptyList()`**: Testea el retorno de lista vacía cuando no hay materiales asociados a la fase.
- **`getProductionMaterialByBatch_success()`**: Testea la obtención exitosa de lista de materiales asociados a un lote (batch) específico.
- **`getProductionMaterialByBatch_empty_returnsEmptyList()`**: Testea el retorno de lista vacía cuando no hay materiales asociados al lote.

### Mapper Tests

#### ProductionMaterialMapperTest

Tests de transformación MapStruct para materiales de producción.

- **`toResponseDTO_mapsNestedFields()`**: Testea el mapeo correcto de campos anidados (material.id → materialId, material.code → materialCode, productionPhase.id → productionPhaseId, quantity).

### Controller Tests

#### ProductionMaterialControllerTest

Tests del controlador REST de materiales de producción.

- **`getProductionMaterial_returns200()`**: Testea la obtención exitosa de material de producción GET /production-materials/{id} retornando 200.
- **`getProductionMaterial_notFound_returns404()`**: Testea el retorno de 404 al obtener material de producción inexistente.
- **`getByPhase_returns200_andArray()`**: Testea la obtención exitosa de materiales por fase GET /production-materials/by-production-phase/{id} retornando array 200.

### Integration Tests

#### ProductionMaterialIntegrationTest

Tests de integración end-to-end para materiales de producción.

- **`getProductionMaterial_notFound_returns404()`**: Testea el retorno de 404 al obtener material de producción inexistente en entorno real.
- **`getProductionMaterialByPhase_emptyOrNotFound_returns200Or404()`**: Testea el retorno de 200 con lista vacía o 404 según implementación del controlador al obtener materiales por fase.
- **`createProductionOrder_generatesProductionMaterials_andEndpointsReturnData()`**: Testea la generación de ProductionMaterials al crear orden de producción verificando que endpoints GET /production-materials/by-batch/{batchId} y /by-production-phase/{phaseId} retornan los materiales esperados.

---

## Production Orders

### Service Tests

#### ProductionOrderServiceImplTest

Tests unitarios del servicio de órdenes de producción.

- **`createProductionOrder_success()`**: Testea la creación exitosa de una orden de producción.
- **`createProductionOrder_callsNotification()`**: Testea la creación de notificación createProductionOrderNotification al crear la orden.
- **`createProductionOrder_persistsProductionMaterials()`**: Testea la persistencia de ProductionMaterial mediante productionMaterialRepository.saveAllAndFlush.
- **`createProductionOrder_roundsQuantitiesCorrectly()`**: Testea el redondeo de cantidad total de orden a 3 decimales y multiplicadores de material a 6 decimales.
- **`createProductionOrder_productNotFound_throws()`**: Testea el fallo cuando el producto no existe.
- **`createProductionOrder_productNotReady_throws()`**: Testea el fallo cuando el producto no está listo.
- **`createProductionOrder_unitMeasurementMismatch_throws()`**: Testea el fallo cuando las unidades no coinciden.
- **`createProductionOrder_verifiesCorrectMaterialReservation()`**: Testea la reserva correcta de materiales.
- **`approveOrder_success()`**: Testea la aprobación exitosa de una orden.
- **`approveOrder_orderNotFound_throws()`**: Testea el fallo al aprobar orden inexistente.
- **`approveOrder_orderNotPending_throws()`**: Testea el fallo al aprobar orden que no está pendiente.
- **`approveOrder_verifiesCorrectMaterialConfirmation()`**: Testea la confirmación correcta de materiales reservados.
- **`returnOrder_toCancelled_success()`**: Testea la cancelación exitosa de una orden.
- **`returnOrder_toRejected_success()`**: Testea el rechazo exitoso de una orden.
- **`returnOrder_orderNotFound_throws()`**: Testea el fallo al devolver orden inexistente.
- **`returnOrder_orderNotPending_throws()`**: Testea el fallo al devolver orden que no está pendiente.
- **`returnOrder_toPendingStatus_throws()`**: Testea el fallo al intentar devolver a estado pendiente.
- **`returnOrder_verifiesCorrectMaterialReturn()`**: Testea la devolución correcta de materiales reservados.
- **`getProductionOrder_success()`**: Testea la obtención exitosa de una orden por ID.
- **`getProductionOrder_notFound_throws()`**: Testea el fallo cuando la orden no existe.
- **`findAll_success()`**: Testea la búsqueda paginada exitosa de órdenes.
- **`findAll_emptyResults()`**: Testea el retorno de lista vacía cuando no hay órdenes.

### Mapper Tests

#### ProductionOrderMapperTest

Tests de transformación MapStruct para órdenes de producción.

- **`testToEntity_fromCreateDTO()`**: Testea el mapeo de DTO de creación a entidad.
- **`testToResponseDTO_allFieldsMapped()`**: Testea el mapeo de todos los campos a DTO de respuesta.
- **`testToResponseDTO_withNullDates()`**: Testea el mapeo cuando las fechas son nulas.
- **`testToResponseDTO_pendingOrder()`**: Testea el mapeo de una orden pendiente.
- **`testToResponseDTO_cancelledOrder()`**: Testea el mapeo de una orden cancelada.

### Controller Tests

#### ProductionOrderControllerTest

Tests del controlador REST de órdenes de producción.

- **`testCreateProductionOrder_Success()`**: Testea la creación exitosa de orden vía API retornando 201.
- **`testCreateProductionOrder_MissingProductId_ShouldReturnBadRequest()`**: Testea la validación de productId requerido.
- **`testCreateProductionOrder_MissingQuantity_ShouldReturnBadRequest()`**: Testea la validación de cantidad requerida.
- **`testCreateProductionOrder_NegativeQuantity_ShouldReturnBadRequest()`**: Testea el rechazo de cantidades negativas.
- **`testCreateProductionOrder_MissingPackagingId_ShouldReturnBadRequest()`**: Testea la validación de packagingId requerido.
- **`testCreateProductionOrder_MissingPlannedDate_ShouldReturnBadRequest()`**: Testea la validación de fecha planificada requerida.
- **`testCreateProductionOrder_ProductNotReady_ShouldReturn400()`**: Testea el fallo con producto no listo.
- **`testCreateProductionOrder_ProductNotFound_ShouldReturn404()`**: Testea el fallo con producto inexistente.
- **`testApproveOrder_Success()`**: Testea la aprobación exitosa de orden retornando 200.
- **`testApproveOrder_NotFound_ShouldReturn400()`**: Testea el fallo al aprobar orden inexistente.
- **`testApproveOrder_NotPending_ShouldReturn400()`**: Testea el fallo al aprobar orden no pendiente.
- **`testCancelOrder_Success()`**: Testea la cancelación exitosa de orden retornando 200.
- **`testCancelOrder_NotFound_ShouldReturn400()`**: Testea el fallo al cancelar orden inexistente.
- **`testCancelOrder_NotPending_ShouldReturn400()`**: Testea el fallo al cancelar orden no pendiente.
- **`testRejectOrder_Success()`**: Testea el rechazo exitoso de orden retornando 200.
- **`testRejectOrder_NotFound_ShouldReturn400()`**: Testea el fallo al rechazar orden inexistente.
- **`testRejectOrder_NotPending_ShouldReturn400()`**: Testea el fallo al rechazar orden no pendiente.
- **`testGetProductionOrders_Success()`**: Testea el listado exitoso de órdenes retornando 200.
- **`testGetProductionOrders_WithPagination()`**: Testea la paginación correcta de órdenes.
- **`testGetProductionOrder_Success()`**: Testea la obtención exitosa de orden por ID retornando 200.
- **`testGetProductionOrder_NotFound_ShouldReturn404()`**: Testea el retorno de 404 cuando no existe.

#### ProductionOrderControllerSecurityTest

Tests de seguridad del controlador de órdenes de producción.

- **`getProductionOrders_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan listar órdenes.
- **`getProductionOrders_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados listan órdenes.
- **`getProductionOrder_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan obtener orden.
- **`getProductionOrder_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados obtienen orden.
- **`approveOrder_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan aprobar.
- **`approveOrder_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados aprueban órdenes.
- **`cancelOrder_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan cancelar.
- **`cancelOrder_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados cancelan órdenes.
- **`rejectOrder_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan rechazar.
- **`rejectOrder_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados rechazan órdenes.

#### ProductionOrderControllerValidationTest

Tests de validación del controlador de órdenes de producción.

- **`createProductionOrder_pastDate_returns400()`**: Testea el retorno de 400 cuando plannedDate está en el pasado exponiendo campo details.plannedDate en el error.
- **`createProductionOrder_todayDate_returns201()`**: Testea la aceptación de plannedDate igual a la fecha actual.
- **`createProductionOrder_futureDate_returns201()`**: Testea la aceptación de plannedDate futura.
- **`createProductionOrder_nullPlannedDate_returns400()`**: Testea el retorno de 400 cuando plannedDate es null (requerida).

---

## Production Phases

### Service Tests

#### ProductionPhaseServiceImplTest

Tests unitarios del servicio de fases de producción.

- **`testSetUnderReview_Success()`**: Testea el envío exitoso de fase EN_PROCESO a BAJO_REVISION con input/output registrados.
- **`testSetUnderReview_NotFound()`**: Testea el lanzamiento de ResourceNotFoundException cuando la fase no existe.
- **`testSetUnderReview_InvalidStatus_ThrowsBadRequest()`**: Testea el rechazo de enviar a revisión desde estados no permitidos (ej: COMPLETADA).
- **`testSetUnderReview_FromSiendoAjustada_Success()`**: Testea el re-envío exitoso a revisión desde SIENDO_AJUSTADA.
- **`testGetProductionPhase_Success()`**: Testea la obtención exitosa de fase por ID mapeada a ResponseDTO.
- **`testGetProductionPhase_NotFound()`**: Testea el lanzamiento de ResourceNotFoundException cuando la fase no existe.
- **`testGetProductionPhasesByBatch_Success()`**: Testea el listado exitoso de fases asociadas a un lote (batch).
- **`testGetProductionPhasesByBatch_NotFound()`**: Testea el lanzamiento de ResourceNotFoundException cuando el batch no tiene fases.
- **`testReviewProductionPhase_ApprovedQuality_CompletesPhase()`**: Testea el marcado de fase como COMPLETADA cuando todos los parámetros de calidad están aprobados.
- **`testReviewProductionPhase_NonCriticalErrors_AdjustsPhase()`**: Testea el cambio a SIENDO_AJUSTADA cuando hay parámetros no críticos rechazados.
- **`testReviewProductionPhase_CriticalErrors_RejectsPhase()`**: Testea el cambio a RECHAZADA cuando hay parámetros críticos rechazados.
- **`testReviewProductionPhase_InvalidStatus_ThrowsBadRequest()`**: Testea la restricción de revisar solo fases en estado BAJO_REVISION.
- **`testReviewProductionPhase_NoQualityParameters_ThrowsBadRequest()`**: Testea el requerimiento de al menos un parámetro de calidad asignado.
- **`testSuspendProductionPhases_WithMaterials()`**: Testea la suspensión de fases creando movimientos de devolución de materiales.
- **`testSuspendProductionPhases_WithoutMaterials()`**: Testea la suspensión de fases sin generar movimientos cuando no hay materiales.

### Mapper Tests

#### ProductionPhaseMapperTest

Tests de transformación MapStruct para fases de producción.

- **`testToResponseDTO_MapsAllFields()`**: Testea el mapeo de todos los campos incluyendo relaciones anidadas (batchId, batchCode, sectorId).
- **`testPartialUpdate_UpdatesOnlyProvidedFields()`**: Testea la actualización parcial preservando campos no enviados (id, phase, status).
- **`testToResponseDTO_WithNullBatch()`**: Testea el manejo correcto de batch null (batchId y batchCode null).
- **`testToResponseDTO_WithNullSector()`**: Testea el manejo correcto de sector null (sectorId null).

### Model Tests

#### ProductionPhaseModelTest

Tests de la entidad ProductionPhase validando callbacks.

- **`prePersist_setsPhaseOrder_whenNull()`**: Testea que phaseOrder se fija desde phase.getOrder() en @PrePersist.
- **`preUpdate_calculatesProductWaste_positiveCase()`**: Testea el cálculo de productWaste cuando standardInput, standardOutput, input y output están presentes.
- **`preUpdate_calculatesProductWaste_nonNegative()`**: Testea que el desperdicio nunca sea negativo (se normaliza a 0).
- **`preUpdate_handlesZeroStandardInput()`**: Testea el caso borde donde standardInput == 0 devolviendo productWaste = 0.0.

### Repository Tests

#### ProductionPhaseRepositoryTest

Tests de consultas JPA del repositorio de fases de producción.

- **`findPreviousPhase_returnsPrevious_whenExists()`**: Testea que findPreviousPhase(batch, order) devuelve la fase anterior cuando existe.
- **`existsByIdAndSector_Supervisor_Id_checksSupervisorRelation()`**: Testea que existsByIdAndSector_Supervisor_Id(phaseId, userId) funciona correctamente verificando relación supervisor.

### Controller Tests

#### ProductionPhaseControllerTest

Tests del controlador REST de fases de producción.

- **`testSetUnderReview_Success()`**: Testea el envío exitoso a revisión PATCH /production-phases/set-under-review/{id} retornando 200.
- **`testSetUnderReview_MissingInput_Returns400()`**: Testea la validación de input requerido.
- **`testSetUnderReview_MissingOutput_Returns400()`**: Testea la validación de output requerido.
- **`testSetUnderReview_NegativeInput_Returns400()`**: Testea el rechazo de valores negativos (constraint @DecimalMin).
- **`testReviewProductionPhase_Success()`**: Testea la revisión exitosa de fase PATCH /production-phases/review/{id} retornando 200 con rol SUPERVISOR_DE_CALIDAD.
- **`testGetProductionPhase_Success()`**: Testea la obtención exitosa de fase específica GET /production-phases/{id}.
- **`testGetProductionPhasesByBatch_Success()`**: Testea la obtención exitosa de array de fases GET /production-phases/by-batch/{id}.
- **`testGetProductionPhasesByBatch_EmptyList()`**: Testea el retorno de array vacío cuando no hay fases para el batch.

#### ProductionPhaseControllerSecurityTest

Tests de seguridad del controlador de fases de producción.

- **`testSetUnderReview_WithoutAuth_Returns401()`**: Testea el retorno de 401 sin autenticación.
- **`testSetUnderReview_WithAuth_AcceptsOrRejects()`**: Testea la accesibilidad del endpoint con autenticación verificando autorización supervisor de fase vía @securityService.
- **`testReviewProductionPhase_WithoutAuth_Returns401()`**: Testea el retorno de 401 sin autenticación.
- **`testReviewProductionPhase_WithoutCorrectRole_Returns403()`**: Testea el retorno de 403 cuando usuarios sin rol SUPERVISOR_DE_CALIDAD intentan acceder.
- **`testReviewProductionPhase_WithCorrectRole_AcceptsRequest()`**: Testea que con rol correcto pasa seguridad.
- **`testGetProductionPhase_WithoutAuth_Returns401()`**: Testea el retorno de 401 GET sin auth.
- **`testGetProductionPhase_WithAuth_Returns200Or404()`**: Testea la accesibilidad con autenticación.
- **`testGetProductionPhasesByBatch_WithoutAuth_Returns401()`**: Testea el retorno de 401 GET lista sin auth.
- **`testGetProductionPhasesByBatch_WithAuth_AcceptsRequest()`**: Testea que con autenticación permite acceso.

### Integration Tests

#### ProductionPhaseIntegrationTest

Tests de integración end-to-end para fases de producción.

- **`testGetProductionPhase_NotFound_Returns404()`**: Testea el retorno de 404 GET por ID inexistente.
- **`testGetProductionPhasesByBatch_NotFound_Returns404()`**: Testea el retorno de 404 GET por batch inexistente/sin fases.
- **`testSetUnderReview_NotFound_Returns404()`**: Testea el retorno de 404 PATCH sobre ID inexistente.
- **`testReviewProductionPhase_NotFound_Returns404()`**: Testea el retorno de 404 PATCH review sobre ID inexistente.
- **`testSetUnderReview_InvalidInput_Returns400()`**: Testea el retorno de 400 validación de input negativo.

---

## Production Phase Qualities

### Service Tests

#### ProductionPhaseQualityServiceImplTest

Tests unitarios del servicio de calidades de fases de producción.

- **`createProductionPhaseQuality_success()`**: Testea la creación exitosa de registro asociando correctamente ProductionPhase y QualityParameter con fase compatible.
- **`createProductionPhaseQuality_phaseMismatch_throwsBadRequest()`**: Testea el rechazo cuando la fase del parámetro de calidad no coincide con la de la producción.
- **`createProductionPhaseQuality_phaseNotFound_throws()`**: Testea el lanzamiento de 404 cuando no existe la ProductionPhase.
- **`createProductionPhaseQuality_qualityParamNotFound_throws()`**: Testea el lanzamiento de 404 cuando no existe el QualityParameter.
- **`updateProductionPhaseQuality_success()`**: Testea la actualización exitosa de valor y aprobación preservando campos no enviados (parcial).
- **`updateProductionPhaseQuality_notFound_throws()`**: Testea el lanzamiento de 404 cuando el registro no existe.
- **`getProductionPhaseQuality_success()`**: Testea la obtención exitosa de registro por ID mapeándolo a DTO.
- **`getProductionPhaseQuality_notFound_throws()`**: Testea el lanzamiento de 404 cuando no existe.
- **`getByPhase_success()`**: Testea el listado exitoso de registros por id de ProductionPhase.
- **`getByPhase_notFound_throws()`**: Testea el lanzamiento de 404 cuando la ProductionPhase no existe.
- **`getByBatch_success()`**: Testea el listado exitoso de registros por id de Batch.
- **`getByBatch_notFound_throws()`**: Testea el lanzamiento de 404 cuando el Batch no existe.
- **`approveProductionPhaseQuality_success()`**: Testea la aprobación exitosa de un parámetro persistiendo el cambio y retornando DTO aprobado.
- **`approveProductionPhaseQuality_notFound_throws()`**: Testea el lanzamiento de 404 al aprobar sobre ID inexistente.
- **`disapproveProductionPhaseQuality_success()`**: Testea la desaprobación exitosa de un parámetro persistiendo el cambio.
- **`getCurrentVersionForPhase_noVersions_returnsOne()`**: Testea el retorno de 1 cuando no hay versiones.
- **`getCurrentVersionForPhase_activeQualitiesReturnsSameVersion()`**: Testea el mantenimiento de versión cuando hay versión y activos.
- **`getCurrentVersionForPhase_noActiveQualities_incrementsVersion()`**: Testea el incremento de versión cuando no hay activos.
- **`createNewVersionForPhase_marksHistoricalAndSaves()`**: Testea el marcado de parámetros activos como históricos y persistencia.
- **`getActiveProductionPhaseQualityByPhase_success()`**: Testea el retorno de solo parámetros activos por fase.
- **`getActiveProductionPhaseQualityByBatch_success()`**: Testea el retorno de solo parámetros activos por lote.

### Mapper Tests

#### ProductionPhaseQualityMapperTest

Tests de transformación MapStruct para calidades de fases de producción.

- **`toEntity_fromCreateDTO_mapsSimpleFields()`**: Testea el mapeo de campos simples desde CreateDTO (relaciones se setean en el servicio).
- **`toResponseDTO_mapsNestedFields()`**: Testea el mapeo de campos anidados (productionPhaseId, productionPhase y qualityParameterName).
- **`partialUpdate_updatesOnlyNonNullFields()`**: Testea la actualización parcial aplicando solo campos provistos (no-null).
- **`partialUpdate_nullFields_doNotOverwrite()`**: Testea que campos null en UpdateDTO no sobrescriben valores existentes (usa NullValuePropertyMappingStrategy.IGNORE).

### Controller Tests

#### ProductionPhaseQualityControllerTest

Tests del controlador REST de calidades de fases de producción.

- **`create_validRequest_returns201()`**: Testea la creación exitosa de registro POST /production-phases-qualities retornando 201.
- **`create_missingFields_returns400()`**: Testea el retorno de 400 con request inválido.
- **`update_validRequest_returns200()`**: Testea la actualización exitosa PATCH retornando 200.
- **`update_notFound_returns404()`**: Testea el retorno de 404 PATCH sobre ID inexistente.
- **`getById_returns200()`**: Testea la obtención exitosa GET por ID retornando 200.
- **`getById_notFound_returns404()`**: Testea el retorno de 404 GET por ID inexistente.
- **`getByPhase_returns200_andArray()`**: Testea la obtención exitosa GET por fase retornando array 200.
- **`getByBatch_returns200_andArray()`**: Testea la obtención exitosa GET por lote retornando array 200.
- **`approve_endpoint_returns200()`**: Testea la aprobación exitosa de parámetro PATCH /{id}/approve retornando 200 con DTO.
- **`disapprove_endpoint_returns200()`**: Testea la desaprobación exitosa de parámetro PATCH /{id}/disapprove retornando 200 con DTO.

#### ProductionPhaseQualityControllerSecurityTest

Tests de seguridad del controlador de calidades de fases de producción.

- **`create_withoutAuth_returns401()`**: Testea el retorno de 401 POST sin autenticación.
- **`create_withWrongRole_returns403()`**: Testea el retorno de 403 POST con rol incorrecto.
- **`create_withCorrectRole_acceptsRequest()`**: Testea que POST con rol correcto supera seguridad permitiendo 201/4xx funcional nunca 401/403.
- **`update_withoutAuth_returns401()`**: Testea el retorno de 401 PATCH sin autenticación.
- **`update_withWrongRole_returns403()`**: Testea el retorno de 403 PATCH con rol incorrecto.
- **`update_withCorrectRole_acceptsRequest()`**: Testea que PATCH con rol correcto supera seguridad permitiendo 200/404 funcional.
- **`getById_withoutAuth_returns401()`**: Testea el retorno de 401 GET sin autenticación.
- **`getById_withAuth_acceptsRequest()`**: Testea que GET autenticado supera seguridad retornando 404 por inexistente.
- **`getByPhase_withoutAuth_returns401()`**: Testea el retorno de 401 GET lista por fase sin auth.
- **`getByPhase_withAuth_acceptsRequest()`**: Testea que GET lista por fase autenticado retorna 404 si fase no existe.
- **`getByBatch_withoutAuth_returns401()`**: Testea el retorno de 401 GET lista por lote sin auth.
- **`getByBatch_withAuth_acceptsRequest()`**: Testea que GET lista por lote autenticado retorna 404 si lote no existe.

### Integration Tests

#### ProductionPhaseQualityIntegrationTest

Tests de integración end-to-end para calidades de fases de producción.

- **`getProductionPhaseQuality_notFound()`**: Testea el retorno de 404 GET por ID inexistente.
- **`updateProductionPhaseQuality_notFound()`**: Testea el retorno de 404 PATCH por ID inexistente.
- **`listByPhase_invalidPhase()`**: Testea el retorno de 404 GET por fase inválida (validación de existencia previa).
- **`listByBatch_invalidBatch()`**: Testea el retorno de 404 GET por lote inválido.

---

## Products

### Service Tests

#### ProductServiceTest

Tests unitarios del servicio de productos.

- **`createProduct_isAlcoholic_createsPhases_andPersists()`**: Testea la creación de producto alcohólico generando automáticamente 9 fases (MOLIENDA → ETIQUETADO).
- **`createProduct_notAlcoholic_addsDesalcoholPhase()`**: Testea la inclusión de fase DESALCOHOL en producto no alcohólico.
- **`createProduct_createsWithoutPackaging()`**: Testea la creación exitosa de producto sin packaging (packaging opcional).
- **`markAsReady_whenPhasesNotReady_throwsBadRequest()`**: Testea el rechazo de marcar producto como listo cuando alguna fase no está lista.
- **`markAsReady_whenAllPhasesReady_setsFlagAndSaves()`**: Testea el marcado exitoso de producto como listo cuando todas las fases están listas (isReady=true).
- **`updateProduct_changesNameAndAlcoholicType()`**: Testea la actualización exitosa de nombre y tipo de producto.
- **`toggleActive_invertsFlag()`**: Testea la inversión exitosa del flag active al activar/desactivar producto.
- **`getProduct_returnsMappedDto()`**: Testea la obtención de producto retornando DTO completo mapeado.
- **`findAll_mapsPage()`**: Testea el listado de productos con paginación retornando Page<ProductResponseDTO>.

### Mapper Tests

#### ProductMapperTest

Tests de transformación MapStruct para productos.

- **`toResponseDto_maps_all_expected_fields()`**: Testea el mapeo completo de entidad a DTO (id, name, isAlcoholic, isReady, active, packaging, phases).
- **`partialUpdate_updates_nonNull_fields()`**: Testea la actualización parcial con UpdateDTO actualizando solo campos no-null.

### Controller Tests

#### ProductControllerTest

Tests del controlador REST de productos.

- **`testCreateProduct()`**: Testea la creación exitosa de producto POST /products retornando 201.
- **`testCreateProduct_BadRequest_ShouldReturn400()`**: Testea el retorno de 400 con datos inválidos.
- **`testCreateProduct_MissingName_ShouldReturn400()`**: Testea el retorno de 400 cuando falta campo obligatorio name.
- **`testCreateProduct_MissingIsAlcoholic_ShouldReturn400()`**: Testea el retorno de 400 cuando falta campo obligatorio isAlcoholic.
- **`testMarkAsReady()`**: Testea el marcado exitoso de producto como listo POST /products/{id}/mark-ready.
- **`testMarkAsReady_NotFound_ShouldReturn404()`**: Testea el retorno de 404 al marcar como listo producto inexistente.
- **`testToggleActive()`**: Testea el cambio exitoso de estado PATCH /products/{id}/toggle-active.
- **`testUpdateProduct()`**: Testea la actualización exitosa de producto PATCH /products/{id}.
- **`testGetProducts_PaginationMetadata_ShouldReturnFields()`**: Testea la presencia de metadata de paginación (totalElements, totalPages, size, number) en respuesta.
- **`testUpdateProduct_NotFound_ShouldReturn404()`**: Testea el retorno de 404 al actualizar producto inexistente.

#### ProductControllerSecurityTest

Tests de seguridad del controlador de productos.

- **`getProducts_unauthenticated_returns401()`**: Testea el retorno de 401 GET /products sin auth.
- **`getProducts_authenticated_returns200()`**: Testea el retorno de 200 GET /products con auth.

### Integration Tests

#### ProductIntegrationTest

Tests de integración end-to-end para productos.

- **`productCrudHappyPath()`**: Testea el flujo completo CRUD de crear producto alcohólico, obtenerlo, actualizar, marcar fases como listas, marcar producto como listo y desactivar.

---

## Quality Parameters

### Service Tests

#### QualityParameterServiceImplTest

Tests unitarios del servicio de parámetros de calidad.

- **`testCreateQualityParameter_success()`**: Testea la creación exitosa de parámetro completo verificando persistencia y mapeo.
- **`testCreateQualityParameter_withMinimalData()`**: Testea la creación exitosa de parámetro sin description (opcional).
- **`testUpdateQualityParameter_success()`**: Testea la actualización exitosa de description preservando otros campos.
- **`testUpdateQualityParameter_notFound_throwsException()`**: Testea el lanzamiento de ResourceNotFoundException con ID inexistente.
- **`testGetQualityParameter_success()`**: Testea la obtención exitosa de parámetro por ID.
- **`testGetQualityParameter_notFound_throwsException()`**: Testea el lanzamiento de excepción al obtener inexistente.
- **`testToggleActive_fromTrueToFalse()`**: Testea el cambio de isActive true→false.
- **`testToggleActive_fromFalseToTrue()`**: Testea el cambio de isActive false→true.
- **`testToggleActive_notFound_throwsException()`**: Testea el lanzamiento de excepción al toggle sobre ID inexistente.
- **`testGetQualityParameters_returnsAll()`**: Testea el listado exitoso con resultados.
- **`testGetQualityParameters_emptyList()`**: Testea el retorno de lista vacía cuando no hay parámetros.
- **`testIsActiveDefault_shouldBeTrue()`**: Testea el default isActive=true en @PrePersist.
- **`testCriticalParameter_creation()`**: Testea la conservación del flag isCritical en parámetro crítico.

### Mapper Tests

#### QualityParameterMapperTest

Tests de transformación MapStruct para parámetros de calidad.

- **`testToEntity_fromCreateDTO()`**: Testea la conversión de CreateDTO completo a entidad (phase, isCritical, name, description).
- **`testToEntity_withMinimalData()`**: Testea el mapeo con datos mínimos (sin description) manteniendo null correctamente.
- **`testToResponseDTO()`**: Testea la conversión de entidad activa y crítica a ResponseDTO preservando flags.
- **`testToResponseDTO_withInactiveParameter()`**: Testea el mapeo cuando isActive=false.
- **`testToResponseDTO_handlesNullDescription()`**: Testea que description null se mantiene null en el DTO.
- **`testListMapping()`**: Testea el mapeo de lista de entidades a lista de DTOs.
- **`testMapping_preservesCriticalFlag()`**: Testea la conservación del flag isCritical (true y false).

### Repository Tests

#### QualityParameterRepositoryTest

Tests de consultas JPA del repositorio de parámetros de calidad.

- **`findByIsActiveTrueOrderByNameAsc_returnsOnlyActiveOrdered()`**: Testea el retorno de solo parámetros activos ordenados por name.
- **`findByPhaseAndIsActiveTrueOrderByNameAsc_filtersByPhase()`**: Testea el filtrado correcto por phase y por isActive.

#### QualityParameterControllerActiveTest

Tests del endpoint de parámetros activos.

- **`getActiveWithoutPhase_returnsList()`**: Testea el retorno de lista mockeando el servicio y verificando respuesta HTTP 200 y contenido.
- **`getActiveWithPhase_callsFilteredService()`**: Testea el procesamiento correcto del parámetro phase mockeando el servicio para la fase solicitada.

### Controller Tests

#### QualityParameterControllerTest

Tests del controlador REST de parámetros de calidad.

- **`testCreateQualityParameter_validRequest_returns201()`**: Testea la creación exitosa POST retornando 201 con campos esperados.
- **`testCreateQualityParameter_missingName_returns400()`**: Testea el retorno de 400 cuando falta name.
- **`testCreateQualityParameter_missingPhase_returns400()`**: Testea el retorno de 400 cuando falta phase.
- **`testCreateQualityParameter_missingIsCritical_returns400()`**: Testea el retorno de 400 cuando falta isCritical.
- **`testCreateQualityParameter_invalidPhase_returns500()`**: Testea el retorno de 500 con enum inválido (comportamiento actual de manejo de enums inválidos).
- **`testCreateQualityParameter_nameTooLong_returns400()`**: Testea la validación de longitud name.
- **`testUpdateQualityParameter_validRequest_returns200()`**: Testea la actualización exitosa PATCH retornando 200.
- **`testUpdateQualityParameter_descriptionTooLong_returns400()`**: Testea el retorno de 400 cuando description excede longitud.
- **`testGetQualityParameter_exists_returns200()`**: Testea la obtención exitosa GET existente retornando 200.
- **`testGetQualityParameter_notFound_returns404()`**: Testea el retorno de 404 GET inexistente.
- **`testToggleActive_success_returns200()`**: Testea el toggle exitoso activo/desactivo.
- **`testToggleActive_notFound_returns404()`**: Testea el retorno de 404 toggle sobre ID inexistente.
- **`testListQualityParameters_returns200()`**: Testea el listado exitoso con elementos.
- **`testListQualityParameters_emptyList_returns200()`**: Testea el retorno de lista vacía.
- **`testCreateQualityParameter_withNullDescription_success()`**: Testea la creación exitosa sin description.

#### QualityParameterControllerSecurityTest

Tests de seguridad del controlador de parámetros de calidad.

- **`testCreate_withoutAuth_returns401()`**: Testea el retorno de 401 POST sin auth.
- **`testCreate_withWrongRole_returns403()`**: Testea el retorno de 403 con rol no autorizado.
- **`testCreate_withCorrectRole_success()`**: Testea la creación exitosa con rol SUPERVISOR_DE_CALIDAD retornando 201.
- **`testUpdate_withoutAuth_returns401()`**: Testea el retorno de 401 PATCH sin auth.
- **`testUpdate_withWrongRole_returns403()`**: Testea el retorno de 403 PATCH rol incorrecto.
- **`testUpdate_withCorrectRole_successOrNotFound()`**: Testea que PATCH autorizado retorna 200 o 404 funcional.
- **`testToggleActive_withoutAuth_returns401()`**: Testea el retorno de 401 toggle sin auth.
- **`testToggleActive_withWrongRole_returns403()`**: Testea el retorno de 403 toggle rol incorrecto.
- **`testToggleActive_withCorrectRole_success()`**: Testea el toggle exitoso autorizado.
- **`testGet_withAnyRole_success()`**: Testea la accesibilidad de GET listado con cualquier rol autenticado.
- **`testGet_withDifferentRole_success()`**: Testea que otro rol autenticado también accede.
- **`testGet_withoutAuth_returns401()`**: Testea el retorno de 401 GET sin autenticación (acceso público deshabilitado actualmente).
- **`testCreate_withMultipleRoles_success()`**: Testea la creación exitosa POST con múltiples roles válidos.

### Integration Tests

#### QualityParameterIntegrationTest

Tests de integración end-to-end para parámetros de calidad.

- **`createQualityParameter_andGetById_happyPath()`**: Testea la creación y recuperación exitosa por ID.
- **`createMultipleParameters_andListAll_happyPath()`**: Testea varias creaciones y verificación en listado.
- **`createParameter_toggleActive_andVerify()`**: Testea el toggle persistente (true→false→true) verificado por GET.
- **`updateParameter_description_success()`**: Testea la actualización de description preservando otros campos.
- **`createParameter_withMinimalData_success()`**: Testea la creación exitosa sin description.
- **`createParameter_withSameName_differentPhase_success()`**: Testea la permisión de mismo nombre en fases distintas.
- **`getParameter_notFound_returns404()`**: Testea el retorno de 404 GET inexistente.
- **`updateParameter_notFound_returns404()`**: Testea el retorno de 404 PATCH inexistente.
- **`toggleActive_notFound_returns404()`**: Testea el retorno de 404 toggle inexistente.
- **`createCriticalParameter_andVerify()`**: Testea la creación de parámetro crítico y verificación del flag.

---

## Recipes

### Service Tests

#### RecipeServiceImplTest

Tests unitarios del servicio de recetas.

- **`createRecipe_success()`**: Testea la creación exitosa de una receta.
- **`createRecipe_materialNotAllowed_throwsBadRequest()`**: Testea el fallo cuando el material no está permitido en esa fase.
- **`createRecipe_productPhaseNotFound_throwsNotFound()`**: Testea el fallo cuando la fase del producto no existe.
- **`createRecipe_materialNotFound_throwsNotFound()`**: Testea el fallo cuando el material no existe.
- **`updateRecipe_success()`**: Testea la actualización exitosa de una receta.
- **`updateRecipe_notFound_throws()`**: Testea el fallo al actualizar receta inexistente.
- **`deleteRecipe_success()`**: Testea la eliminación exitosa de una receta.
- **`deleteRecipe_notFound_throws()`**: Testea el fallo al eliminar receta inexistente.
- **`getRecipe_success()`**: Testea la obtención exitosa de una receta por ID.
- **`getRecipe_notFound_throws()`**: Testea el fallo cuando la receta no existe.
- **`getRecipeList_success()`**: Testea el listado exitoso de todas las recetas.
- **`getMaterialByPhase_success()`**: Testea la obtención exitosa de materiales por fase de producto.
- **`getMaterialByPhase_productPhaseNotFound_throws()`**: Testea el fallo cuando la fase no existe.
- **`getMaterialByProduct_success()`**: Testea la obtención exitosa de materiales por producto.
- **`getMaterialByProduct_emptyList()`**: Testea el retorno de lista vacía cuando no hay materiales.
- **`getRecipeByProduct_success()`**: Testea la obtención exitosa de recetas por producto.
- **`getRecipeByProduct_emptyList()`**: Testea el retorno de lista vacía cuando no hay recetas.
- **`createRecipe_withOtrosType_shouldAllowAnyPhase()`**: Testea la permisión de materiales tipo "Otros" en cualquier fase.

### Mapper Tests

#### RecipeMapperTest

Tests de transformación MapStruct para recetas.

- **`testToEntity()`**: Testea el mapeo de DTO a entidad.
- **`testToResponseDTO_mapsMaterialFields()`**: Testea el mapeo correcto de campos del material.

### Repository Tests

#### RecipeRepositoryTest

Tests de consultas JPA del repositorio de recetas.

- **Consultas JPA**: Testea findByProductPhase_ProductId, findByProductPhase, existsByMaterial_Type y existsByProductPhaseIdAndMaterial_Type con fixtures reales en H2.

#### RecipeControllerByProductTest

Tests de endpoints de recetas por producto.

- **Endpoints por producto**: Testea GET /recipes/by-product-phase/{id} y GET /recipes/by-product/{id} usando MockMvc y RecipeService simulado.

### Controller Tests

#### RecipeControllerTest

Tests del controlador REST de recetas.

- **`createRecipe_success()`**: Testea la creación exitosa de receta vía API retornando 201.
- **`updateRecipe_success()`**: Testea la actualización exitosa de receta retornando 200.
- **`deleteRecipe_success()`**: Testea la eliminación exitosa de receta retornando 204.
- **`getRecipe_success()`**: Testea la obtención exitosa de receta por ID retornando 200.
- **`getRecipeList_success()`**: Testea el listado exitoso de recetas retornando 200.

#### RecipeControllerSecurityTest

Tests de seguridad del controlador de recetas.

- **`getRecipeList_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan acceder.
- **`getRecipeList_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados acceden.

### Integration Tests

#### RecipeIntegrationTest

Tests de integración end-to-end de recetas.

- **`createRecipe_andGetById_happyPath()`**: Testea el flujo completo de crear y obtener una receta.
- **`updateRecipe_happyPath()`**: Testea el flujo completo de actualización de receta.
- **`getRecipesByProductPhase_happyPath()`**: Testea la obtención exitosa de recetas filtradas por fase de producto.

---

## Sectors

### Service Tests

#### SectorServiceImplTest

Tests unitarios del servicio de sectores.

- **`createSector_withValidProduccionData_success()`**: Testea la creación exitosa de un sector de producción.
- **`createSector_withValidAlmacenData_success()`**: Testea la creación exitosa de un sector de almacén.
- **`createSector_withNonExistentSupervisor_throwsResourceNotFoundException()`**: Testea el fallo cuando el supervisor no existe.
- **`createSector_withWrongSupervisorRole_throwsBadRequestException()`**: Testea el fallo cuando el supervisor no tiene el rol correcto.
- **`createSector_produccionWithoutRequiredFields_throwsBadRequestException()`**: Testea el fallo cuando faltan campos requeridos en sector de producción.
- **`getSector_withValidId_success()`**: Testea la obtención exitosa de un sector por ID.
- **`getSector_withInvalidId_throwsResourceNotFoundException()`**: Testea el fallo cuando el sector no existe.
- **`updateSector_withValidData_success()`**: Testea la actualización exitosa de un sector.
- **`updateSector_withInvalidId_throwsResourceNotFoundException()`**: Testea el fallo al actualizar sector inexistente.
- **`updateSector_changingSupervisorWithWrongRole_throwsBadRequestException()`**: Testea el fallo al cambiar supervisor con rol incorrecto.
- **`getAllSectorsAvailableByPhase_returnsOrderedList()`**: Testea el retorno de sectores de PRODUCCIÓN disponibles para una fase ordenados por menor carga actual (actualProduction ASC).
- **`getAllSectorsAvailableByPhase_emptyList_returnsEmpty()`**: Testea el retorno de lista vacía cuando no hay sectores disponibles.
- **`saveAll_delegatesToRepository()`**: Testea la delegación correcta de operación en lote al repositorio.
- **`createSector_produccion_setsIsActiveAndCreationDate()`**: Testea que en creación de sector de PRODUCCIÓN el servicio setea isActive=true y creationDate automáticamente.
- **`createSector_produccion_missingOnlyCapacity_throwsBadRequest()`**: Testea el lanzamiento de 400 cuando falta exclusivamente productionCapacity por validación de negocio.
- **`updateSector_toAlmacen_withProductionFields_throwsBadRequest()`**: Testea el rechazo (400) al cambiar a ALMACEN enviando campos de PRODUCCIÓN (phase/capacity/isTimeActive).

### Mapper Tests

#### SectorMapperTest

Tests de transformación MapStruct para sectores.

- **`testToEntity_fromCreateDTO()`**: Testea el mapeo de DTO de creación a entidad.
- **`testToResponseDTO_mapsSupervisorIdCorrectly()`**: Testea el mapeo correcto del ID del supervisor.
- **`testToResponseDTO_withProduccionFields()`**: Testea el mapeo correcto de campos específicos de producción.

### Model Tests

#### SectorModelTest

Tests de la entidad Sector validando lifecycle y helpers.

- **Lifecycle y helpers**: Testea callbacks PrePersist, métodos increase/decrease actual production.

### Repository Tests

#### SectorRepositoryTest

Tests de consultas JPA del repositorio de sectores.

- **`findAvailableProductionSectorsByPhase`**: Testea filtros JPQL y ordenamiento (excluye sectores llenos, ordena por actualProduction).

### Controller Tests

#### SectorControllerSecurityTest

Tests de seguridad del controlador de sectores.

- **`getSector_unauthenticated_returns401()`**: Testea el retorno de 401 cuando usuarios no autenticados intentan acceder.
- **`getSector_authenticated_returns200()`**: Testea el retorno de 200 cuando usuarios autenticados acceden.

#### SectorControllerCreateUpdateTest

Tests de creación y actualización de sectores.

- **`POST /sectors happy-path`**: Testea la creación exitosa retornando 201 usando Mockito-provided SectorService test bean.
- **`PATCH /sectors/{id} happy-path`**: Testea la actualización exitosa retornando 200 usando Mockito-provided SectorService test bean.

### Integration Tests

#### SectorIntegrationTest

Tests de integración end-to-end de sectores.

- **`getSector_asAuthenticatedUser_returns404NotFound()`**: Testea el retorno de 404 cuando el sector no existe.
- **`createSector_withNonExistentSupervisor_returns404()`**: Testea el retorno de 404 cuando el supervisor no existe.
- **`createSector_withUnauthorizedRole_returns403()`**: Testea el retorno de 403 cuando usuarios sin autorización intentan crear.
- **`updateSector_withUnauthorizedRole_returns403()`**: Testea el retorno de 403 cuando usuarios sin autorización intentan actualizar.
- **`createSector_produccion_missingRequiredFields_returns400()`**: Testea el retorno de 400 al crear sector de PRODUCCIÓN sin los campos requeridos.
- **`createSector_produccion_withNonExistentSupervisor_returns404()`**: Testea el retorno de 404 en variante específica de PRODUCCIÓN con supervisor inexistente.
- **`updateSector_nonExistentId_returns404()`**: Testea el retorno de 404 al actualizar un ID de sector inexistente.

---

## System Configuration

### Service Tests

#### SystemConfigurationServiceImplTest

Tests unitarios del servicio de configuración del sistema.

- **`getSystemConfiguration_createsDefaultWhenNoneExists()`**: Testea la creación de configuración con isActive=true y 7 WorkingDay cuando no existe configuración activa.
- **`updateWorkingDays_appliesPartialUpdateAndSaves()`**: Testea la aplicación de WorkingDayMapper.partialUpdate solo sobre los días enviados persistiendo la configuración.
- **`getWorkingDays_returnsMapByDayOfWeek()`**: Testea la construcción correcta de Map<DayOfWeek, WorkingDay>.
- **`getSystemConfiguration_returnsExistingWithoutCreating()`**: Testea que devuelve el DTO mapeado sin persistir nueva configuración cuando existe una activa.
- **`updateWorkingDays_throwsWhenNoActiveConfig()`**: Testea el lanzamiento de ResourceNotFoundException cuando no hay configuración activa.
- **`createdWorkingDays_referenceParentConfiguration()`**: Testea que los WorkingDay creados en configuración default referencian la SystemConfiguration padre.

### Mapper Tests

#### SystemConfigurationMapperTest

Tests de transformación MapStruct para configuración del sistema.

- **`toResponseDto_mapsBasicFields_andWorkingDays()`**: Testea el mapeo de isActive y lista workingDays correctamente.

#### WorkingDayMapperTest

Tests de transformación MapStruct para días laborables.

- **`partialUpdate_shouldOnlyUpdateNonNullFields()`**: Testea que campos null en WorkingDayUpdateDTO no sobrescriben valores existentes.

### Repository Tests

#### SystemConfigurationRepositoryTest

Tests de consultas JPA del repositorio de configuración del sistema.

- **`findFirstByIsActiveTrueOrderByIdDesc_returnsLatestActiveWithWorkingDays()`**: Testea el retorno de última configuración activa junto con sus workingDays.

### DTO Tests

#### SystemConfiguration DTO Serialization

Tests de serialización/deserialización JSON de DTOs.

- **`workingDayUpdateDto_serializesAndDeserializesLocalTime()`**: Testea el formato y parseo de LocalTime al serializar/deserializar WorkingDayUpdateDTO.
- **`systemConfigurationResponseDto_serializesWorkingDays()`**: Testea la serialización del flag isActive y la lista workingDays en SystemConfigurationResponseDTO.

### Controller Tests

#### SystemConfigurationControllerSecurityTest

Tests de seguridad del controlador de configuración del sistema.

- **`get_withoutAuth_returns401()`**: Testea el retorno de 401 GET /system-configurations sin auth.
- **`get_withWrongRole_returns403()`**: Testea el retorno de 403 GET con rol incorrecto.
- **`get_withAdmin_acceptsRequest()`**: Testea que GET con rol ADMIN permite acceso funcional (200 o 4xx funcional, nunca 401/403).
- **`patch_withoutAuth_returns401()`**: Testea el retorno de 401 PATCH /system-configurations/working-days sin auth.
- **`patch_withWrongRole_returns403()`**: Testea el retorno de 403 PATCH con rol no-admin.
- **`patch_withAdmin_acceptsRequest()`**: Testea que PATCH con ADMIN acepta la petición funcionalmente (200 o 4xx funcional).

### Integration Tests

#### SystemConfigurationIntegrationTest

Tests de integración end-to-end para configuración del sistema.

- **`getAndPatchEndpoints_flowWorks_whenDataLoaderMocked()`**: Testea que GET /system-configurations crea configuración por defecto cuando no existe y que PATCH /system-configurations/working-days aplica cambios y persiste.

---

## Users

### Service Tests

#### UserServiceImplTest

Tests unitarios del servicio de usuarios.

- **`createUser_success()`**: Testea la creación exitosa de usuario con username, email, password (encriptado) y roles.
- **`createUser_setsDefaultValues()`**: Testea el establecimiento correcto de valores por defecto (active=true).
- **`toggleActive_whenActive_disablesAndInvalidatesSessions()`**: Testea la desactivación de usuario activo invalidando todas sus sesiones.
- **`toggleActive_whenInactive_enables()`**: Testea la reactivación de usuario inactivo.
- **`updateUser_success()`**: Testea la actualización parcial exitosa de campos (name, email) preservando password.
- **`updateUser_notFound_throws()`**: Testea el lanzamiento de ResourceNotFoundException al actualizar usuario inexistente.
- **`updateUserRole_success()`**: Testea el cambio exitoso de roles de usuario (OPERATOR → ADMIN).
- **`updateUserPassword_success()`**: Testea el cambio exitoso de contraseña con encriptación BCrypt.
- **`getUserById_success()`**: Testea la obtención exitosa de usuario por ID retornando DTO completo.
- **`getUserById_notFound_throws()`**: Testea el lanzamiento de excepción cuando usuario no existe.
- **`getUserByUsername_success()`**: Testea la búsqueda exitosa de usuario por username para autenticación.
- **`findAll_returnsPage()`**: Testea el listado de usuarios con paginación y ordenamiento.
- **`loadUserByUsername_registersSseCache_and_returnsUserDetails_evenIfSseFails()`**: Testea que loadUserByUsername registra en cache SSE y que si el registro falla la autenticación continúa sin lanzar.
- **`getCurrentUser_returnsNullWhenNoAuth_and_returnsUserWhenAuthenticated()`**: Testea el comportamiento de getCurrentUser con y sin SecurityContext.

### Mapper Tests

#### UserMapperTest

Tests de transformación MapStruct para usuarios.

- **`toResponseDto_mapsRolesToStrings()`**: Testea la conversión de Set<Role> a List<String> en DTO.
- **`toUserDetailDTO_mapsRolesToStrings()`**: Testea el mapeo a DTO detallado con roles como strings.
- **`updateUserRoles_replacesRoles()`**: Testea el reemplazo completo de roles al actualizar.
- **`updateUserPassword_setsPassword()`**: Testea la actualización de password en entidad existente.

### Controller Tests

#### UserControllerTest

Tests del controlador REST de usuarios.

- **`testCreateUser_success()`**: Testea la creación exitosa de usuario POST /users retornando 201.
- **`testToggleActive_success()`**: Testea el cambio exitoso de estado PATCH /users/{id}/toggle-active.
- **`testUpdateUser_success()`**: Testea la actualización exitosa de datos personales PATCH /users/{id}.
- **`testUpdateUserRole_success()`**: Testea el cambio exitoso de roles PATCH /users/{id}/roles (requiere ADMIN).
- **`testUpdateUserPassword_success()`**: Testea el cambio exitoso de contraseña PATCH /users/{id}/password.
- **`testGetUserById_success()`**: Testea la obtención exitosa de usuario específico GET /users/{id}.
- **`testFindAll_withPagination()`**: Testea el listado con parámetros page/size/sort GET /users.

#### UserControllerValidationTest

Tests de validación del controlador de usuarios.

- **`testCreateUser_missingUsername_returns400()`**: Testea el retorno de 400 cuando falta username obligatorio.
- **`testCreateUser_missingPassword_returns400()`**: Testea el retorno de 400 cuando falta password obligatorio.
- **`testCreateUser_missingRole_returns400()`**: Testea el retorno de 400 cuando falta role obligatorio.
- **`testUpdateUserPassword_mismatch_returns400()`**: Testea el retorno de 400 cuando password y confirmPassword no coinciden.

#### UserSecurityTest

Tests de lógica de seguridad de usuarios.

- **`isSelf_returnsTrue_whenIdsMatch()`**: Testea que usuario autenticado puede ver sus propios datos.
- **`isSelf_returnsFalse_whenAnonymous()`**: Testea que usuario anónimo no es "self".
- **`canDeactivateUser_adminCannotToggleSelf_orOtherAdmin_butCanToggleNonAdmin()`**: Testea las reglas de ADMIN no puede desactivar a sí mismo ni a otros ADMIN, pero sí a roles menores.

### Integration Tests

#### UserRoleAuthorizationMatrixIntegrationTest

Tests de matriz de autorización por roles.

- **`createUser_requiresAdmin(Role role)`**: Testea que POST /users solo permite ADMIN, otros roles retornan 403.
- **`updateUserRole_requiresAdmin(Role role)`**: Testea que PATCH /users/{id}/roles solo ADMIN.
- **`listUsers_requiresAdmin(Role role)`**: Testea que GET /users solo ADMIN puede listar todos los usuarios.
- **`updateUser_selfOrAdmin(Role role)`**: Testea que PATCH /users/{id} permite al propio usuario o ADMIN.
- **`updatePassword_selfOrAdmin(Role role)`**: Testea que PATCH /users/{id}/password permite self o ADMIN.
- **`getUser_selfOrAdmin(Role role)`**: Testea que GET /users/{id} permite self o ADMIN.
- **`admin_canToggle_nonAdmin()`**: Testea que ADMIN puede desactivar OPERATOR/MANAGER/VIEWER.
- **`admin_cannotToggle_otherAdmin()`**: Testea que ADMIN no puede desactivar a otro ADMIN (retorna 403).
- **`nonAdmin_cannotToggle_any(Role role)`**: Testea que roles no-ADMIN no pueden desactivar a nadie.

#### UserPaginationIntegrationTest

Tests de paginación y ordenamiento de usuarios.

- **`findAll_defaultPagination_returnsFirstPage()`**: Testea el retorno de página 0 con tamaño por defecto sin parámetros.
- **`findAll_customPageSize_returnsCorrectNumberOfElements()`**: Testea que parámetro size controla cantidad de elementos.
- **`findAll_sortByNameDesc_returnsSortedResults()`**: Testea el funcionamiento de ordenamiento descendente por nombre.
- **`findAll_outOfRangePage_returnsEmptyContent()`**: Testea el retorno de content vacío pero con metadata correcta cuando página está fuera de rango.
- **`toggleActive_success_togglesActiveStatus()`**: Testea la persistencia correcta del cambio de estado activo.

#### UserValidationIntegrationTest

Tests de validación a nivel de integración.

- **`createUser_invalidPayload_returns400()`**: Testea el retorno de 400 con JSON mal formado o campos inválidos.
- **`createUser_weakPassword_returns400()`**: Testea el retorno de 400 con password débil (menos de 8 caracteres).
- **`createUser_emptyRoles_returns400()`**: Testea el retorno de 400 con lista de roles vacía.
- **`createUser_invalidEmail_returns400()`**: Testea el retorno de 400 con email con formato inválido.
- **`updateUser_invalidPayload_returns400()`**: Testea el retorno de 400 con datos de actualización inválidos.
- **`updatePassword_weakPassword_returns400()`**: Testea el retorno de 400 con nueva contraseña débil.

### Auth Tests

#### AuthControllerTest

Tests del controlador de autenticación.

- **`login_blocked_throws()`**: Testea el lanzamiento de BlockedUserException cuando LoginAttemptService.isBlocked es true.
- **`login_success()`**: Testea el retorno de AuthResponseDTO con credenciales válidas registrando loginSuccess.
- **`logout_clearsSseAndInvalidatesSession()`**: Testea las llamadas a SseNotificationService y que la sesión HTTP se invalida.
- **`me_and_validate()`**: Testea que me() retorna 401 sin auth y 200 con auth.

---

## Warehouse

### Service Tests

#### WarehouseLayoutServiceTest

Tests unitarios de la lógica del mapa del almacén.

- **`isValidLocation_nulls()`**: Testea el retorno de false con entradas nulas.
- **`isValidLocation_invalidSectionOrLevel()`**: Testea el rechazo de secciones/levels inválidos.
- **`isValidLocation_valid()`**: Testea la aceptación de combinaciones válidas.
- **`calculateCoordinates_levelsOffset()`**: Testea los offsets por nivel para una sección conocida.
- **`calculateCoordinates_unknownSection()`**: Testea el comportamiento cuando la sección no está mapeada.

### Mapper Tests

#### WarehouseCoordinateMapperTest

Tests del parseo de secciones y cálculo de coordenadas.

- **`calculateCoordinates_nullInputs()`**: Testea el rechazo de inputs nulos.
- **`calculateCoordinates_parseA1()`**: Testea el parseo de "A1" y cálculo de coordenadas válidas.
- **`calculateCoordinates_invalidFormat()`**: Testea el manejo de formatos de sección inválidos.
- **`calculateCoordinates_sectionOutOfRange()`**: Testea el rechazo de secciones fuera del rango de la zona.

### Controller Tests

#### WarehouseControllerTest

Tests del controlador REST de almacén.

- **`getZones_returnsZones()`**: Testea el retorno de zonas y secciones esperadas GET /warehouse/zones.
- **`validateLocation_delegatesAndReturns()`**: Testea la delegación al servicio y retorno de isValid POST /warehouse/validate-location.

---

