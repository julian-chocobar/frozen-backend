# Documentación Completa de Tests - Frozen Backend

Este documento describe todos los tests del proyecto organizados por módulo funcional.

---

## Batches Tests

### BatchServiceImplTest
**Propósito**: Pruebas unitarias de la lógica de negocio del servicio de lotes (batches), validando operaciones CRUD y reglas de negocio.

#### Funciones de Test:
- **`canCreateBatch()`**: Verifica que se puede crear un lote con propiedades básicas válidas.
- **`canChangeBatchStatus()`**: Valida que el estado de un lote puede ser modificado correctamente.
- **`batchQuantityCannotBeNegative()`**: Asegura que no se permita crear lotes con cantidades negativas.
- **`batchCodeShouldNotBeNull()`**: Verifica que el código de lote es un campo obligatorio.

---

### BatchServiceEstimationTest
**Propósito**: Pruebas de integración del servicio de estimación de tiempo de producción para lotes, utilizando DateUtil para calcular fechas de fin considerando fases activas/pasivas, días laborables y fines de semana.

#### Funciones de Test:
- **`estimateEndDate_withActivePhases()`**: Calcula fecha de fin para fases activas cortas (5 horas) dentro del mismo día laborable.
- **`estimateEndDate_withPassivePhases()`**: Verifica el cálculo con fases mixtas (activa + 48h fermentación pasiva) que funciona 24/7.
- **`estimateEndDate_crossingWeekend()`**: Valida que una fase activa de 15 horas que cruza fin de semana excluye sábado/domingo correctamente.
- **`estimateEndDate_realBeerProduction()`**: Escenario completo de producción de cerveza con múltiples fases (molienda, maceración, cocción, fermentación, maduración, envasado).
- **`estimateEndDate_startingWeekend()`**: Asegura que si el inicio es fin de semana, mueve la fecha al siguiente lunes a las 8:00.
- **`estimateEndDate_withDecimalHours()`**: Prueba cálculos con horas decimales (2.5h) para mayor precisión.
- **`estimateEndDate_multipleWeeks()`**: Valida fases que abarcan múltiples semanas con periodos de fermentación largos.

---

### BatchMapperTest
**Propósito**: Validación de la transformación MapStruct entre entidades Batch y DTOs.

#### Funciones de Test:
- **`mapsBatchToResponseDTO()`**: Verifica que todos los campos de la entidad Batch se mapean correctamente al DTO de respuesta.

---

### BatchControllerTest
**Propósito**: Tests del controlador de lotes con MockMvc, validando endpoints REST con autenticación mock.

#### Funciones de Test:
- **`getAllBatchesReturnsOk()`**: Verifica que GET /batches retorna status 200 con autenticación.
- **`getBatchByIdReturnsOk()`**: Asegura que GET /batches/{id} retorna 200 cuando el lote existe.

---

### BatchControllerSecurityTest
**Propósito**: Pruebas de seguridad del controlador, verificando que los endpoints requieren autenticación.

#### Funciones de Test:
- **`getAllBatchesRequiresAuth()`**: Valida que GET /batches sin autenticación retorna 401 Unauthorized.
- **`getAllBatchesWithValidAuthReturnsOk()`**: Confirma que con autenticación válida se obtiene 200 OK.

---

### BatchIntegrationTest
**Propósito**: Tests de integración completos con @SpringBootTest, validando flujo end-to-end con base de datos.

#### Funciones de Test:
- **`getBatchById_happyPath()`**: Flujo completo: crear lote, recuperarlo por ID y verificar datos.
- **`listBatches_happyPath()`**: Crear múltiples lotes y obtener listado paginado.
- **`listBatches_withPagination()`**: Valida parámetros de paginación (page, size, sort) funcionan correctamente.

---

## Materials Tests

### MaterialServiceImplTest
**Propósito**: Pruebas unitarias del servicio de materiales, validando lógica de negocio con Mockito.

#### Funciones de Test:
- **`testSaveMaterial()`**: Verifica la creación de un material con todos sus campos (nombre, tipo, stock, umbral).
- **`testUpdateMaterial_Success()`**: Valida actualización parcial de material existente.
- **`testUpdateMaterial_NotFound()`**: Asegura que actualizar material inexistente lanza ResourceNotFoundException.
- **`testToggleActive_Success()`**: Verifica que se puede activar/desactivar material.
- **`testGetMaterial_Success()`**: Obtener material por ID retorna DTO mapeado correctamente.
- **`testFindAll_ReturnsPage()`**: Listar materiales con paginación retorna Page correctamente.
- **`testGetMaterialSimpleList()`**: Obtener lista simple de materiales activos para selectores.

---

### MaterialMapperTest
**Propósito**: Validación de MapStruct mapper para conversiones Material entity ↔ DTOs.

#### Funciones de Test:
- **`testToResponseDto_isBelowThresholdTrue()`**: Mapeo a DTO cuando stock está por debajo del umbral (campo calculado isBelowThreshold=true).
- **`testToResponseDto_isBelowThresholdFalse()`**: Mapeo cuando stock es suficiente (isBelowThreshold=false).
- **`testToEntityFromCreateDTO()`**: Crear entidad desde CreateDTO con validaciones.
- **`testToDetailDto_isBelowThresholdTrue()`**: Mapeo a DTO detallado con cálculo de umbral.
- **`testPartialUpdate_IgnoresNullsAndUpdatesNonNulls()`**: Actualización parcial ignora campos null y actualiza solo los provistos.

---

### MaterialControllerTest
**Propósito**: Tests del controlador REST de materiales con MockMvc.

#### Funciones de Test:
- **`testGetMaterial_NotFound_ShouldReturn404()`**: GET /materials/{id} inexistente retorna 404.
- **`testCreateMaterial_BadRequest_ShouldReturn409()`**: Crear material con datos inválidos retorna 409 Conflict.
- **`testCreateMaterial()`**: POST /materials con datos válidos retorna 201 Created.
- **`testCreateMaterial_MissingName_ShouldReturnBadRequest()`**: Validación de campo obligatorio name retorna 400.
- **`testCreateMaterial_NegativeStock_ShouldReturnBadRequest()`**: Stock negativo retorna 400 Bad Request.
- **`testCreateMaterial_InvalidType_ShouldReturnBadRequest()`**: Tipo de material inválido retorna 400.
- **`testUpdateMaterial()`**: PATCH /materials/{id} actualiza correctamente.
- **`testToggleActive()`**: PATCH /materials/{id}/toggle-active cambia estado activo.
- **`testGetMaterials()`**: GET /materials retorna listado paginado.
- **`testGetMaterial()`**: GET /materials/{id} retorna material específico.

---

### MaterialControllerSecurityTest
**Propósito**: Tests de seguridad para endpoints de materiales.

#### Funciones de Test:
- **`getMaterials_unauthenticated_returns401()`**: GET /materials sin auth retorna 401.
- **`getMaterials_authenticated_returns200()`**: GET /materials con auth válida retorna 200.

---

### MaterialIntegrationTest
**Propósito**: Tests de integración end-to-end para materiales.

#### Funciones de Test:
- **`createMaterial_andGetById_happyPath()`**: Crear material y recuperarlo verificando persistencia.
- **`listMaterials_happyPath()`**: Listar materiales con paginación y filtros.
- **`updateMaterial_happyPath()`**: Actualización completa de material existente.
- **`toggleMaterialActive_happyPath()`**: Activar/desactivar material y verificar cambio.

---

## Users Tests

### UserServiceImplTest
**Propósito**: Pruebas unitarias exhaustivas del servicio de usuarios, incluyendo gestión de roles, sesiones y seguridad.

#### Funciones de Test:
- **`createUser_success()`**: Crear usuario con username, email, password (encriptado) y roles.
- **`createUser_setsDefaultValues()`**: Verificar que valores por defecto (active=true) se establecen correctamente.
- **`toggleActive_whenActive_disablesAndInvalidatesSessions()`**: Desactivar usuario activo invalida todas sus sesiones.
- **`toggleActive_whenInactive_enables()`**: Reactivar usuario inactivo.
- **`updateUser_success()`**: Actualización parcial de campos (name, email) preservando password.
- **`updateUser_notFound_throws()`**: Actualizar usuario inexistente lanza ResourceNotFoundException.
- **`updateUserRole_success()`**: Cambiar roles de usuario (OPERATOR → ADMIN).
- **`updateUserPassword_success()`**: Cambiar contraseña con encriptación BCrypt.
- **`getUserById_success()`**: Obtener usuario por ID retorna DTO completo.
- **`getUserById_notFound_throws()`**: Usuario inexistente lanza excepción.
- **`getUserByUsername_success()`**: Buscar usuario por username para autenticación.
- **`findAll_returnsPage()`**: Listar usuarios con paginación y ordenamiento.

---

### UserControllerTest
**Propósito**: Tests del controlador REST de usuarios.

#### Funciones de Test:
- **`testCreateUser_success()`**: POST /users crea usuario retornando 201.
- **`testToggleActive_success()`**: PATCH /users/{id}/toggle-active cambia estado.
- **`testUpdateUser_success()`**: PATCH /users/{id} actualiza datos personales.
- **`testUpdateUserRole_success()`**: PATCH /users/{id}/roles cambia roles (requiere ADMIN).
- **`testUpdateUserPassword_success()`**: PATCH /users/{id}/password cambia contraseña.
- **`testGetUserById_success()`**: GET /users/{id} retorna usuario específico.
- **`testFindAll_withPagination()`**: GET /users con parámetros page/size/sort.

---

### UserControllerValidationTest
**Propósito**: Validación de entrada en endpoints de usuarios (Bean Validation).

#### Funciones de Test:
- **`testCreateUser_missingUsername_returns400()`**: Username obligatorio, su ausencia retorna 400.
- **`testCreateUser_missingPassword_returns400()`**: Password obligatorio.
- **`testCreateUser_missingRole_returns400()`**: Role obligatorio.
- **`testUpdateUserPassword_mismatch_returns400()`**: Password y confirmPassword deben coincidir.

---

### UserMapperTest
**Propósito**: Tests del mapper User entity ↔ DTOs.

#### Funciones de Test:
- **`toResponseDto_mapsRolesToStrings()`**: Convertir Set<Role> a List<String> en DTO.
- **`toUserDetailDTO_mapsRolesToStrings()`**: Mapeo a DTO detallado con roles como strings.
- **`updateUserRoles_replacesRoles()`**: Actualizar roles reemplaza completamente la colección.
- **`updateUserPassword_setsPassword()`**: Actualizar password en entidad existente.

---

### UserSecurityTest
**Propósito**: Tests de lógica de seguridad de usuarios (método `isSelf`, `canDeactivateUser`).

#### Funciones de Test:
- **`isSelf_returnsTrue_whenIdsMatch()`**: Usuario autenticado puede ver sus propios datos.
- **`isSelf_returnsFalse_whenAnonymous()`**: Usuario anónimo no es "self".
- **`canDeactivateUser_adminCannotToggleSelf_orOtherAdmin_butCanToggleNonAdmin()`**: Validar reglas: ADMIN no puede desactivar a sí mismo ni a otros ADMIN, pero sí a roles menores.

---

### UserRoleAuthorizationMatrixIntegrationTest
**Propósito**: Tests de matriz de autorización por roles (ADMIN, MANAGER, OPERATOR, VIEWER). Verifica que cada endpoint respeta permisos según rol.

#### Funciones de Test:
- **`createUser_requiresAdmin(Role role)`**: POST /users solo permite ADMIN, otros roles retornan 403.
- **`updateUserRole_requiresAdmin(Role role)`**: PATCH /users/{id}/roles solo ADMIN.
- **`listUsers_requiresAdmin(Role role)`**: GET /users solo ADMIN puede listar todos los usuarios.
- **`updateUser_selfOrAdmin(Role role)`**: PATCH /users/{id} permite al propio usuario o ADMIN.
- **`updatePassword_selfOrAdmin(Role role)`**: PATCH /users/{id}/password permite self o ADMIN.
- **`getUser_selfOrAdmin(Role role)`**: GET /users/{id} permite self o ADMIN.
- **`admin_canToggle_nonAdmin()`**: ADMIN puede desactivar OPERATOR/MANAGER/VIEWER.
- **`admin_cannotToggle_otherAdmin()`**: ADMIN no puede desactivar a otro ADMIN (retorna 403).
- **`nonAdmin_cannotToggle_any(Role role)`**: Roles no-ADMIN no pueden desactivar a nadie.

---

### UserPaginationIntegrationTest
**Propósito**: Tests de paginación, ordenamiento y filtrado en listado de usuarios.

#### Funciones de Test:
- **`findAll_defaultPagination_returnsFirstPage()`**: Sin parámetros, retorna página 0 con tamaño por defecto.
- **`findAll_customPageSize_returnsCorrectNumberOfElements()`**: Parámetro size controla cantidad de elementos.
- **`findAll_sortByNameDesc_returnsSortedResults()`**: Ordenamiento descendente por nombre funciona.
- **`findAll_outOfRangePage_returnsEmptyContent()`**: Página fuera de rango retorna content vacío pero con metadata correcta.
- **`toggleActive_success_togglesActiveStatus()`**: Cambiar estado activo persiste correctamente.

---

### UserValidationIntegrationTest
**Propósito**: Tests de validación a nivel de integración (Bean Validation + lógica de negocio).

#### Funciones de Test:
- **`createUser_invalidPayload_returns400()`**: JSON mal formado o campos inválidos retornan 400.
- **`createUser_weakPassword_returns400()`**: Password débil (menos de 8 caracteres) retorna 400.
- **`createUser_emptyRoles_returns400()`**: Lista de roles vacía retorna 400.
- **`createUser_invalidEmail_returns400()`**: Email con formato inválido retorna 400.
- **`updateUser_invalidPayload_returns400()`**: Datos de actualización inválidos retornan 400.
- **`updatePassword_weakPassword_returns400()`**: Nueva contraseña débil retorna 400.

---

## Product Phases Tests

### ProductPhaseServiceImplTest
**Propósito**: Pruebas unitarias del servicio de fases de producto, validando lógica de negocio para gestión de fases de producción.

#### Funciones de Test:
- **`testUpdateProductPhase_Success()`**: Actualizar fase existente (duración, materiales).
- **`testUpdateProductPhase_NotFound()`**: Actualizar fase inexistente lanza ResourceNotFoundException.
- **`testFindAll_Success()`**: Listar todas las fases con paginación.
- **`testGetProductPhase_Success()`**: Obtener fase por ID.
- **`testGetProductPhase_NotFound()`**: Fase inexistente lanza excepción.
- **`testGetByProduct_Success()`**: Obtener fases de un producto específico ordenadas por phaseOrder.
- **`testGetByProduct_EmptyAndProductNotExists()`**: Producto sin fases retorna lista vacía.
- **`testMarkAsReady_Success()`**: Marcar fase como lista validando que materiales y duración están completos.
- **`testMarkAsReady_NotFound()`**: Marcar como lista fase inexistente lanza excepción.
- **`testToggleReady_IncompletePhase()`**: Fase incompleta (sin duración) no puede marcarse como lista.
- **`testMarkAsReady_MissingMaterials()`**: Fase sin materiales asignados no puede marcarse como lista.

---

### ProductPhaseMapperTest
**Propósito**: Tests del mapper ProductPhase entity ↔ DTOs, incluyendo callbacks de lifecycle (@PrePersist, @PreUpdate).

#### Funciones de Test:
- **`testToResponseDto()`**: Mapeo de entidad a DTO de respuesta con todos los campos.
- **`testPartialUpdate()`**: Actualización parcial ignora nulls, actualiza solo campos provistos.
- **`testSetPhaseOrder_prePersist()`**: Callback @PrePersist establece phaseOrder desde phase.getOrder() automáticamente.
- **`testSetPhaseOrder_withDifferentPhases()`**: Diferentes fases (MOLIENDA, FERMENTACION) obtienen orders correctos (1, 4).
- **`testSetPhaseOrder_whenPhaseIsNull()`**: Si phase es null, phaseOrder permanece 0 (manejo seguro de null).
- **`testSetPhaseOrder_preUpdate()`**: Callback @PreUpdate actualiza phaseOrder si cambia la fase.

---

### ProductPhaseControllerTest
**Propósito**: Tests del controlador REST de fases de producto.

#### Funciones de Test:
- **`testUpdateProductPhase()`**: PATCH /product-phases/{id} actualiza fase correctamente.
- **`testGetProductPhases()`**: GET /product-phases retorna listado paginado.
- **`testGetProductPhase()`**: GET /product-phases/{id} retorna fase específica.
- **`testGetProductPhasesByProduct()`**: GET /product-phases/product/{productId} retorna fases de un producto.

---

### ProductPhaseControllerSecurityTest
**Propósito**: Tests de seguridad para endpoints de fases de producto.

#### Funciones de Test:
- **`getProductPhases_unauthenticated_returns401()`**: GET sin auth retorna 401.
- **`getProductPhases_authenticated_returns200()`**: GET con auth retorna 200.

---

### ProductPhaseIntegrationTest
**Propósito**: Tests de integración end-to-end para fases de producto.

#### Funciones de Test:
- **`productPhasesHappyPath()`**: Flujo completo: crear producto, obtener fases, actualizar fase, marcar como lista.

---

### PhaseTest
**Propósito**: Tests del enum Phase, validando propiedades order, isTimeActive, y métodos comesBefore(), next().

#### Funciones de Test:
- **`allPhases_haveUniqueOrders()`**: Todos los valores del enum Phase (MOLIENDA=1, MACERACION=2, ..., ENVASADO=9) tienen orders únicos.
- **`activePhases_areCorrectlyIdentified()`**: 7 fases activas (isTimeActive=true): MOLIENDA, MACERACION, COCCION, ENFRIADO, DESALCOHOL, ENVASADO, ETIQUETADO.
- **`passivePhases_areCorrectlyIdentified()`**: 2 fases pasivas (isTimeActive=false): FERMENTACION, MADURACION.
- **`phaseOrder_isCorrect()`**: Orden secuencial: MOLIENDA(1) → MACERACION(2) → COCCION(3) → FERMENTACION(4) → MADURACION(5) → ENFRIADO(6) → DESALCOHOL(7) → ENVASADO(8) → ETIQUETADO(9).
- **`comesBefore_returnsTrue_whenPhaseIsEarlier()`**: MOLIENDA.comesBefore(FERMENTACION) = true.
- **`comesBefore_returnsFalse_whenPhaseIsLater()`**: ENVASADO.comesBefore(MOLIENDA) = false.
- **`comesBefore_returnsFalse_whenPhasesAreSame()`**: COCCION.comesBefore(COCCION) = false.
- **`next_returnsNextPhase_whenExists()`**: MOLIENDA.next() = Optional[MACERACION].
- **`next_returnsEmpty_forLastPhase()`**: ETIQUETADO.next() = Optional.empty().
- **`next_chainingMultipleTimes()`**: MOLIENDA.next().next().next() = FERMENTACION (chaining funciona correctamente).
- **`phaseSequence_isLogical()`**: Secuencia completa refleja flujo de producción real de cerveza.
- **`activeVsPassivePhases_distribution()`**: Verificar que hay balance entre fases activas (7) y pasivas (2).
- **`getOrder_isImmutable()`**: El valor de order no cambia (propiedad inmutable del enum).
- **`getIsTimeActive_isImmutable()`**: El valor de isTimeActive es inmutable.

---

## Products Tests

### ProductServiceTest
**Propósito**: Pruebas unitarias del servicio de productos, validando lógica de creación de fases automáticas según tipo de producto (alcohólico/no alcohólico).

#### Funciones de Test:
- **`createProduct_isAlcoholic_createsPhases_andPersists()`**: Crear producto alcohólico genera automáticamente 9 fases (MOLIENDA → ETIQUETADO).
- **`createProduct_notAlcoholic_addsDesalcoholPhase()`**: Producto no alcohólico incluye fase DESALCOHOL.
- **`createProduct_createsWithoutPackaging()`**: Producto sin packaging se crea correctamente (packaging opcional).
- **`markAsReady_whenPhasesNotReady_throwsBadRequest()`**: Producto no puede marcarse como listo si alguna fase no está lista.
- **`markAsReady_whenAllPhasesReady_setsFlagAndSaves()`**: Todas las fases listas permite marcar producto como listo (isReady=true).
- **`updateProduct_changesNameAndAlcoholicType()`**: Actualizar nombre y tipo de producto funciona correctamente.
- **`toggleActive_invertsFlag()`**: Activar/desactivar producto invierte flag active.
- **`getProduct_returnsMappedDto()`**: Obtener producto retorna DTO completo mapeado.
- **`findAll_mapsPage()`**: Listar productos con paginación retorna Page<ProductResponseDTO>.

---

### ProductMapperTest
**Propósito**: Tests del mapper Product entity ↔ DTOs.

#### Funciones de Test:
- **`toResponseDto_maps_all_expected_fields()`**: Mapeo completo de entidad a DTO (id, name, isAlcoholic, isReady, active, packaging, phases).
- **`partialUpdate_updates_nonNull_fields()`**: Actualización parcial con UpdateDTO solo actualiza campos no-null.

---

### ProductControllerTest
**Propósito**: Tests del controlador REST de productos.

#### Funciones de Test:
- **`testCreateProduct()`**: POST /products crea producto retornando 201.
- **`testCreateProduct_BadRequest_ShouldReturn400()`**: Datos inválidos retornan 400.
- **`testCreateProduct_MissingName_ShouldReturn400()`**: Name obligatorio.
- **`testCreateProduct_MissingIsAlcoholic_ShouldReturn400()`**: isAlcoholic obligatorio.
- **`testMarkAsReady()`**: POST /products/{id}/mark-ready marca producto como listo.
- **`testMarkAsReady_NotFound_ShouldReturn404()`**: Marcar como listo producto inexistente retorna 404.
- **`testToggleActive()`**: PATCH /products/{id}/toggle-active cambia estado.
- **`testUpdateProduct()`**: PATCH /products/{id} actualiza producto.
- **`testGetProducts_PaginationMetadata_ShouldReturnFields()`**: Metadata de paginación (totalElements, totalPages, size, number) presente en respuesta.
- **`testUpdateProduct_NotFound_ShouldReturn404()`**: Actualizar producto inexistente retorna 404.

---

### ProductControllerSecurityTest
**Propósito**: Tests de seguridad para endpoints de productos.

#### Funciones de Test:
- **`getProducts_unauthenticated_returns401()`**: GET /products sin auth retorna 401.
- **`getProducts_authenticated_returns200()`**: GET /products con auth retorna 200.

---

### ProductIntegrationTest
**Propósito**: Tests de integración end-to-end para productos.

#### Funciones de Test:
- **`productCrudHappyPath()`**: Flujo completo CRUD: crear producto alcohólico, obtenerlo, actualizar, marcar fases como listas, marcar producto como listo, desactivar.

---

## Common/Utils Tests

### DateUtilTest
**Propósito**: Pruebas exhaustivas del utilitario DateUtil para estimación de fechas de fin de producción, considerando días laborables (Lun-Vie 8:00-17:00), fines de semana, fases activas vs pasivas.

#### Funciones de Test:
- **`estimateEndDate_singleActivePhase_withinSameDay()`**: Fase activa de 4h iniciando a las 10:00 termina el mismo día a las 14:00.
- **`estimateEndDate_singleActivePhase_spansMultipleDays()`**: Fase activa de 20h que cruza múltiples días (excluye noches y fines de semana).
- **`estimateEndDate_singlePassivePhase_ignoresWorkingHours()`**: Fase pasiva de 48h funciona 24/7 (fermenta durante noche y fin de semana).
- **`estimateEndDate_multiplePhasesActiveAndPassive()`**: Combina fases activas y pasivas correctamente.
- **`estimateEndDate_startBeforeWorkingHours_alignsToOpeningTime()`**: Si inicio es 06:00, se alinea a 08:00 (apertura).
- **`estimateEndDate_startAfterWorkingHours_movesToNextDay()`**: Si inicio es 19:00 (después de cierre), mueve al siguiente día 08:00.
- **`estimateEndDate_startOnWeekend_movesToMonday()`**: Si inicio es sábado/domingo, mueve al lunes 08:00.
- **`estimateEndDate_phaseSpansWeekend_skipsWeekend()`**: Fase activa de 15h que cae en fin de semana salta al lunes.
- **`estimateEndDate_phaseWithDecimalHours()`**: Fases de 2.5h calculan minutos correctamente (2h 30min).
- **`estimateEndDate_phaseEndsExactlyAtClosingTime()`**: Fase que termina justo a las 17:00 no se extiende al día siguiente.
- **`estimateEndDate_realWorldScenario_beerProduction()`**: Escenario completo de producción de cerveza con 7 fases (molienda, maceración, cocción, fermentación 336h, maduración 336h, enfriado, envasado) calculando ~21 días.
- **`estimateEndDate_emptyPhaseList_returnsStartDate()`**: Sin fases, retorna la misma fecha de inicio.
- **`estimateEndDate_phaseSpansMultipleWeeks()`**: Fase larga de 168h (7 días) abarca múltiples semanas laborables.
- **`estimateEndDate_startAtMiddayWithPartialHours()`**: Inicio a las 12:00 con fase de 3.5h termina a las 15:30.

---

## 🔄 Movements Tests

### MovementServiceImplTest

Tests unitarios para el servicio de movimientos de materiales.

- **testCreateMovement_Ingreso_Success**: Testea la creación exitosa de un movimiento de ingreso de material
- **testCreateMovement_Egreso_Success**: Testea la creación exitosa de un movimiento de egreso de material
- **testCreateMovement_Egreso_StockInsuficiente**: Testea que falla un egreso cuando el stock es insuficiente
- **testCreateMovement_MaterialNoEncontrado**: Testea que falla cuando el material no existe
- **testGetMovement_Success**: Testea la obtención exitosa de un movimiento por ID
- **testGetMovement_NotFound**: Testea que falla cuando el movimiento no existe
- **testFindAll_ReturnsPage**: Testea la paginación y búsqueda de movimientos
- **testCreateReserveOrReturn_Reserva_Success**: Testea la reserva exitosa de material
- **testCreateReserveOrReturn_Reserva_StockInsuficiente**: Testea que falla la reserva cuando el stock es insuficiente
- **testCreateReserveOrReturn_Devuelto_Success**: Testea la devolución exitosa de material reservado
- **testCreateReserveOrReturn_Devuelto_ReservedStockInsuficiente**: Testea que falla la devolución cuando el stock reservado es insuficiente
- **testCreateReserveOrReturn_TipoInvalido**: Testea que falla cuando el tipo de movimiento es inválido
- **testConfirmReservation_Success**: Testea la confirmación exitosa de una reserva
- **testConfirmReservation_ReservedStockInsuficiente**: Testea que falla la confirmación cuando el stock reservado es insuficiente

### MovementMapperTest

Tests del mapper para transformaciones de movimientos.

- **testToResponseDto**: Testea el mapeo de entidad a DTO de respuesta
- **testToDetailDTO**: Testea el mapeo a DTO con detalles completos del movimiento

### MovementControllerTest

Tests unitarios del controlador de movimientos con MockMvc.

- **testCreateMovement**: Testea la creación de movimiento vía API retorna 201
- **testCreateMovement_BadRequest_ShouldReturn400**: Testea que retorna 400 con datos inválidos
- **testGetMovement**: Testea obtener un movimiento por ID retorna 200
- **testGetMovement_NotFound_ShouldReturn404**: Testea que retorna 404 cuando el movimiento no existe
- **testCreateMovement_InvalidType_ShouldReturn400**: Testea que retorna 400 con tipo de movimiento inválido

### MovementControllerSecurityTest

Tests de seguridad del controlador de movimientos.

- **getMovements_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401
- **getMovements_authenticated_returns200**: Testea que usuarios autenticados pueden acceder

### MovementIntegrationTest

Tests de integración end-to-end de movimientos.

- **createMovement_andGetById_happyPath**: Testea el flujo completo de crear y obtener un movimiento
- **createMovement_updatesStockCorrectly**: Testea que crear movimientos actualiza correctamente el stock
- **listMovements_happyPath**: Testea listar movimientos con paginación

---

## 📦 Packagings Tests

### PackagingServiceImplTest

Tests unitarios para el servicio de envases/packaging.

- **testCreatePackaging_Success**: Testea la creación exitosa de un envase
- **testToggleActive_Success**: Testea activar/desactivar un envase
- **testToggleActive_NotFound**: Testea que falla al activar/desactivar envase inexistente
- **testFindAll_ReturnsPage**: Testea la paginación de envases
- **testGetPackaging_Success**: Testea obtener un envase por ID
- **testGetPackaging_NotFound**: Testea que falla cuando el envase no existe
- **testGetPackagingList_FiltersActivePackagings**: Testea el filtrado de envases activos
- **testUpdatePackaging_Success**: Testea la actualización exitosa de un envase
- **testUpdatePackaging_NotFound**: Testea que falla al actualizar envase inexistente

### PackagingMapperTest

Tests del mapper para transformaciones de packagings.

- **testToEntity**: Testea el mapeo de DTO a entidad
- **testToResponseDto**: Testea el mapeo de entidad a DTO de respuesta con todos los campos
- **testToSimpleResponseDTO**: Testea el mapeo a DTO simplificado
- **testPartialUpdate**: Testea la actualización parcial de un envase existente

### PackagingControllerTest

Tests unitarios del controlador de packagings con MockMvc.

- **testCreatePackaging**: Testea la creación de envase vía API retorna 201
- **testCreatePackaging_BadRequest_ShouldReturn400**: Testea que retorna 400 con datos inválidos
- **testUpdatePackaging**: Testea la actualización de envase retorna 200
- **testToggleActive**: Testea activar/desactivar envase retorna 200
- **testGetPackaging**: Testea obtener envase por ID retorna 200
- **testGetPackaging_NotFound_ShouldReturn404**: Testea que retorna 404 cuando no existe
- **testGetPackagings**: Testea listar envases retorna 200
- **testGetPackagings_PaginationMetadata_ShouldReturnFields**: Testea que la paginación retorna metadatos correctos

### PackagingControllerSecurityTest

Tests de seguridad del controlador de packagings.

- **getPackagings_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401
- **getPackagings_authenticated_returns200**: Testea que usuarios autenticados pueden acceder

### PackagingIntegrationTest

Tests de integración end-to-end de packagings.

- **packagingCrudHappyPath**: Testea el flujo completo CRUD de envases

---

## 📋 Production Orders Tests

### ProductionOrderServiceImplTest

Tests unitarios para el servicio de órdenes de producción.

- **createProductionOrder_success**: Testea la creación exitosa de una orden de producción
- **createProductionOrder_productNotFound_throws**: Testea que falla cuando el producto no existe
- **createProductionOrder_productNotReady_throws**: Testea que falla cuando el producto no está listo
- **createProductionOrder_unitMeasurementMismatch_throws**: Testea que falla cuando las unidades no coinciden
- **createProductionOrder_verifiesCorrectMaterialReservation**: Testea que reserva correctamente los materiales
- **approveOrder_success**: Testea la aprobación exitosa de una orden
- **approveOrder_orderNotFound_throws**: Testea que falla al aprobar orden inexistente
- **approveOrder_orderNotPending_throws**: Testea que falla al aprobar orden que no está pendiente
- **approveOrder_verifiesCorrectMaterialConfirmation**: Testea que confirma correctamente los materiales reservados
- **returnOrder_toCancelled_success**: Testea la cancelación exitosa de una orden
- **returnOrder_toRejected_success**: Testea el rechazo exitoso de una orden
- **returnOrder_orderNotFound_throws**: Testea que falla al devolver orden inexistente
- **returnOrder_orderNotPending_throws**: Testea que falla al devolver orden que no está pendiente
- **returnOrder_toPendingStatus_throws**: Testea que falla al intentar devolver a estado pendiente
- **returnOrder_verifiesCorrectMaterialReturn**: Testea que devuelve correctamente los materiales reservados
- **getProductionOrder_success**: Testea obtener una orden por ID
- **getProductionOrder_notFound_throws**: Testea que falla cuando la orden no existe
- **findAll_success**: Testea la búsqueda paginada de órdenes
- **findAll_emptyResults**: Testea que retorna lista vacía cuando no hay órdenes

### ProductionOrderMapperTest

Tests del mapper para transformaciones de órdenes de producción.

- **testToEntity_fromCreateDTO**: Testea el mapeo de DTO de creación a entidad
- **testToResponseDTO_allFieldsMapped**: Testea que mapea todos los campos a DTO de respuesta
- **testToResponseDTO_withNullDates**: Testea el mapeo cuando las fechas son nulas
- **testToResponseDTO_pendingOrder**: Testea el mapeo de una orden pendiente
- **testToResponseDTO_cancelledOrder**: Testea el mapeo de una orden cancelada

### ProductionOrderControllerTest

Tests unitarios del controlador de órdenes de producción con MockMvc.

- **testCreateProductionOrder_Success**: Testea la creación de orden vía API retorna 201
- **testCreateProductionOrder_MissingProductId_ShouldReturnBadRequest**: Testea validación de productId requerido
- **testCreateProductionOrder_MissingQuantity_ShouldReturnBadRequest**: Testea validación de cantidad requerida
- **testCreateProductionOrder_NegativeQuantity_ShouldReturnBadRequest**: Testea que rechaza cantidades negativas
- **testCreateProductionOrder_MissingPackagingId_ShouldReturnBadRequest**: Testea validación de packagingId requerido
- **testCreateProductionOrder_MissingPlannedDate_ShouldReturnBadRequest**: Testea validación de fecha planificada requerida
- **testCreateProductionOrder_ProductNotReady_ShouldReturn400**: Testea que falla con producto no listo
- **testCreateProductionOrder_ProductNotFound_ShouldReturn404**: Testea que falla con producto inexistente
- **testApproveOrder_Success**: Testea la aprobación de orden retorna 200
- **testApproveOrder_NotFound_ShouldReturn400**: Testea que falla al aprobar orden inexistente
- **testApproveOrder_NotPending_ShouldReturn400**: Testea que falla al aprobar orden no pendiente
- **testCancelOrder_Success**: Testea la cancelación de orden retorna 200
- **testCancelOrder_NotFound_ShouldReturn400**: Testea que falla al cancelar orden inexistente
- **testCancelOrder_NotPending_ShouldReturn400**: Testea que falla al cancelar orden no pendiente
- **testRejectOrder_Success**: Testea el rechazo de orden retorna 200
- **testRejectOrder_NotFound_ShouldReturn400**: Testea que falla al rechazar orden inexistente
- **testRejectOrder_NotPending_ShouldReturn400**: Testea que falla al rechazar orden no pendiente
- **testGetProductionOrders_Success**: Testea listar órdenes retorna 200
- **testGetProductionOrders_WithPagination**: Testea la paginación de órdenes
- **testGetProductionOrder_Success**: Testea obtener orden por ID retorna 200
- **testGetProductionOrder_NotFound_ShouldReturn404**: Testea que retorna 404 cuando no existe

### ProductionOrderControllerSecurityTest

Tests de seguridad del controlador de órdenes de producción.

- **getProductionOrders_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401
- **getProductionOrders_authenticated_returns200**: Testea que usuarios autenticados pueden listar órdenes
- **getProductionOrder_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401 al obtener orden
- **getProductionOrder_authenticated_returns200**: Testea que usuarios autenticados pueden obtener orden
- **approveOrder_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401 al aprobar
- **approveOrder_authenticated_returns200**: Testea que usuarios autenticados pueden aprobar órdenes
- **cancelOrder_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401 al cancelar
- **cancelOrder_authenticated_returns200**: Testea que usuarios autenticados pueden cancelar órdenes
- **rejectOrder_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401 al rechazar
- **rejectOrder_authenticated_returns200**: Testea que usuarios autenticados pueden rechazar órdenes

---

## 🍺 Recipes Tests

### RecipeServiceImplTest

Tests unitarios para el servicio de recetas.

- **createRecipe_success**: Testea la creación exitosa de una receta
- **createRecipe_materialNotAllowed_throwsBadRequest**: Testea que falla cuando el material no está permitido en esa fase
- **createRecipe_productPhaseNotFound_throwsNotFound**: Testea que falla cuando la fase del producto no existe
- **createRecipe_materialNotFound_throwsNotFound**: Testea que falla cuando el material no existe
- **updateRecipe_success**: Testea la actualización exitosa de una receta
- **updateRecipe_notFound_throws**: Testea que falla al actualizar receta inexistente
- **deleteRecipe_success**: Testea la eliminación exitosa de una receta
- **deleteRecipe_notFound_throws**: Testea que falla al eliminar receta inexistente
- **getRecipe_success**: Testea obtener una receta por ID
- **getRecipe_notFound_throws**: Testea que falla cuando la receta no existe
- **getRecipeList_success**: Testea listar todas las recetas
- **getMaterialByPhase_success**: Testea obtener materiales por fase de producto
- **getMaterialByPhase_productPhaseNotFound_throws**: Testea que falla cuando la fase no existe
- **getMaterialByProduct_success**: Testea obtener materiales por producto
- **getMaterialByProduct_emptyList**: Testea que retorna lista vacía cuando no hay materiales
- **getRecipeByProduct_success**: Testea obtener recetas por producto
- **getRecipeByProduct_emptyList**: Testea que retorna lista vacía cuando no hay recetas
- **createRecipe_withOtrosType_shouldAllowAnyPhase**: Testea que materiales tipo "Otros" se permiten en cualquier fase

### RecipeMapperTest

Tests del mapper para transformaciones de recetas.

- **testToEntity**: Testea el mapeo de DTO a entidad
- **testToResponseDTO_mapsMaterialFields**: Testea que mapea correctamente los campos del material

### RecipeControllerTest

Tests unitarios del controlador de recetas con MockMvc.

- **createRecipe_success**: Testea la creación de receta vía API retorna 201
- **updateRecipe_success**: Testea la actualización de receta retorna 200
- **deleteRecipe_success**: Testea la eliminación de receta retorna 204
- **getRecipe_success**: Testea obtener receta por ID retorna 200
- **getRecipeList_success**: Testea listar recetas retorna 200

### RecipeControllerSecurityTest

Tests de seguridad del controlador de recetas.

- **getRecipeList_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401
- **getRecipeList_authenticated_returns200**: Testea que usuarios autenticados pueden acceder

### RecipeIntegrationTest

Tests de integración end-to-end de recetas.

- **createRecipe_andGetById_happyPath**: Testea el flujo completo de crear y obtener una receta
- **updateRecipe_happyPath**: Testea el flujo completo de actualización de receta
- **getRecipesByProductPhase_happyPath**: Testea obtener recetas filtradas por fase de producto

---

## 🏭 Sectors Tests

### SectorServiceImplTest

Tests unitarios para el servicio de sectores.

- **createSector_withValidProduccionData_success**: Testea la creación exitosa de un sector de producción
- **createSector_withValidAlmacenData_success**: Testea la creación exitosa de un sector de almacén
- **createSector_withNonExistentSupervisor_throwsResourceNotFoundException**: Testea que falla cuando el supervisor no existe
- **createSector_withWrongSupervisorRole_throwsBadRequestException**: Testea que falla cuando el supervisor no tiene el rol correcto
- **createSector_produccionWithoutRequiredFields_throwsBadRequestException**: Testea que falla cuando faltan campos requeridos en sector de producción
- **getSector_withValidId_success**: Testea obtener un sector por ID
- **getSector_withInvalidId_throwsResourceNotFoundException**: Testea que falla cuando el sector no existe
- **updateSector_withValidData_success**: Testea la actualización exitosa de un sector
- **updateSector_withInvalidId_throwsResourceNotFoundException**: Testea que falla al actualizar sector inexistente
- **updateSector_changingSupervisorWithWrongRole_throwsBadRequestException**: Testea que falla al cambiar supervisor con rol incorrecto

### SectorMapperTest

Tests del mapper para transformaciones de sectores.

- **testToEntity_fromCreateDTO**: Testea el mapeo de DTO de creación a entidad
- **testToResponseDTO_mapsSupervisorIdCorrectly**: Testea que mapea correctamente el ID del supervisor
- **testToResponseDTO_withProduccionFields**: Testea que mapea correctamente los campos específicos de producción

### SectorControllerSecurityTest

Tests de seguridad del controlador de sectores.

- **getSector_unauthenticated_returns401**: Testea que usuarios no autenticados reciben 401
- **getSector_authenticated_returns200**: Testea que usuarios autenticados pueden acceder

### SectorIntegrationTest

Tests de integración end-to-end de sectores.

- **getSector_asAuthenticatedUser_returns404NotFound**: Testea que retorna 404 cuando el sector no existe
- **createSector_withNonExistentSupervisor_returns404**: Testea que retorna 404 cuando el supervisor no existe
- **createSector_withUnauthorizedRole_returns403**: Testea que usuarios sin autorización reciben 403
- **updateSector_withUnauthorizedRole_returns403**: Testea que usuarios sin autorización reciben 403 al actualizar
