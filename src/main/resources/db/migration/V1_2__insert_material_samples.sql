-- Script completo: limpia la BD y crea datos de ejemplo desde cero
BEGIN;

-- 0) BORRAR TODO (reinicia secuencias automáticamente)
TRUNCATE TABLE recipes RESTART IDENTITY CASCADE;
TRUNCATE TABLE product_phases RESTART IDENTITY CASCADE;
TRUNCATE TABLE products RESTART IDENTITY CASCADE;
TRUNCATE TABLE materials RESTART IDENTITY CASCADE;
TRUNCATE TABLE packagings RESTART IDENTITY CASCADE;
TRUNCATE TABLE movements RESTART IDENTITY CASCADE;
TRUNCATE TABLE production_orders RESTART IDENTITY CASCADE;
TRUNCATE TABLE batches RESTART IDENTITY CASCADE;
-- Limpiar usuarios y roles (user_roles se limpia automáticamente por CASCADE)
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- ===========================
-- 1) MATERIALS (type mapping: 0=MALTA,1=LUPULO,2=AGUA,3=LEVADURA,4=ENVASE,5=OTROS)
-- Aumenté stocks para poder crear varias órdenes
INSERT INTO materials (id, code, creation_date, is_active, last_update_date, name, reserved_stock, stock, supplier, threshold, type, unit_measurement, value)
VALUES
(1,'MAL-1', now()-interval '120 days', TRUE, now()-interval '2 days',  'Malta Pale',           0.0, 20000.0, 'Molino San Martín', 50.0, 0, 'KG', 0.0),
(2,'MAL-2', now()-interval '120 days', TRUE, now()-interval '2 days',  'Malta Crystal',        0.0, 10000.0, 'Molino San Martín', 10.0, 0, 'KG', 0.0),
(3,'MAL-3', now()-interval '120 days', TRUE, now()-interval '14 days', 'Malta Chocolate',      0.0,  8000.0, 'Molino San Martín',  5.0, 0, 'KG', 0.0),
(4,'LUP-4', now()-interval '90 days',  TRUE, now()-interval '1 day',   'Lúpulo Citra',         0.0,  2000.0, 'HopsCo',            1.0, 1, 'KG', 0.0),
(5,'LUP-5', now()-interval '90 days',  TRUE, now()-interval '1 day',   'Lúpulo Simcoe',        0.0,  1500.0, 'HopsCo',            1.0, 1, 'KG', 0.0),
(6,'LEV-6', now()-interval '60 days',  TRUE, now()-interval '2 days',  'Levadura Ale',         0.0,  2000.0, 'Fermentos AR',      0.2, 3, 'KG', 0.0),
(7,'LEV-7', now()-interval '60 days',  TRUE, now()-interval '2 days',  'Levadura Lager',       0.0,  1500.0, 'Fermentos AR',      0.2, 3, 'KG', 0.0),
(8,'AGU-8', now()-interval '365 days', TRUE, now()-interval '1 day',   'Agua potable',         0.0,1000000.0, 'Acueducto Local',   500.0, 2, 'LT', 0.0),
(9,'OTR-9', now()-interval '300 days', TRUE, now()-interval '10 days',  'Azúcar',               0.0, 20000.0, 'Proveeduría',       10.0, 5, 'KG', 0.0),
(10,'OTR-10', now()-interval '200 days', TRUE, now()-interval '10 days','Dextrina / Adjuntos',  0.0, 10000.0, 'Proveeduría',        5.0, 5, 'KG', 0.0),
(11,'OTR-11', now()-interval '150 days', TRUE, now()-interval '20 days','Clarificante', 0.0,  5000.0, 'Química Brews',      2.0, 5, 'LT', 0.0),
(12,'OTR-12', now()-interval '200 days', TRUE, now()-interval '2 days',   'CO2',                  0.0, 50000.0, 'Gases SRL',          5.0, 5, 'KG', 0.0),
(13,'ENV-13', now()-interval '400 days', TRUE, now()-interval '5 days',   'Botella 330ml',        0.0,500000.0,'Envases SA',        200.0, 4, 'UNIDAD', 0.0),
(14,'OTR-14', now()-interval '400 days', TRUE, now()-interval '5 days',   'Tapa 26mm',            0.0,500000.0,'Envases SA',        300.0, 4, 'UNIDAD', 0.0),
(15,'OTR-15', now()-interval '400 days', TRUE, now()-interval '5 days',   'Etiqueta 330ml',       0.0,300000.0,'Imprenta Local',    200.0, 5, 'UNIDAD', 0.0),
(16,'ENV-16', now()-interval '400 days', TRUE, now()-interval '5 days',   'Barril 20L',           0.0,  5000.0, 'Envases SA',          5.0, 4, 'UNIDAD', 0.0),
(17,'OTR-17', now()-interval '200 days', TRUE, now()-interval '30 days', 'Adsorbente columna',   0.0,  5000.0, 'Química Brews',      1.0, 5, 'KG', 0.0);

-- ===========================
-- 2) PRODUCTS (3 productos: 2 alcohólicos y 1 no alcohólico)
INSERT INTO products (id, creation_date, is_active, is_alcoholic, is_ready, name, standard_quantity, unit_measurement)
VALUES
(1,now()-interval '120 days', TRUE, TRUE,  FALSE, 'Pale Ale Clásica', 1000.0, 'LT'),
(2,now()-interval '110 days', TRUE, TRUE,  FALSE, 'Stout Intensa',    800.0,  'LT'),
(3,now()-interval '90 days',  TRUE, FALSE, FALSE, 'Pale Sin Alcohol',1000.0, 'LT');

-- Recupero ids recién creados para usarlos con claridad (opcional pero útil)
-- NOTA: si ejecutás en psql podés usar RETURNING; aquí asumimos ids 1..3 por RESTART IDENTITY.

-- ===========================
-- 3) PRODUCT_PHASES
-- Para cada producto creamos todas las fases aplicables según getApplicablePhases()
-- Fases estándar: MOLIENDA, MACERACION, FILTRACION, COCCION, FERMENTACION, MADURACION, GASIFICACION, ENVASADO
-- Para no alcohólico se inserta DESALCOHOLIZACION entre FERMENTACION y MADURACION

-- Producto 1: Pale Ale (alcohólica) - Valores realistas de cervecería
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product) VALUES
(1, now()-interval '119 days', 2.0,   240.000, TRUE, 238.000, 'KG', 'MOLIENDA', 1),        -- 2h, pérdida 0.8%
(2, now()-interval '119 days', 5.0,  1200.000, TRUE,1050.000, 'LT','MACERACION', 1),       -- 5h, concentración del mosto
(3, now()-interval '119 days', 2.0,  1050.000, TRUE,1000.000, 'LT','FILTRACION', 1),       -- 2h, pérdida por filtrado 5%
(4, now()-interval '119 days', 2.5,  1000.000, TRUE, 950.000, 'LT','COCCION', 1),          -- 2.5h, evaporación 5%
(5, now()-interval '118 days',168.0,  950.000, TRUE, 920.000, 'LT','FERMENTACION', 1),     -- 7 días, pérdida por sedimento
(6, now()-interval '111 days',240.0,  920.000, TRUE, 900.000, 'LT','MADURACION', 1),       -- 10 días, clarificación
(7, now()-interval '109 days', 3.0,   900.000, TRUE, 900.000, 'LT','GASIFICACION', 1),     -- 3h, adición CO2
(8, now()-interval '110 days', 8.0,   900.000, TRUE, 895.000, 'LT','ENVASADO', 1);         -- 8h, pérdida minimal

-- Producto 2: Stout Intensa (alcohólica) - Proceso más complejo
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product) VALUES
(9,  now()-interval '109 days', 2.5,   220.000, TRUE, 218.000, 'KG','MOLIENDA', 2),        -- 2.5h, molienda más fina
(10, now()-interval '109 days', 6.0,   980.000, TRUE, 850.000, 'LT','MACERACION', 2),      -- 6h, maceración compleja
(11, now()-interval '108 days', 2.5,   850.000, TRUE, 800.000, 'LT','FILTRACION', 2),      -- 2.5h, filtrado más lento
(12, now()-interval '109 days', 3.0,   800.000, TRUE, 750.000, 'LT','COCCION', 2),         -- 3h, más evaporación
(13, now()-interval '108 days',192.0,  750.000, TRUE, 720.000, 'LT','FERMENTACION', 2),    -- 8 días, fermentación lenta
(14, now()-interval '105 days',336.0,  720.000, TRUE, 705.000, 'LT','MADURACION', 2),      -- 14 días, maduración larga
(15, now()-interval '104 days', 3.0,   705.000, TRUE, 705.000, 'LT','GASIFICACION', 2),    -- 3h, carbonatación
(16, now()-interval '104 days',10.0,   705.000, TRUE, 700.000, 'LT','ENVASADO', 2);        -- 10h, proceso más lento

-- Producto 3: Pale Sin Alcohol (no alcohólica) - Incluye proceso de desalcoholización
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product) VALUES
(17, now()-interval '89 days', 2.0,   225.000, TRUE, 223.000, 'KG', 'MOLIENDA', 3),        -- 2h, molienda estándar
(18, now()-interval '89 days', 5.0,  1200.000, TRUE,1050.000, 'LT','MACERACION', 3),       -- 5h, maceración normal
(19, now()-interval '89 days', 2.0,  1050.000, TRUE,1000.000, 'LT','FILTRACION', 3),       -- 2h, filtrado estándar
(20, now()-interval '89 days', 2.5,  1000.000, TRUE, 950.000, 'LT','COCCION', 3),          -- 2.5h, cocción normal
(21, now()-interval '88 days',144.0,  950.000, TRUE, 920.000, 'LT','FERMENTACION', 3),     -- 6 días, fermentación corta
(22, now()-interval '80 days', 48.0,  920.000, TRUE, 900.000, 'LT','DESALCOHOLIZACION', 3), -- 48h, proceso de desalco
(23, now()-interval '79 days',120.0,  900.000, TRUE, 890.000, 'LT','MADURACION', 3),       -- 5 días, maduración
(24, now()-interval '77 days', 4.0,   890.000, TRUE, 890.000, 'LT','GASIFICACION', 3),     -- 4h, carbonatación suave
(25, now()-interval '78 days', 8.0,   890.000, TRUE, 885.000, 'LT','ENVASADO', 3);         -- 8h, envasado cuidadoso

-- ===========================
-- 4) PACKAGINGS (unit_measurement = 'LT' por pedido tuyo)
INSERT INTO packagings (id, creation_date, is_active, name, quantity, unit_measurement, id_material)
VALUES
(1,now()-interval '400 days', TRUE, 'Botella 330ml (equiv. en LT)', 0.330, 'LT', (SELECT id FROM materials WHERE code = 'ENV-13' LIMIT 1)),
(2,now()-interval '400 days', TRUE, 'Barril 20L (equiv. en LT)',     20, 'LT', (SELECT id FROM materials WHERE code = 'ENV-16' LIMIT 1));

-- ===========================
-- 5) RECIPES: garantizar que cada fase tenga al menos una receta con material del tipo requerido
-- Mapping de tipos:
-- MOLIENDA -> MALTA (type 0)
-- MACERACION -> AGUA (type 2)
-- FILTRACION -> (vacío) -> usar OTROS (type 5) (clarificante)
-- COCCION -> AGUA (2) y LUPULO (1)
-- FERMENTACION -> LEVADURA (3)
-- MADURACION -> (vacío) -> usar CO2 (type 5)
-- GASIFICACION -> (vacío) -> usar CO2 (type 5)
-- ENVASADO -> ENVASE (type 4)
-- DESALCOHOLIZACION -> OTROS (Adsorbente / VACUUM-CHAR)

-- Para simplicidad inserto con subselects que referencian los ids recién creados según code
-- Se actualizan los códigos a los nuevos valores de la tabla materials:
-- MALT-PALE  -> MAL-1
-- WATER      -> AGU-8
-- HOPS-CITRA -> LUP-4
-- HOPS-SIMCOE-> LUP-5
-- YEAST-ALE  -> LEV-6
-- YEAST-LAGER-> LEV-7
-- WHIRLPOOL-CLAR -> OTR-11
-- CO2 -> OTR-12
-- BOTTLE-330 -> ENV-13
-- KEG-20L -> ENV-16
-- VACUUM-CHAR -> OTR-17

-- ---------- Producto 1: Pale Ale Clásica ----------
-- (recipes 1..9) -> la suma de cantidades de MOLIENDA = input de MOLIENDA (240 KG)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(1, now(), 180.000, (SELECT id FROM materials WHERE code='MAL-1' LIMIT 1), 1),  -- Malta Pale (180 kg)
(2, now(),  60.000, (SELECT id FROM materials WHERE code='MAL-2' LIMIT 1), 1),  -- Malta Crystal (60 kg)

-- MACERACION: agua = 1200 LT (input de la fase)
(3, now(), 1200.000, (SELECT id FROM materials WHERE code='AGU-8' LIMIT 1), 2),  -- Agua (1200 L)

-- FILTRACION: clarificante pequeño (ingrediente auxiliar)
(4, now(),    2.000, (SELECT id FROM materials WHERE code='OTR-11' LIMIT 1), 3),  -- Clarificante (2 kg)

-- COCCION: lúpulo (amargor/aroma) - cantidad en kg (o g según tu convención)
(5, now(),    6.000, (SELECT id FROM materials WHERE code='LUP-4' LIMIT 1), 4),  -- Lúpulo (6 kg)

-- FERMENTACION: levadura (cantidad en kg) - insumo auxiliar, pequeño
(6, now(),    1.500, (SELECT id FROM materials WHERE code='LEV-6' LIMIT 1), 5),  -- Levadura (1.5 kg)

-- MADURACION: CO2 para acondicionar (ejemplo)
(7, now(),   12.000, (SELECT id FROM materials WHERE code='OTR-12' LIMIT 1), 6),  -- CO2 (12 kg equiv)

-- GASIFICACION: CO2
(8, now(),   12.000, (SELECT id FROM materials WHERE code='OTR-12' LIMIT 1), 7),  -- CO2 (12 kg equiv)

-- ENVASADO: envases (cantidad en unidades; representada en tu tabla como 'quantity' - ejemplo 3000 botellas)
(9, now(), 3000.000, (SELECT id FROM materials WHERE code='ENV-13' LIMIT 1), 8);  -- Envases (3000 unidades)

-- ---------- Producto 2: Stout Intensa ----------
-- (recipes 10..18) -> MOLIENDA suma = 220 KG
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(10, now(), 140.000, (SELECT id FROM materials WHERE code='MAL-1' LIMIT 1), 9),  -- Malta base (140 kg)
(11, now(),  40.000, (SELECT id FROM materials WHERE code='MAL-3' LIMIT 1), 9),  -- Malta Chocolate (40 kg)
(12, now(),  40.000, (SELECT id FROM materials WHERE code='MAL-2' LIMIT 1), 9);  -- Malta Crystal (40 kg)

-- MACERACION: agua = 980 LT
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(13, now(), 980.000, (SELECT id FROM materials WHERE code='AGU-8' LIMIT 1), 10); -- Agua (980 L)

-- FILTRACION: clarificante pequeño
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(14, now(),   2.000, (SELECT id FROM materials WHERE code='OTR-11' LIMIT 1), 11); -- Clarificante (2 kg)

-- COCCION: lúpulo
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(15, now(),   5.000, (SELECT id FROM materials WHERE code='LUP-5' LIMIT 1), 12); -- Lúpulo (5 kg)

-- FERMENTACION: levadura
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(16, now(),   2.000, (SELECT id FROM materials WHERE code='LEV-7' LIMIT 1), 13); -- Levadura (2 kg)

-- MADURACION: CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(17, now(),   9.600, (SELECT id FROM materials WHERE code='OTR-12' LIMIT 1), 14); -- CO2 (9.6 kg)

-- GASIFICACION: CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(18, now(),   9.600, (SELECT id FROM materials WHERE code='OTR-12' LIMIT 1), 15); -- CO2 (9.6 kg)

-- ENVASADO: envases KEG (ejemplo 2000 unidades equivalentes)
-- (envase asociado a phase id = 16)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(19, now(), 2000.000, (SELECT id FROM materials WHERE code='ENV-16' LIMIT 1), 16); -- Kegs (2000 unidades)

-- ---------- Producto 3: Pale Sin Alcohol ----------
-- (recipes 20..29) -> MOLIENDA suma = 225 KG
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(20, now(), 180.000, (SELECT id FROM materials WHERE code='MAL-1' LIMIT 1), 17), -- Malta Pale (180 kg)
(21, now(),  45.000, (SELECT id FROM materials WHERE code='MAL-2' LIMIT 1), 17); -- Malta Crystal (45 kg)

-- MACERACION: agua = 1200 LT (phase id = 18)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(22, now(), 1200.000, (SELECT id FROM materials WHERE code='AGU-8' LIMIT 1), 18); -- Agua (1200 L)

-- FILTRACION: clarificante (phase id = 19)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(23, now(),    2.000, (SELECT id FROM materials WHERE code='OTR-11' LIMIT 1), 19); -- Clarificante (2 kg)

-- COCCION: lúpulo (phase id = 20)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(24, now(),    5.500, (SELECT id FROM materials WHERE code='LUP-4' LIMIT 1), 20); -- Lúpulo (5.5 kg)

-- FERMENTACION: levadura (phase id = 21)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(25, now(),    1.600, (SELECT id FROM materials WHERE code='LEV-6' LIMIT 1), 21); -- Levadura (1.6 kg)

-- DESALCOHOLIZACION: agente/proceso (phase id = 22)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(26, now(),    8.000, (SELECT id FROM materials WHERE code='OTR-17' LIMIT 1), 22); -- Agente desalco (8 kg equiv)

-- MADURACION: CO2 + dextrina (phase id = 23)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(27, now(),   10.000, (SELECT id FROM materials WHERE code='OTR-12' LIMIT 1), 23), -- CO2 (10 kg)
(28, now(),    5.000, (SELECT id FROM materials WHERE code='OTR-9'  LIMIT 1), 23); -- Dextrina/azúcar (5 kg)

-- GASIFICACION: CO2 (phase id = 24)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(29, now(),   10.000, (SELECT id FROM materials WHERE code='OTR-12' LIMIT 1), 24); -- CO2 (10 kg)

-- ENVASADO (phase id = 25) -> envases botellas (3000 unidades equiv)
-- Si querés un recipe por envase distinto por lote, lo modelamos aquí
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase) VALUES
(30, now(), 3000.000, (SELECT id FROM materials WHERE code='ENV-13' LIMIT 1), 25); -- Envases (3000 unidades)

-- ===========================
-- 6) MOVEMENTS: crear movimientos de ingreso inicial para reflejar stocks (opcional, útil para auditar)
INSERT INTO movements (id, id_usuario, realization_date, reason, stock, type, id_material)
VALUES
(1,NULL, now()-interval '119 days', 'Ingreso inicial Maltas', 5000.0, 'INGRESO', (SELECT id FROM materials WHERE code='MAL-1' LIMIT 1)),
(2,NULL, now()-interval '119 days', 'Ingreso inicial Agua', 250000.0, 'INGRESO', (SELECT id FROM materials WHERE code='AGU-8' LIMIT 1)),
(3,NULL, now()-interval '110 days', 'Ingreso envases', 150000.0, 'INGRESO', (SELECT id FROM materials WHERE code='ENV-13' LIMIT 1));

-- ===========================
-- 7) MARCAR TODOS LOS product_phases Y products COMO ready = TRUE (requisito pedido)
UPDATE product_phases SET is_ready = TRUE;
UPDATE products SET is_ready = TRUE;

-- ===========================
-- 8) CREAR BATCHES para los 3 ordenes
INSERT INTO batches (id, code, completed_date, creation_date, estimated_completed_date, 
                    planned_date, quantity, start_date, status, id_packaging)
VALUES
(1, 'LOT-001', NULL, now(), NULL, now() + interval '5 days', 1000.0, NULL, 'PENDIENTE', 1),
(2, 'LOT-002', NULL, now(), NULL, now() + interval '10 days', 800.0, NULL, 'PENDIENTE', 1),
(3, 'LOT-003', NULL, now(), NULL, now() + interval '15 days', 1000.0, NULL, 'PENDIENTE', 1);

-- ===========================
-- 9) CREAR PRODUCTION_ORDERS para los 3 productos listos
-- production_orders columns as in your DER: (id, creation_date, quantity, status, validation_date, id_batch, id_product)
INSERT INTO production_orders (id, creation_date, quantity, status, validation_date, id_batch, id_product)
VALUES
(1,now(), 1000.0, 'PENDIENTE', NULL, 1, (SELECT id FROM products WHERE name='Pale Ale Clásica' LIMIT 1)),
(2,now(),  800.0, 'PENDIENTE', NULL, 2, (SELECT id FROM products WHERE name='Stout Intensa' LIMIT 1)),
(3,now(),1000.0, 'PENDIENTE', NULL, 3, (SELECT id FROM products WHERE name='Pale Sin Alcohol' LIMIT 1));

-- ===========================



-- 11) Crear usuarios de ejemplo con roles asignados
-- Crear usuarios con contraseñas encoded (BCrypt)
-- Contraseñas: Admin1234, Operario123, Supervisor123, GerenteG123, GerenteP123

-- Usuario 1: ADMIN
INSERT INTO users (
    id, username, password, name, enabled, account_non_expired, 
    account_non_locked, credentials_non_expired, is_active, 
    creation_date, email, last_login_date, phone_number
) VALUES (
    1, 'admin',
    '$2a$10$6Ze1fILsxzJ2by6KAKTeSO4AFvbHjOndEc0WyTKlMIXPlhBbEammm',
    'Administrador del Sistema',
    true, true, true, true, true,
    now(), 'admin@brewery.com', NULL, '+54911111111'
);

-- Asignar rol ADMIN al usuario admin
INSERT INTO user_roles (user_id, role_id) VALUES
(1, (SELECT id FROM roles WHERE name = 'ADMIN'));

-- Usuario 2: OPERARIO
INSERT INTO users (
    id, username, password, name, enabled, account_non_expired, 
    account_non_locked, credentials_non_expired, is_active, 
    creation_date, email, last_login_date, phone_number
) VALUES (
    2, 'operario',
    '$2a$10$UgL2q08c0PeLC2tJi65QM.kdC1AWFIj6QjTpTb3x1D69GNhAp9Af.',
    'Operario General',
    true, true, true, true, true,
    now(), 'operario@brewery.com', NULL, '+54922222222'
);

-- Asignar todos los roles OPERARIO_* al usuario operario
INSERT INTO user_roles (user_id, role_id) VALUES
(2, (SELECT id FROM roles WHERE name = 'OPERARIO_DE_CALIDAD')),
(2, (SELECT id FROM roles WHERE name = 'OPERARIO_DE_ALMACEN')),
(2, (SELECT id FROM roles WHERE name = 'OPERARIO_DE_PRODUCCION'));

-- Usuario 3: SUPERVISOR
INSERT INTO users (
    id, username, password, name, enabled, account_non_expired, 
    account_non_locked, credentials_non_expired, is_active, 
    creation_date, email, last_login_date, phone_number
) VALUES (
    3, 'supervisor',
    '$2a$10$3lQYpGCqm3xPRN3AjRSkbuuXBdKMWsbhATgrF8iGMUwKEw1FlfqXq',
    'Supervisor General',
    true, true, true, true, true,
    now(), 'supervisor@brewery.com', NULL, '+54933333333'
);

-- Asignar todos los roles SUPERVISOR_* al usuario supervisor
INSERT INTO user_roles (user_id, role_id) VALUES
(3, (SELECT id FROM roles WHERE name = 'SUPERVISOR_DE_CALIDAD')),
(3, (SELECT id FROM roles WHERE name = 'SUPERVISOR_DE_ALMACEN')),
(3, (SELECT id FROM roles WHERE name = 'SUPERVISOR_DE_PRODUCCION'));

-- Usuario 4: GERENTE GENERAL
INSERT INTO users (
    id, username, password, name, enabled, account_non_expired, 
    account_non_locked, credentials_non_expired, is_active, 
    creation_date, email, last_login_date, phone_number
) VALUES (
    4, 'gerente_general',
    '$2a$10$uznmY2P8cFGO4TD2iB9qj.1JKc2/7DATKgEabLD1F.RTPpWdpM1fG',
    'Gerente General',
    true, true, true, true, true,
    now(), 'gerente.general@brewery.com', NULL, '+54944444444'
);

-- Asignar rol GERENTE_GENERAL al usuario gerente_general
INSERT INTO user_roles (user_id, role_id) VALUES
(4, (SELECT id FROM roles WHERE name = 'GERENTE_GENERAL'));

-- Usuario 5: GERENTE DE PLANTA
INSERT INTO users (
    id, username, password, name, enabled, account_non_expired, 
    account_non_locked, credentials_non_expired, is_active, 
    creation_date, email, last_login_date, phone_number
) VALUES (
    5, 'gerente_planta',
    '$2a$10$OmhOZYXAT43.Ex3.HcxbuuBC9Ew5Qz2ME2WgFOnclR.urWtCUF8vi',
    'Gerente de Planta',
    true, true, true, true, true,
    now(), 'gerente.planta@brewery.com', NULL, '+54955555555'
);

-- Asignar rol GERENTE_DE_PLANTA al usuario gerente_planta
INSERT INTO user_roles (user_id, role_id) VALUES
(5, (SELECT id FROM roles WHERE name = 'GERENTE_DE_PLANTA'));

-- 12) Ajustar secuencias
SELECT setval('materials_seq', (SELECT MAX(id) FROM materials));
SELECT setval('products_seq',  (SELECT MAX(id) FROM products));
SELECT setval('product_phases_seq', (SELECT MAX(id) FROM product_phases));
SELECT setval('recipes_seq',   (SELECT MAX(id) FROM recipes));
SELECT setval('packagings_seq',(SELECT MAX(id) FROM packagings));
SELECT setval('movements_seq', (SELECT MAX(id) FROM movements));
SELECT setval('production_orders_seq', (SELECT MAX(id) FROM production_orders));
SELECT setval('batches_seq',   (SELECT MAX(id) FROM batches));
SELECT setval('user_seq', (SELECT MAX(id) FROM users));


COMMIT;