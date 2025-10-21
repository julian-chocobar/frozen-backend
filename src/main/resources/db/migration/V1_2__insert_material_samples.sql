-- Script completo: limpia la BD y crea datos de ejemplo desde cero
BEGIN;

-- 0) BORRAR TODO (reinicia secuencias automáticamente)
TRUNCATE TABLE recipes, product_phases, production_orders, batches, packagings, movements, products, materials RESTART IDENTITY CASCADE;

-- ===========================
-- 1) MATERIALS (type mapping: 0=MALTA,1=LUPULO,2=AGUA,3=LEVADURA,4=ENVASE,5=OTROS)
-- Aumenté stocks para poder crear varias órdenes
INSERT INTO materials (id, code, creation_date, is_active, last_update_date, name, reserved_stock, stock, supplier, threshold, type, unit_measurement, value)
VALUES
(1,'MAL-1', now()-interval '120 days', TRUE, now()-interval '2 days',  'Malta Pale',           0.0, 5000.0, 'Molino San Martín', 50.0, 0, 'KG', 0.0),
(2,'MAL-2',now()-interval '120 days', TRUE, now()-interval '2 days',  'Malta Crystal',        0.0, 1500.0, 'Molino San Martín', 10.0, 0, 'KG', 0.0),
(3,'MAL-3', now()-interval '120 days', TRUE, now()-interval '14 days', 'Malta Chocolate',      0.0,  800.0, 'Molino San Martín',  5.0, 0, 'KG', 0.0),
(4,'LUP-4',now()-interval '90 days',  TRUE, now()-interval '1 day',   'Lúpulo Citra',         0.0,  200.0, 'HopsCo',            1.0, 1, 'KG', 0.0),
(5,'LUP-5',now()-interval '90 days', TRUE, now()-interval '1 day',   'Lúpulo Simcoe',        0.0,  150.0, 'HopsCo',            1.0, 1, 'KG', 0.0),
(6,'LEV-6',  now()-interval '60 days',  TRUE, now()-interval '2 days',  'Levadura Ale',         0.0,  500.0, 'Fermentos AR',      0.2, 3, 'KG', 0.0),
(7,'LEV-7',now()-interval '60 days',  TRUE, now()-interval '2 days',  'Levadura Lager',       0.0,  300.0, 'Fermentos AR',      0.2, 3, 'KG', 0.0),
(8,'AGU-8',      now()-interval '365 days', TRUE, now()-interval '1 day',   'Agua potable',         0.0,250000.0, 'Acueducto Local',   500.0, 2, 'LT', 0.0),
(9,'OTR-9',      now()-interval '300 days', TRUE, now()-interval '10 days',  'Azúcar',               0.0, 5000.0, 'Proveeduría',       10.0, 5, 'KG', 0.0),
(10,'OTR-10',    now()-interval '200 days', TRUE, now()-interval '10 days',  'Dextrina / Adjuntos',  0.0, 2000.0, 'Proveeduría',        5.0, 5, 'KG', 0.0),
(11,'OTR-11', now()-interval '150 days', TRUE, now()-interval '20 days','Clarificante', 0.0, 1000.0, 'Química Brews',      2.0, 5, 'LT', 0.0),
(12,'OTR-12',        now()-interval '200 days', TRUE, now()-interval '2 days',   'CO2',                  0.0, 10000.0,'Gases SRL',          5.0, 5, 'KG', 0.0),
(13,'ENV-13', now()-interval '400 days', TRUE, now()-interval '5 days',   'Botella 330ml',        0.0,150000.0,'Envases SA',        200.0, 4, 'UNIDAD', 0.0),
(14,'OTR-14',    now()-interval '400 days', TRUE, now()-interval '5 days',   'Tapa 26mm',            0.0,150000.0,'Envases SA',        300.0, 4, 'UNIDAD', 0.0),
(15,'OTR-15',  now()-interval '400 days', TRUE, now()-interval '5 days',   'Etiqueta 330ml',       0.0,150000.0,'Imprenta Local',    200.0, 5, 'UNIDAD', 0.0),
(16,'ENV-16',    now()-interval '400 days', TRUE, now()-interval '5 days',   'Barril 20L',           0.0,  500.0, 'Envases SA',          5.0, 4, 'UNIDAD', 0.0),
(17,'OTR-17',now()-interval '200 days', TRUE, now()-interval '30 days', 'Adsorbente columna',   0.0,  200.0, 'Química Brews',      1.0, 5, 'KG', 0.0);

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

-- Producto 1 (id = 1) - Pale Ale (alcohólica)
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product) VALUES
(1,now()-interval '119 days', 1.0, 240.0, TRUE, 0.0, 'KG', 'MOLIENDA', 1),
(2,now()-interval '119 days', 4.0,1200.0, TRUE,1000.0, 'LT','MACERACION', 1),
(3,now()-interval '119 days', 1.0,   0.0, TRUE,1000.0, 'LT','FILTRACION', 1),
(4,now()-interval '119 days', 1.5,   0.0, TRUE,1000.0, 'LT','COCCION', 1),
(5,now()-interval '118 days',168.0,  0.0, TRUE, 990.0, 'LT','FERMENTACION', 1),
(6,now()-interval '111 days',72.0,   0.0, TRUE, 980.0, 'LT','MADURACION', 1),
(7,now()-interval '109 days', 1.0,   0.0, TRUE,1000.0, 'LT','GASIFICACION', 1),
(8,now()-interval '110 days', 6.0,   0.0, TRUE, 980.0, 'LT','ENVASADO', 1);

-- Producto 2 (id = 2) - Stout (alcohólica)
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product) VALUES
(9,now()-interval '109 days', 1.0, 220.0, TRUE, 0.0, 'KG','MOLIENDA', 2),
(10,now()-interval '109 days', 4.5,980.0, TRUE,800.0,  'LT','MACERACION', 2),
(11,now()-interval '108 days', 1.0,   0.0, TRUE,800.0,  'LT','FILTRACION', 2),
(12,now()-interval '109 days', 1.0,   0.0, TRUE,800.0,  'LT','COCCION', 2),
(13,now()-interval '108 days',168.0,  0.0, TRUE,800.0,  'LT','FERMENTACION', 2),
(14,now()-interval '105 days',96.0,   0.0, TRUE,790.0,  'LT','MADURACION', 2),
(15,now()-interval '104 days', 1.0,   0.0, TRUE,800.0,  'LT','GASIFICACION', 2),
(16,now()-interval '104 days', 8.0,   0.0, TRUE,790.0,  'LT','ENVASADO', 2);

-- Producto 3 (id = 3) - Pale Sin Alcohol (NO alcohólica) -> incluye DESALCOHOLIZACION
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product) VALUES
(17,now()-interval '89 days', 1.0, 225.0, TRUE, 0.0, 'KG', 'MOLIENDA', 3),
(18,now()-interval '89 days', 4.0,1200.0, TRUE,1000.0,'LT','MACERACION', 3),
(19,now()-interval '89 days', 1.0,   0.0, TRUE,1000.0,'LT','FILTRACION', 3),
(20,now()-interval '89 days', 1.0,   0.0, TRUE,1000.0,'LT','COCCION', 3),
(21,now()-interval '88 days',168.0,  0.0, TRUE,1000.0,'LT','FERMENTACION', 3),
-- DESALCOHOLIZACION inserted here (must exist for non-alcoholic)
(22,now()-interval '80 days',24.0,    0.0, TRUE,1000.0,'LT','DESALCOHOLIZACION', 3),
(23,now()-interval '79 days',48.0,    0.0, TRUE,1000.0,'LT','MADURACION', 3),
(24,now()-interval '77 days', 1.0,    0.0, TRUE,1000.0,'LT','GASIFICACION', 3),
(25,now()-interval '78 days', 6.0,    0.0, TRUE,1000.0,'LT','ENVASADO', 3);

-- ===========================
-- 4) PACKAGINGS (unit_measurement = 'LT' por pedido tuyo)
INSERT INTO packagings (id, creation_date, is_active, name, quantity, unit_measurement, id_material)
VALUES
(1,now()-interval '400 days', TRUE, 'Botella 330ml (equiv. en LT)', 1.0, 'LT', (SELECT id FROM materials WHERE code = 'BOTTLE-330' LIMIT 1)),
(2,now()-interval '400 days', TRUE, 'Barril 20L (equiv. en LT)',     1.0, 'LT', (SELECT id FROM materials WHERE code = 'KEG-20L' LIMIT 1));

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
-- DESALCOHOLIZACION -> OTROS (id VACUUM-CHAR)

-- Para simplicidad inserto con subselects que referencian los ids recién creados según code
-- MALTA principal: 'MALT-PALE', AGUA: 'WATER', LUPULO: 'HOPS-CITRA' (y HOPS-SIMCOE), LEVADURA: 'YEAST-ALE', ENVASE: 'BOTTLE-330' o 'KEG-20L', OTROS: 'WHIRLPOOL-CLAR' o 'VACUUM-CHAR'

-- A) Producto 1 (Pale Ale) -> fase por fase
-- MOLIENDA
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(1,now()-interval '119 days', 220.0, (SELECT id FROM materials WHERE code='MALT-PALE' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='MOLIENDA' LIMIT 1));

-- MACERACION
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(2,now()-interval '119 days', 1200.0, (SELECT id FROM materials WHERE code='WATER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='MACERACION' LIMIT 1));

-- FILTRACION -> clarificante (OTROS)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(3,now()-interval '119 days', 2.0, (SELECT id FROM materials WHERE code='WHIRLPOOL-CLAR' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='FILTRACION' LIMIT 1));

-- COCCION -> AGUA + LUPULO
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(4,now()-interval '119 days', 100.0, (SELECT id FROM materials WHERE code='WATER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='COCCION' LIMIT 1)),
(5,now()-interval '119 days',   6.0, (SELECT id FROM materials WHERE code='HOPS-CITRA' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='COCCION' LIMIT 1));

-- FERMENTACION -> LEVADURA
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(6,now()-interval '118 days', 1.5, (SELECT id FROM materials WHERE code='YEAST-ALE' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='FERMENTACION' LIMIT 1));

-- MADURACION -> CO2 (OTROS)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(7,now()-interval '111 days', 12.0, (SELECT id FROM materials WHERE code='CO2' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='MADURACION' LIMIT 1));

-- GASIFICACION -> CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(8,now()-interval '109 days', 12.0, (SELECT id FROM materials WHERE code='CO2' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='GASIFICACION' LIMIT 1));

-- ENVASADO -> BOTELLAS
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(9,now()-interval '110 days', 3000.0, (SELECT id FROM materials WHERE code='BOTTLE-330' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=1 AND phase='ENVASADO' LIMIT 1));

-- ---------------------------
-- Producto 2 (Stout)
-- MOLIENDA
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(10,now()-interval '109 days', 180.0, (SELECT id FROM materials WHERE code='MALT-PALE' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='MOLIENDA' LIMIT 1));

-- MACERACION (agua)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(11,now()-interval '109 days', 980.0, (SELECT id FROM materials WHERE code='WATER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='MACERACION' LIMIT 1));

-- FILTRACION -> clarificante
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(12,now()-interval '108 days', 2.0, (SELECT id FROM materials WHERE code='WHIRLPOOL-CLAR' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='FILTRACION' LIMIT 1));

-- COCCION -> agua + lúpulo
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(13,now()-interval '109 days', 50.0, (SELECT id FROM materials WHERE code='WATER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='COCCION' LIMIT 1)),
(14,now()-interval '109 days',  5.0, (SELECT id FROM materials WHERE code='HOPS-SIMCOE' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='COCCION' LIMIT 1));

-- FERMENTACION -> levadura lager
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(15,now()-interval '108 days', 2.0, (SELECT id FROM materials WHERE code='YEAST-LAGER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='FERMENTACION' LIMIT 1));

-- MADURACION -> CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(16,now()-interval '105 days', 9.6, (SELECT id FROM materials WHERE code='CO2' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='MADURACION' LIMIT 1));

-- GASIFICACION -> CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(17,now()-interval '104 days', 9.6, (SELECT id FROM materials WHERE code='CO2' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='GASIFICACION' LIMIT 1));

-- ENVASADO -> KEGS (ejemplo)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(18,now()-interval '104 days', 2000.0, (SELECT id FROM materials WHERE code='KEG-20L' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=2 AND phase='ENVASADO' LIMIT 1));

-- ---------------------------
-- Producto 3 (Pale Sin Alcohol) -> incluye DESALCOHOLIZACION
-- MOLIENDA
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(19,now()-interval '89 days', 210.0, (SELECT id FROM materials WHERE code='MALT-PALE' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='MOLIENDA' LIMIT 1));

-- MACERACION agua
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(20,now()-interval '89 days',1200.0, (SELECT id FROM materials WHERE code='WATER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='MACERACION' LIMIT 1));

-- FILTRACION clarificante
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(21,now()-interval '87 days', 2.0, (SELECT id FROM materials WHERE code='WHIRLPOOL-CLAR' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='FILTRACION' LIMIT 1));

-- COCCION -> agua + lúpulo
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(22,now()-interval '89 days', 60.0, (SELECT id FROM materials WHERE code='WATER' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='COCCION' LIMIT 1)),
(23,now()-interval '89 days',  5.5, (SELECT id FROM materials WHERE code='HOPS-CITRA' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='COCCION' LIMIT 1));

-- FERMENTACION -> levadura ale
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(24,now()-interval '88 days', 1.6, (SELECT id FROM materials WHERE code='YEAST-ALE' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='FERMENTACION' LIMIT 1));

-- DESALCOHOLIZACION -> VACUUM-CHAR (OTROS)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(25,now()-interval '80 days', 8.0, (SELECT id FROM materials WHERE code='VACUUM-CHAR' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='DESALCOHOLIZACION' LIMIT 1));

-- MADURACION -> CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(26,now()-interval '79 days', 10.0, (SELECT id FROM materials WHERE code='CO2' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='MADURACION' LIMIT 1));

-- GASIFICACION -> CO2
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(27,now()-interval '77 days', 10.0, (SELECT id FROM materials WHERE code='CO2' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='GASIFICACION' LIMIT 1));

-- ENVASADO -> botellas
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(28,now()-interval '78 days', 3000.0, (SELECT id FROM materials WHERE code='BOTTLE-330' LIMIT 1), (SELECT id FROM product_phases WHERE id_product=3 AND phase='ENVASADO' LIMIT 1));

-- ===========================
-- 6) MOVEMENTS: crear movimientos de ingreso inicial para reflejar stocks (opcional, útil para auditar)
INSERT INTO movements (id, id_usuario, realization_date, reason, stock, type, id_material)
VALUES
(1,NULL, now()-interval '119 days', 'Ingreso inicial Maltas', 5000.0, 'INGRESO', (SELECT id FROM materials WHERE code='MALT-PALE' LIMIT 1)),
(2,NULL, now()-interval '119 days', 'Ingreso inicial Agua', 250000.0, 'INGRESO', (SELECT id FROM materials WHERE code='WATER' LIMIT 1)),
(3,NULL, now()-interval '110 days', 'Ingreso envases', 150000.0, 'INGRESO', (SELECT id FROM materials WHERE code='BOTTLE-330' LIMIT 1));

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
-- 10) Ajustar secuencias
SELECT setval('materials_seq', (SELECT MAX(id) FROM materials));
SELECT setval('products_seq',  (SELECT MAX(id) FROM products));
SELECT setval('product_phases_seq', (SELECT MAX(id) FROM product_phases));
SELECT setval('recipes_seq',   (SELECT MAX(id) FROM recipes));
SELECT setval('packagings_seq',(SELECT MAX(id) FROM packagings));
SELECT setval('movements_seq', (SELECT MAX(id) FROM movements));
SELECT setval('production_orders_seq', (SELECT MAX(id) FROM production_orders));

COMMIT;
