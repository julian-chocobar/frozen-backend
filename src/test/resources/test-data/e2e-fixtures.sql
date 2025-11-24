-- ========================================
-- E2E Test Fixtures - Frozen Backend (H2 Compatible)
-- ========================================
-- Este script crea datos de prueba completos para tests E2E
-- Incluye: Users, Sectors, Materials, Products con Phases READY, Recipes, Packagings
-- IDs predecibles: 9xx para infraestructura, 1xxx materiales, 2xxx productos, 3xxx phases, 4xxx recipes, 5xxx packagings
-- Compatible con H2 Database (case-sensitive, tablas en minúsculas)

-- ========================================
-- 1. USERS & ROLES
-- ========================================

INSERT INTO "users" ("id", "username", "name", "email", "password", "is_active", "enabled", "account_non_expired", "account_non_locked", "credentials_non_expired", "creation_date") 
VALUES (999, 'supervisor-e2e', 'Supervisor E2E', 'supervisor-e2e@test.com', '$2a$10$encoded.password.hash', true, true, true, true, true, NOW());

INSERT INTO "user_roles" ("user_id", "role") 
VALUES (999, 'SUPERVISOR_DE_PRODUCCION');

-- ========================================
-- 2. SECTORS
-- ========================================

INSERT INTO "sectors" ("id", "name", "id_user", "type", "phase", "production_capacity", "actual_production", "is_active", "is_time_active", "creation_date")
VALUES 
  (900, 'Sector Molienda E2E', 999, 'PRODUCCION', 'MOLIENDA', 10000.0, 0.0, true, true, NOW()),
  (901, 'Sector Maceración E2E', 999, 'PRODUCCION', 'MACERACION', 10000.0, 0.0, true, true, NOW()),
  (902, 'Sector Cocción E2E', 999, 'PRODUCCION', 'COCCION', 10000.0, 0.0, true, true, NOW());

-- ========================================
-- 3. MATERIALS (Stock suficiente para tests)
-- ========================================
-- Note: type field uses ORDINAL mapping (MALTA=0, LUPULO=1, AGUA=2, LEVADURA=3, ENVASE=4, ETIQUETADO=5, OTROS=6)

INSERT INTO "materials" ("id", "code", "name", "type", "supplier", "value", "stock", "reserved_stock", "unit_measurement", "threshold", "is_active", "creation_date")
VALUES 
  -- Materiales con STOCK ALTO (para test exitoso)
  (1000, 'MALTA-E2E-HIGH', 'Malta Pilsen E2E', 0, 'Proveedor E2E', 10.0, 1000.0, 0.0, 'KG', 50.0, true, NOW()),
  (1001, 'LUPULO-E2E-HIGH', 'Lúpulo Cascade E2E', 1, 'Proveedor E2E', 15.0, 100.0, 0.0, 'KG', 10.0, true, NOW()),
  (1002, 'LEVADURA-E2E-HIGH', 'Levadura Ale E2E', 3, 'Proveedor E2E', 20.0, 50.0, 0.0, 'KG', 5.0, true, NOW()),
  (1003, 'AGUA-E2E-HIGH', 'Agua E2E', 2, 'Proveedor E2E', 1.0, 10000.0, 0.0, 'LT', 1000.0, true, NOW()),
  (1004, 'BOTELLA-E2E-HIGH', 'Botella 500ml E2E', 4, 'Proveedor E2E', 0.5, 5000.0, 0.0, 'UNIDAD', 500.0, true, NOW()),
  (1005, 'ETIQUETA-E2E-HIGH', 'Etiqueta IPA E2E', 5, 'Proveedor E2E', 0.1, 5000.0, 0.0, 'UNIDAD', 500.0, true, NOW()),
  
  -- Material con STOCK BAJO (para test de stock insuficiente)
  (1100, 'MALTA-E2E-LOW', 'Malta Baja Stock E2E', 0, 'Proveedor E2E', 10.0, 10.0, 0.0, 'KG', 50.0, true, NOW()),
  
  -- Materiales para cancelación
  (1200, 'MALTA-E2E-CANCEL', 'Malta Cancelación E2E', 0, 'Proveedor E2E', 10.0, 500.0, 0.0, 'KG', 50.0, true, NOW()),
  (1201, 'ENVASE-E2E-CANCEL', 'Envase Cancelación E2E', 4, 'Proveedor E2E', 0.5, 1000.0, 0.0, 'UNIDAD', 100.0, true, NOW()),
  (1202, 'ETIQUETA-E2E-CANCEL', 'Etiqueta Cancelación E2E', 5, 'Proveedor E2E', 0.1, 1000.0, 0.0, 'UNIDAD', 100.0, true, NOW());

-- ========================================
-- 4. PRODUCTS con todas las PHASES READY
-- ========================================

-- Producto 1: IPA Completa (para test exitoso)
INSERT INTO "products" ("id", "name", "is_alcoholic", "standard_quantity", "unit_measurement", "is_ready", "is_active", "creation_date")
VALUES (2000, 'Cerveza IPA E2E Test', true, 500.0, 'LT', true, true, NOW());

-- Producto 2: Producto con stock bajo (para test de fallo)
INSERT INTO "products" ("id", "name", "is_alcoholic", "standard_quantity", "unit_measurement", "is_ready", "is_active", "creation_date")
VALUES (2100, 'Cerveza Stock Bajo E2E', true, 100.0, 'LT', true, true, NOW());

-- Producto 3: Para test de cancelación
INSERT INTO "products" ("id", "name", "is_alcoholic", "standard_quantity", "unit_measurement", "is_ready", "is_active", "creation_date")
VALUES (2200, 'Cerveza Cancelación E2E', true, 100.0, 'LT', true, true, NOW());

-- ========================================
-- 5. PRODUCT PHASES (fases válidas para cada producto alcohólico)
-- ========================================
-- Note: Valid phases are: MOLIENDA, MACERACION, FILTRACION, COCCION, FERMENTACION, MADURACION, DESALCOHOLIZACION, GASIFICACION, ENVASADO

-- Phases para Producto 2000 (IPA Completa) - 8 fases
INSERT INTO "product_phases" ("id", "id_product", "phase", "phase_order", "input", "output", "output_unit", "estimated_hours", "is_ready")
VALUES 
  (3000, 2000, 'MOLIENDA', 1, 100.0, 95.0, 'LT', 2.0, true),
  (3001, 2000, 'MACERACION', 2, 95.0, 90.0, 'LT', 4.0, true),
  (3002, 2000, 'FILTRACION', 3, 90.0, 85.0, 'LT', 3.0, true),
  (3003, 2000, 'COCCION', 4, 85.0, 80.0, 'LT', 2.0, true),
  (3004, 2000, 'FERMENTACION', 5, 80.0, 75.0, 'LT', 336.0, true),
  (3005, 2000, 'MADURACION', 6, 75.0, 70.0, 'LT', 336.0, true),
  (3006, 2000, 'GASIFICACION', 7, 70.0, 65.0, 'LT', 2.0, true),
  (3007, 2000, 'ENVASADO', 8, 65.0, 60.0, 'LT', 3.0, true);

-- Phases para Producto 2100 (Stock Bajo) - solo MOLIENDA para simplificar
INSERT INTO "product_phases" ("id", "id_product", "phase", "phase_order", "input", "output", "output_unit", "estimated_hours", "is_ready")
VALUES (3100, 2100, 'MOLIENDA', 1, 100.0, 95.0, 'LT', 10.0, true);

-- Phases para Producto 2200 (Cancelación) - solo MOLIENDA para simplificar
INSERT INTO "product_phases" ("id", "id_product", "phase", "phase_order", "input", "output", "output_unit", "estimated_hours", "is_ready")
VALUES (3200, 2200, 'MOLIENDA', 1, 100.0, 95.0, 'LT', 10.0, true);

-- ========================================
-- 6. RECIPES (relacionan phases con materials)
-- ========================================

-- Recipes para Producto 2000 (IPA Completa)
INSERT INTO "recipes" ("id", "id_product_phase", "id_material", "quantity")
VALUES 
  (4000, 3000, 1000, 100.0),  -- MOLIENDA → Malta (100kg por 500L)
  (4001, 3001, 1003, 400.0),  -- MACERACION → Agua (400L por 500L)
  (4002, 3003, 1001, 3.0),    -- COCCION → Lúpulo (3kg por 500L)
  (4003, 3004, 1002, 2.0);    -- FERMENTACION → Levadura (2kg por 500L)

-- Recipe para Producto 2100 (Stock Bajo) - necesita 100kg malta por cada 100L
INSERT INTO "recipes" ("id", "id_product_phase", "id_material", "quantity")
VALUES (4100, 3100, 1100, 100.0);  -- MOLIENDA → Malta Baja Stock

-- Recipe para Producto 2200 (Cancelación)
INSERT INTO "recipes" ("id", "id_product_phase", "id_material", "quantity")
VALUES (4200, 3200, 1200, 50.0);  -- MOLIENDA → Malta Cancelación (50kg por 100L)

-- ========================================
-- 7. PACKAGINGS
-- ========================================

-- Packaging para producto exitoso
INSERT INTO "packagings" ("id", "name", "id_packaging_material", "id_labeling_material", "quantity", "unit_measurement", "is_active", "creation_date")
VALUES (5000, 'Pack 1L E2E', 1004, 1005, 1.0, 'LT', true, NOW());

-- Packaging para stock bajo
INSERT INTO "packagings" ("id", "name", "id_packaging_material", "id_labeling_material", "quantity", "unit_measurement", "is_active", "creation_date")
VALUES (5100, 'Pack 1L E2E Low', 1004, 1005, 1.0, 'LT', true, NOW());

-- Packaging para cancelación
INSERT INTO "packagings" ("id", "name", "id_packaging_material", "id_labeling_material", "quantity", "unit_measurement", "is_active", "creation_date")
VALUES (5200, 'Pack 1L E2E Cancel', 1201, 1202, 1.0, 'LT', true, NOW());

-- ========================================
-- NOTAS:
-- ========================================
-- Este script crea un entorno completo para tests E2E:
-- 
-- TEST 1 (Happy Path): Usar Product 2000 + Materials 1000-1005 + Packaging 5000
-- - Stock suficiente para producir 500L de IPA
-- - Todas las phases configuradas y ready
-- - Recipes completas para 4 fases principales
--
-- TEST 2 (Stock Insuficiente): Usar Product 2100 + Material 1100 + Packaging 5100
-- - Material 1100 solo tiene 10kg disponibles
-- - Receta necesita 100kg por cada 100L
-- - Debe fallar al crear orden que necesita 1000kg (10x multiplicador)
--
-- TEST 3 (Cancelación): Usar Product 2200 + Material 1200 + Packaging 5200
-- - Material 1200 con 500kg disponibles
-- - Crear orden → reserva 50kg
-- - Cancelar → debe liberar reserva
