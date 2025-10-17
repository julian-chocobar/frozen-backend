-- Asegurarse de que la secuencia esté en el valor correcto
SELECT setval('materials_seq', COALESCE((SELECT MAX(id) FROM materials), 0) + 1, false);

-- Mapeo de tipos (orden del enum MaterialType):
-- 0 = MALTA
-- 1 = LUPULO
-- 2 = AGUA
-- 3 = LEVADURA
-- 4 = ENVASE
-- 5 = OTROS

-- Inserción de materiales de ejemplo para producción de cerveza
-- Materiales tipo MALTA (0)
INSERT INTO materials (id, code, name, type, supplier, value, stock, unit_measurement, threshold, is_active, creation_date, last_update_date)
VALUES 
    (1, 'MALT-001', 'Malta Pilsen', 0, 'Weyermann', 3.50, 500.0, 'KG', 100.0, true, NOW(), NOW()),
    (2, 'MALT-002', 'Malta Munich', 0, 'Weyermann', 4.20, 300.0, 'KG', 80.0, true, NOW(), NOW()),
    (3, 'MALT-003', 'Malta Caramelo 60L', 0, 'Briess', 5.10, 200.0, 'KG', 50.0, true, NOW(), NOW()),
    (4, 'MALT-004', 'Malta Chocolate', 0, 'Crisp', 6.80, 150.0, 'KG', 40.0, true, NOW(), NOW()),
    (5, 'MALT-005', 'Malta Trigo', 0, 'Weyermann', 4.50, 250.0, 'KG', 70.0, true, NOW(), NOW());

-- Materiales tipo LUPULO (1)
INSERT INTO materials (id, code, name, type, supplier, value, stock, unit_measurement, threshold, is_active, creation_date, last_update_date)
VALUES 
    (6, 'HOP-001', 'Cascade', 1, 'Yakima Valley Hops', 15.90, 20.0, 'KG', 5.0, true, NOW(), NOW()),
    (7, 'HOP-002', 'Citra', 1, 'Yakima Valley Hops', 18.50, 15.0, 'KG', 4.0, true, NOW(), NOW()),
    (8, 'HOP-003', 'Amarillo', 1, 'Hopsteiner', 14.20, 25.0, 'KG', 6.0, true, NOW(), NOW()),
    (9, 'HOP-004', 'Mosaic', 1, 'Yakima Valley Hops', 19.90, 18.0, 'KG', 5.0, true, NOW(), NOW()),
    (10, 'HOP-005', 'Hallertau', 1, 'Hopsteiner', 12.80, 30.0, 'KG', 8.0, true, NOW(), NOW());

-- Material tipo AGUA (2)
INSERT INTO materials (id, code, name, type, supplier, value, stock, unit_measurement, threshold, is_active, creation_date, last_update_date)
VALUES 
    (11, 'WTR-001', 'Agua Purificada', 2, 'Proveedor Local', 0.50, 5000.0, 'LT', 1000.0, true, NOW(), NOW());

-- Materiales tipo LEVADURA (3)
INSERT INTO materials (id, code, name, type, supplier, value, stock, unit_measurement, threshold, is_active, creation_date, last_update_date)
VALUES 
    (12, 'YST-001', 'Levadura Ale US-05', 3, 'Fermentis', 8.90, 50.0, 'KG', 10.0, true, NOW(), NOW()),
    (13, 'YST-002', 'Levadura Lager W-34/70', 3, 'Fermentis', 9.50, 40.0, 'KG', 8.0, true, NOW(), NOW());

-- Materiales tipo ENVASE (4)
INSERT INTO materials (id, code, name, type, supplier, value, stock, unit_measurement, threshold, is_active, creation_date, last_update_date)
VALUES
    (16, 'BTL-001', 'Botella Verde 330ml', 4, 'Envases S.A.', 0.20, 2000.0, 'UNIDAD', 500.0, true, NOW(), NOW()),
    (17, 'CAP-001', 'Tapa Corona', 4, 'Envases S.A.', 0.01, 10000.0, 'UNIDAD', 2000.0, true, NOW(), NOW()),
    (18, 'BOX-001', 'Caja 24 botellas', 4, 'Envases S.A.', 2.50, 500.0, 'UNIDAD', 100.0, true, NOW(), NOW());

-- Otros insumos comunes (5 para OTROS)
INSERT INTO materials (id, code, name, type, supplier, value, stock, unit_measurement, threshold, is_active, creation_date, last_update_date)
VALUES 
    (14, 'CLN-001', 'Limpiador PBW', 5, 'Five Star', 12.00, 25.0, 'KG', 5.0, true, NOW(), NOW()),
    (15, 'SAN-001', 'Sanitizador Star San', 5, 'Five Star', 15.00, 20.0, 'LT', 5.0, true, NOW(), NOW());
    
-- ...inserciones manuales...
SELECT setval('materials_seq', (SELECT MAX(id) FROM materials));