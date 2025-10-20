BEGIN;

-- ===========================
-- Datos de ejemplo: PyME Cervecera (adaptado a tipos reales)
-- Notas:
--  - materials.type: integer (0 MALTA, 1 LUPULO, 2 AGUA, 3 LEVADURA, 4 ENVASE, 5 OTROS)
--  - unit_measurement -> varchar: 'KG','LT','UNIDAD'
--  - product_phases.input/output -> float8 (cantidad numérica)
--  - phase -> varchar: 'MOLIENDA','MACERACION','FILTRACION','COCCION','FERMENTACION','MADURACION','GASIFICACION','ENVASADO','DESALCOHOLIZACION'
--  - movement.type assumed varchar ('INGRESO','EGRESO', etc.)
-- ===========================

-- ---------- MATERIALS ----------
INSERT INTO materials (id, code, creation_date, is_active, last_update_date, name, reserved_stock, stock, supplier, threshold, type, unit_measurement, value)
VALUES
(1,  'MALT-PALE', now()-interval '120 days', TRUE, now()-interval '2 days',  'Malta Pale',           0.0, 500.0, 'Molino San Martín', 50.0, 0, 'KG', 0.0),
(2,  'MALT-CRYST',now()-interval '120 days', TRUE, now()-interval '2 days',  'Malta Crystal',        0.0,  80.0, 'Molino San Martín', 10.0, 0, 'KG', 0.0),
(3,  'MALT-CHOC', now()-interval '120 days', TRUE, now()-interval '14 days', 'Malta Chocolate',      0.0,  25.0, 'Molino San Martín',  5.0, 0, 'KG', 0.0),
(4,  'HOPS-CITRA',now()-interval '90 days',  TRUE, now()-interval '1 day',   'Lúpulo Citra',         0.0,   6.0, 'HopsCo',            1.0, 1, 'KG', 0.0),
(5,  'HOPS-SIMCOE',now()-interval '90 days', TRUE, now()-interval '1 day',   'Lúpulo Simcoe',        0.0,   4.5, 'HopsCo',            1.0, 1, 'KG', 0.0),
(6,  'YEAST-01',  now()-interval '60 days',  TRUE, now()-interval '2 days',  'Levadura Ale',         0.0,   3.0, 'Fermentos AR',      0.2, 3, 'KG', 0.0),
(7,  'YEAST-LA1', now()-interval '60 days',  TRUE, now()-interval '2 days',  'Levadura Lager',       0.0,   2.0, 'Fermentos AR',      0.2, 3, 'KG', 0.0),
(8,  'WATER',     now()-interval '365 days', TRUE, now()-interval '1 day',   'Agua potable',         0.0,20000.0, 'Acueducto Local',   500.0, 2, 'LT', 0.0),
(9,  'SUGAR',     now()-interval '300 days', TRUE, now()-interval '10 days',  'Azúcar',               0.0, 150.0, 'Proveeduría',       10.0, 5, 'KG', 0.0),
(10, 'DEXTRIN',   now()-interval '200 days', TRUE, now()-interval '10 days',  'Dextrina / Adjuntos',  0.0,  40.0, 'Proveeduría',        5.0, 5, 'KG', 0.0),
(11, 'WHIRLPOOL-CLAR', now()-interval '150 days', TRUE, now()-interval '20 days','Clarificante', 0.0,  20.0, 'Química Brews',      2.0, 5, 'LT', 0.0),
(12, 'CO2',       now()-interval '200 days', TRUE, now()-interval '2 days',   'CO2',                  0.0,  50.0, 'Gases SRL',          5.0, 5, 'KG', 0.0),
(13, 'BOTTLE-330',now()-interval '400 days', TRUE, now()-interval '5 days',   'Botella 330ml',        0.0,3000.0, 'Envases SA',        200.0, 4, 'UNIDAD', 0.0),
(14, 'CAP-330',   now()-interval '400 days', TRUE, now()-interval '5 days',   'Tapa 26mm',            0.0,3500.0, 'Envases SA',        300.0, 4, 'UNIDAD', 0.0),
(15, 'LABEL-330', now()-interval '400 days', TRUE, now()-interval '5 days',   'Etiqueta 330ml',       0.0,3200.0, 'Imprenta Local',    200.0, 5, 'UNIDAD', 0.0),
(16, 'KEG-20L',   now()-interval '400 days', TRUE, now()-interval '5 days',   'Barril 20L',           0.0,   40.0, 'Envases SA',          5.0, 4, 'UNIDAD', 0.0),
(17, 'VACUUM-CHAR',now()-interval '200 days', TRUE, now()-interval '30 days', 'Adsorbente columna',   0.0,   10.0, 'Química Brews',      1.0, 5, 'KG', 0.0);

-- ---------- PRODUCTS ----------
-- unit_measurement stored as varchar
INSERT INTO products (id, creation_date, is_active, is_alcoholic, is_ready, name, standard_quantity, unit_measurement)
VALUES
(1, now()-interval '120 days', TRUE, TRUE, FALSE, 'Pale Ale Clásica', 1000.0, 'LT'),
(2, now()-interval '110 days', TRUE, TRUE, FALSE, 'Stout Intensa',    800.0,  'LT'),
(3, now()-interval '90 days',  TRUE, FALSE, FALSE, 'Pale Sin Alcohol',1000.0, 'LT');

-- ---------- PRODUCT_PHASES ----------
-- (id, creation_date, estimated_hours, input(float8), is_ready, output(float8), output_unit(varchar), phase(varchar), id_product)
-- INPUT/OUTPUT here son cantidades (kg o L) según la fase.
INSERT INTO product_phases (id, creation_date, estimated_hours, input, is_ready, output, output_unit, phase, id_product)
VALUES
-- Pale Ale (producto 1) standard_quantity = 1000 LT
(1, now()-interval '119 days', 1.0,   240.0, FALSE,    0.0,    'KG', 'MOLIENDA', 1),  -- molienda: entra ~240 kg maltas
(2, now()-interval '119 days', 4.0,  1200.0, FALSE, 1000.0,   'LT', 'MACERACION', 1),  -- maceración: 1200 L agua -> 1000 L mosto
(3, now()-interval '119 days', 1.0,    0.0, FALSE, 1000.0,   'LT', 'FILTRACION', 1),  -- lautering/filtrado
(4, now()-interval '119 days', 1.5,    0.0, FALSE, 1000.0,   'LT', 'COCCION', 1),      -- ebullición
(5, now()-interval '119 days', 0.5,    0.0, FALSE,  990.0,   'LT', 'FILTRACION', 1),  -- whirlpool/enfriado pérdida parcial
(6, now()-interval '118 days',168.0,    0.0, FALSE,  990.0,   'LT', 'FERMENTACION', 1), -- fermentación (volumen similar)
(7, now()-interval '111 days',72.0,     0.0, FALSE,  980.0,   'LT', 'MADURACION', 1),
(8, now()-interval '110 days',6.0,      0.0, FALSE,  980.0,   'LT', 'ENVASADO', 1),

-- Stout (producto 2) standard_quantity = 800 LT
(9,  now()-interval '109 days', 1.0,   220.0, FALSE,    0.0, 'KG', 'MOLIENDA', 2),
(10, now()-interval '109 days', 4.5,  980.0, FALSE,  800.0, 'LT', 'MACERACION', 2),
(11, now()-interval '109 days', 1.0,    0.0, FALSE,  800.0, 'LT', 'COCCION', 2),
(12, now()-interval '108 days',168.0,    0.0, FALSE,  800.0, 'LT', 'FERMENTACION', 2),
(13, now()-interval '105 days',96.0,     0.0, FALSE,  790.0, 'LT', 'MADURACION', 2),
(14, now()-interval '104 days',8.0,      0.0, FALSE,  790.0, 'LT', 'ENVASADO', 2),

-- Pale Sin Alcohol (producto 3) standard_quantity = 1000 LT, incluye DESALCOHOLIZACION
(15, now()-interval '89 days', 1.0,   225.0, FALSE,    0.0, 'KG', 'MOLIENDA', 3),
(16, now()-interval '89 days', 4.0,  1200.0, FALSE, 1000.0, 'LT', 'MACERACION', 3),
(17, now()-interval '89 days', 1.0,    0.0, FALSE, 1000.0, 'LT', 'COCCION', 3),
(18, now()-interval '88 days',168.0,    0.0, FALSE, 1000.0, 'LT', 'FERMENTACION', 3),
(19, now()-interval '80 days',24.0,     0.0, FALSE, 1000.0, 'LT', 'DESALCOHOLIZACION', 3),
(20, now()-interval '79 days',48.0,     0.0, FALSE, 1000.0, 'LT', 'MADURACION', 3),
(21, now()-interval '78 days',6.0,      0.0, FALSE, 1000.0, 'LT', 'ENVASADO', 3);

-- ---------- RECIPES ----------
-- quantity: cantidad necesaria para producir la standard_quantity del product asociado
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
-- Pale Ale (1) -> standard_quantity 1000 LT
(1,  now()-interval '119 days', 220.0, 1, 1),  -- 220 kg Malta Pale (fase MOLIENDA)
(2,  now()-interval '119 days', 20.0,  2, 1),  -- 20 kg Malta Crystal
(3,  now()-interval '119 days',1200.0, 8, 2),  -- 1200 L Agua (MACERACION)
(4,  now()-interval '119 days',  5.0,10, 2),  -- 5 kg Dextrina/Adjuntos
(5,  now()-interval '119 days',  6.0, 4, 4),  -- 6 kg Lúpulo Citra (COCCION)
(6,  now()-interval '119 days',  1.5, 6, 6),  -- 1.5 kg Levadura Ale (FERMENTACION)
(7,  now()-interval '119 days',  2.0,11, 5),  -- 2 L Clarificante (FILTRACION/WHIRLPOOL)
(8,  now()-interval '118 days', 10.0, 9, 6),  -- 10 kg Azúcar (opcional priming en FERMENTACION)
(9,  now()-interval '111 days', 12.0,12, 7),  -- 12 kg CO2 (MADURACION)
(10, now()-interval '110 days',3000.0,13,8),  -- 3000 Botellas 330ml (ENVASADO)
(11, now()-interval '110 days',3000.0,14,8),  -- 3000 Tapas
(12, now()-interval '110 days',3000.0,15,8);  -- 3000 Etiquetas

-- Stout (2)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(13, now()-interval '109 days', 180.0, 1, 9),   -- 180 kg Malta Pale
(14, now()-interval '109 days', 40.0,  3, 9),   -- 40 kg Malta Chocolate
(15, now()-interval '109 days',980.0,  8,10),   -- 980 L Agua
(16, now()-interval '109 days',  5.0,  5,11),   -- 5 kg Lúpulo Simcoe
(17, now()-interval '109 days',  2.0,  6,11),   -- 2 kg Levadura (referencia)
(18, now()-interval '108 days',  2.0,  7,12),   -- 2 kg Levadura Lager (FERMENTACION)
(19, now()-interval '105 days',  9.6, 12,13),   -- 9.6 kg CO2 (MADURACION)
(20, now()-interval '104 days',2000.0,16,14);    -- 2000 Barriles 20L (ENVASADO)

-- Pale Sin Alcohol (3)
INSERT INTO recipes (id, creation_date, quantity, id_material, id_product_phase)
VALUES
(21, now()-interval '89 days', 210.0, 1,15),    -- 210 kg Malta Pale
(22, now()-interval '89 days', 15.0,  2,15),    -- 15 kg Crystal
(23, now()-interval '89 days',1200.0, 8,16),    -- 1200 L Agua
(24, now()-interval '89 days',  5.5,  4,17),    -- 5.5 kg Lúpulo Citra
(25, now()-interval '88 days',  1.6,  6,18),    -- 1.6 kg Levadura Ale
(26, now()-interval '80 days',  8.0, 17,19),    -- 8 kg Adsorbente (DESALCOHOLIZACION)
(27, now()-interval '79 days', 10.0, 12,20),    -- 10 kg CO2
(28, now()-interval '78 days',3000.0,13,21),
(29, now()-interval '78 days',3000.0,14,21),
(30, now()-interval '78 days',3000.0,15,21);

-- ---------- PACKAGINGS ----------
INSERT INTO packagings (id, creation_date, is_active, name, quantity, unit_measurement, id_material)
VALUES
(1, now()-interval '400 days', TRUE, 'Botella 330ml + tapa + etiqueta (kit)', 1.0, 'UNIDAD', 13),
(2, now()-interval '400 days', TRUE, 'Barril 20L',                             1.0, 'UNIDAD', 16);

-- ---------- MOVEMENTS (ejemplos de ingreso inicial) ----------
INSERT INTO movements (id, id_usuario, realization_date, reason, stock, type, id_material)
VALUES
(1, NULL, now()-interval '119 days', 'Ingreso inicial materia prima', 500.0, 'INGRESO', 1),
(2, NULL, now()-interval '119 days', 'Ingreso inicial materia prima',1200.0, 'INGRESO', 8),
(3, NULL, now()-interval '110 days', 'Ingreso envases',              3000.0, 'INGRESO', 13);

-- ===========================
-- Ajuste de secuencias (cambia nombres si tus secuencias difieren)
-- ===========================
SELECT setval('materials_seq', (SELECT COALESCE(MAX(id),0) FROM materials));
SELECT setval('products_seq',  (SELECT COALESCE(MAX(id),0) FROM products));
SELECT setval('product_phases_seq', (SELECT COALESCE(MAX(id),0) FROM product_phases));
SELECT setval('recipes_seq',   (SELECT COALESCE(MAX(id),0) FROM recipes));
SELECT setval('packagings_seq',(SELECT COALESCE(MAX(id),0) FROM packagings));
SELECT setval('movements_seq', (SELECT COALESCE(MAX(id),0) FROM movements));

COMMIT;
