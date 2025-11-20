-- Script SQL para cargar datos de ejemplo de órdenes de producción y lotes
-- Este script asume que ya existen: productos, packagings, usuarios, sectores y quality_parameters
-- 5 órdenes con fechas específicas en 2025
-- Lotes 2 y 4 (Agosto y Octubre) tienen desperdicio (95% eficiencia)
-- TODOS los INSERTs son directos, sin bucles

DO $$
DECLARE
    -- Variables para IDs de batches
    batch1_id BIGINT;
    batch2_id BIGINT;
    batch3_id BIGINT;
    batch4_id BIGINT;
    batch5_id BIGINT;
    
    -- Variables para IDs de production orders
    order1_id BIGINT;
    order2_id BIGINT;
    order3_id BIGINT;
    order4_id BIGINT;
    order5_id BIGINT;
    
    -- Variables para IDs de production phases (cada orden tiene ~8-9 fases)
    -- Orden 1
    phase1_1_id BIGINT; -- MOLIENDA
    phase1_2_id BIGINT; -- MACERACION
    phase1_3_id BIGINT; -- FILTRACION
    phase1_4_id BIGINT; -- COCCION
    phase1_5_id BIGINT; -- FERMENTACION
    phase1_6_id BIGINT; -- MADURACION
    phase1_7_id BIGINT; -- GASIFICACION
    phase1_8_id BIGINT; -- ENVASADO
    -- Orden 2
    phase2_1_id BIGINT;
    phase2_2_id BIGINT;
    phase2_3_id BIGINT;
    phase2_4_id BIGINT;
    phase2_5_id BIGINT;
    phase2_6_id BIGINT;
    phase2_7_id BIGINT;
    phase2_8_id BIGINT;
    -- Orden 3
    phase3_1_id BIGINT;
    phase3_2_id BIGINT;
    phase3_3_id BIGINT;
    phase3_4_id BIGINT;
    phase3_5_id BIGINT;
    phase3_6_id BIGINT;
    phase3_7_id BIGINT;
    phase3_8_id BIGINT;
    phase3_9_id BIGINT;
    -- Orden 4
    phase4_1_id BIGINT;
    phase4_2_id BIGINT;
    phase4_3_id BIGINT;
    phase4_4_id BIGINT;
    phase4_5_id BIGINT;
    phase4_6_id BIGINT;
    phase4_7_id BIGINT;
    phase4_8_id BIGINT;
    -- Orden 5
    phase5_1_id BIGINT;
    phase5_2_id BIGINT;
    phase5_3_id BIGINT;
    phase5_4_id BIGINT;
    phase5_5_id BIGINT;
    phase5_6_id BIGINT;
    phase5_7_id BIGINT;
    phase5_8_id BIGINT;
    
    -- IDs de referencias
    product1_id BIGINT;
    product2_id BIGINT;
    product3_id BIGINT;
    packaging1_id BIGINT;
    packaging2_id BIGINT;
    packaging3_id BIGINT;
    supervisor_prod_id BIGINT;
    supervisor_calidad_id BIGINT;
    operario_calidad_id BIGINT;
    
    -- Variables para cálculos
    multiplier NUMERIC;
    standard_input NUMERIC;
    standard_output NUMERIC;
    current_input NUMERIC;
    current_output NUMERIC;
    efficiency_factor NUMERIC;
    sector_id BIGINT;
    quality_param_id BIGINT;
    quality_value VARCHAR;
    phase_order_val INTEGER;
    output_unit_val VARCHAR;
BEGIN
    -- Obtener IDs de usuarios
    SELECT id INTO supervisor_prod_id FROM users WHERE username = 'supervisorproduccion';
    SELECT id INTO supervisor_calidad_id FROM users WHERE username = 'supervisorcalidad';
    SELECT id INTO operario_calidad_id FROM users WHERE username = 'operariocalidad';
    
    -- Obtener productos (rotar entre los disponibles)
    SELECT id INTO product1_id FROM products WHERE is_active = true ORDER BY id LIMIT 1;
    SELECT id INTO product2_id FROM products WHERE is_active = true ORDER BY id LIMIT 1 OFFSET 1;
    SELECT id INTO product3_id FROM products WHERE is_active = true ORDER BY id LIMIT 1 OFFSET 2;
    
    -- Obtener packagings compatibles
    SELECT pkg.id INTO packaging1_id
    FROM packagings pkg
    INNER JOIN products p ON p.unit_measurement = pkg.unit_measurement
    WHERE p.id = product1_id AND pkg.is_active = true ORDER BY pkg.id LIMIT 1;
    
    SELECT pkg.id INTO packaging2_id
    FROM packagings pkg
    INNER JOIN products p ON p.unit_measurement = pkg.unit_measurement
    WHERE p.id = product2_id AND pkg.is_active = true ORDER BY pkg.id LIMIT 1;
    
    SELECT pkg.id INTO packaging3_id
    FROM packagings pkg
    INNER JOIN products p ON p.unit_measurement = pkg.unit_measurement
    WHERE p.id = product3_id AND pkg.is_active = true ORDER BY pkg.id LIMIT 1;
    
    -- ============================================================================
    -- ORDEN 1: Julio 15, 2025 (SIN DESPERDICIO - eficiencia 100%)
    -- ============================================================================
    
    -- Batch 1
    INSERT INTO batches (id, code, id_packaging, status, quantity, creation_date, planned_date, start_date, completed_date, estimated_completed_date)
    VALUES (
        nextval('batches_seq'),
        NULL,
        packaging1_id,
        'COMPLETADO',
        (SELECT CASE 
            WHEN pkg.quantity = 0.33 THEN FLOOR(p.standard_quantity / 0.33)::integer
            WHEN pkg.quantity = 20.0 THEN FLOOR(p.standard_quantity / 20.0)::integer
            ELSE 1000
         END
         FROM products p, packagings pkg
         WHERE p.id = product1_id AND pkg.id = packaging1_id),
        '2025-07-15 09:00:00'::timestamptz,
        '2025-07-15 09:00:00'::timestamptz,
        '2025-07-15 09:00:00'::timestamptz,
        '2025-08-14 09:00:00'::timestamptz,
        '2025-08-14 09:00:00'::timestamptz
    ) RETURNING id INTO batch1_id;
    
    UPDATE batches SET code = 'LOT-' || batch1_id WHERE id = batch1_id;
    
    -- Production Order 1
    INSERT INTO production_orders (id, id_batch, id_product, status, quantity, creation_date, validation_date, created_by_user_id, approved_by_user_id)
    VALUES (
        nextval('production_orders_seq'),
        batch1_id,
        product1_id,
        'APROBADA',
        (SELECT standard_quantity FROM products WHERE id = product1_id),
        '2025-07-15 09:00:00'::timestamptz,
        '2025-07-15 09:00:00'::timestamptz,
        supervisor_prod_id,
        supervisor_prod_id
    ) RETURNING id INTO order1_id;
    
    -- Calcular multiplicador
    SELECT po.quantity / p.standard_quantity INTO multiplier
    FROM production_orders po, products p
    WHERE po.id = order1_id AND p.id = product1_id;
    
    efficiency_factor := 1.0; -- Sin desperdicio
    
    -- Production Phase 1.1: MOLIENDA
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'MOLIENDA';
    
    current_input := standard_input * multiplier;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-07-15 09:00:00'::timestamptz,
        '2025-07-15 11:00:00'::timestamptz
    ) RETURNING id INTO phase1_1_id;
    
    -- Quality parameters para MOLIENDA - Granulometria
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_1_id, quality_param_id, '350 μm', true, true, 1, '2025-07-15 09:00:00'::timestamptz);
    
    -- Quality parameters para MOLIENDA - Humedad Malta
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_1_id, quality_param_id, '4.5 %', true, true, 1, '2025-07-15 09:00:00'::timestamptz);
    
    -- Production Phase 1.2: MACERACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'MACERACION';
    
    current_input := current_output; -- El output de MOLIENDA es el input de MACERACION
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-07-15 11:00:00'::timestamptz,
        '2025-07-15 16:00:00'::timestamptz
    ) RETURNING id INTO phase1_2_id;
    
    -- Quality parameters para MACERACION - Temp Macerac
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_2_id, quality_param_id, '66 °C', true, true, 1, '2025-07-15 11:00:00'::timestamptz);
    
    -- Quality parameters para MACERACION - pH Macerac
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_2_id, quality_param_id, '5.4 pH', true, true, 1, '2025-07-15 11:00:00'::timestamptz);
    
    -- Production Phase 1.3: FILTRACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'FILTRACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-07-15 16:00:00'::timestamptz,
        '2025-07-15 18:00:00'::timestamptz
    ) RETURNING id INTO phase1_3_id;
    
    -- Quality parameters para FILTRACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_3_id, quality_param_id, '15 NTU', true, true, 1, '2025-07-15 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_3_id, quality_param_id, '78 °C', true, true, 1, '2025-07-15 16:00:00'::timestamptz);
    
    -- Production Phase 1.4: COCCION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'COCCION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-07-15 18:00:00'::timestamptz,
        '2025-07-15 20:30:00'::timestamptz
    ) RETURNING id INTO phase1_4_id;
    
    -- Quality parameters para COCCION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_4_id, quality_param_id, '12.5 °P', true, true, 1, '2025-07-15 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_4_id, quality_param_id, '10 min', true, true, 1, '2025-07-15 18:00:00'::timestamptz);
    
    -- Production Phase 1.5: FERMENTACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'FERMENTACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-07-15 20:30:00'::timestamptz,
        '2025-07-22 20:30:00'::timestamptz
    ) RETURNING id INTO phase1_5_id;
    
    -- Quality parameters para FERMENTACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_5_id, quality_param_id, '19 °C', true, true, 1, '2025-07-15 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_5_id, quality_param_id, '1.010 SG', true, true, 1, '2025-07-15 20:30:00'::timestamptz);
    
    -- Production Phase 1.6: MADURACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'MADURACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-07-22 20:30:00'::timestamptz,
        '2025-08-01 20:30:00'::timestamptz
    ) RETURNING id INTO phase1_6_id;
    
    -- Quality parameters para MADURACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_6_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-07-22 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_6_id, quality_param_id, '5 EBC', true, true, 1, '2025-07-22 20:30:00'::timestamptz);
    
    -- Production Phase 1.7: GASIFICACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'GASIFICACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-01 20:30:00'::timestamptz,
        '2025-08-02 00:30:00'::timestamptz
    ) RETURNING id INTO phase1_7_id;
    
    -- Quality parameters para GASIFICACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_7_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-08-01 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_7_id, quality_param_id, '18 psi', true, true, 1, '2025-08-01 20:30:00'::timestamptz);
    
    -- Production Phase 1.8: ENVASADO
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'ENVASADO';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch1_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-02 00:30:00'::timestamptz,
        '2025-08-02 08:30:00'::timestamptz
    ) RETURNING id INTO phase1_8_id;
    
    -- Quality parameters para ENVASADO
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_8_id, quality_param_id, 'OK', true, true, 1, '2025-08-02 00:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase1_8_id, quality_param_id, 'OK', true, true, 1, '2025-08-02 00:30:00'::timestamptz);
    
    -- ============================================================================
    -- ORDEN 2: Agosto 10, 2025 (CON DESPERDICIO - 95% eficiencia)
    -- ============================================================================
    
    -- Batch 2
    INSERT INTO batches (id, code, id_packaging, status, quantity, creation_date, planned_date, start_date, completed_date, estimated_completed_date)
    VALUES (
        nextval('batches_seq'),
        NULL,
        packaging2_id,
        'COMPLETADO',
        (SELECT CASE 
            WHEN pkg.quantity = 0.33 THEN FLOOR(p.standard_quantity / 0.33)::integer
            WHEN pkg.quantity = 20.0 THEN FLOOR(p.standard_quantity / 20.0)::integer
            ELSE 1000
         END
         FROM products p, packagings pkg
         WHERE p.id = product2_id AND pkg.id = packaging2_id),
        '2025-08-10 09:00:00'::timestamptz,
        '2025-08-10 09:00:00'::timestamptz,
        '2025-08-10 09:00:00'::timestamptz,
        '2025-09-09 09:00:00'::timestamptz,
        '2025-09-09 09:00:00'::timestamptz
    ) RETURNING id INTO batch2_id;
    
    UPDATE batches SET code = 'LOT-' || batch2_id WHERE id = batch2_id;
    
    -- Production Order 2
    INSERT INTO production_orders (id, id_batch, id_product, status, quantity, creation_date, validation_date, created_by_user_id, approved_by_user_id)
    VALUES (
        nextval('production_orders_seq'),
        batch2_id,
        product2_id,
        'APROBADA',
        (SELECT standard_quantity FROM products WHERE id = product2_id),
        '2025-08-10 09:00:00'::timestamptz,
        '2025-08-10 09:00:00'::timestamptz,
        supervisor_prod_id,
        supervisor_prod_id
    ) RETURNING id INTO order2_id;
    
    SELECT po.quantity / p.standard_quantity INTO multiplier
    FROM production_orders po, products p
    WHERE po.id = order2_id AND p.id = product2_id;
    
    efficiency_factor := 0.95; -- 5% de desperdicio
    
    -- Production Phase 2.1: MOLIENDA (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'MOLIENDA';
    
    current_input := standard_input * multiplier;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-10 09:00:00'::timestamptz,
        '2025-08-10 11:00:00'::timestamptz
    ) RETURNING id INTO phase2_1_id;
    
    -- Quality parameters para MOLIENDA
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_1_id, quality_param_id, '350 μm', true, true, 1, '2025-08-10 09:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_1_id, quality_param_id, '4.5 %', true, true, 1, '2025-08-10 09:00:00'::timestamptz);
    
    -- Production Phase 2.2: MACERACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'MACERACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-10 11:00:00'::timestamptz,
        '2025-08-10 16:00:00'::timestamptz
    ) RETURNING id INTO phase2_2_id;
    
    -- Quality parameters para MACERACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_2_id, quality_param_id, '66 °C', true, true, 1, '2025-08-10 11:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_2_id, quality_param_id, '5.4 pH', true, true, 1, '2025-08-10 11:00:00'::timestamptz);
    
    -- Production Phase 2.3: FILTRACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'FILTRACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-10 16:00:00'::timestamptz,
        '2025-08-10 18:00:00'::timestamptz
    ) RETURNING id INTO phase2_3_id;
    
    -- Quality parameters para FILTRACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_3_id, quality_param_id, '15 NTU', true, true, 1, '2025-08-10 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_3_id, quality_param_id, '78 °C', true, true, 1, '2025-08-10 16:00:00'::timestamptz);
    
    -- Production Phase 2.4: COCCION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'COCCION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-10 18:00:00'::timestamptz,
        '2025-08-10 20:30:00'::timestamptz
    ) RETURNING id INTO phase2_4_id;
    
    -- Quality parameters para COCCION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_4_id, quality_param_id, '12.5 °P', true, true, 1, '2025-08-10 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_4_id, quality_param_id, '10 min', true, true, 1, '2025-08-10 18:00:00'::timestamptz);
    
    -- Production Phase 2.5: FERMENTACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'FERMENTACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-10 20:30:00'::timestamptz,
        '2025-08-17 20:30:00'::timestamptz
    ) RETURNING id INTO phase2_5_id;
    
    -- Quality parameters para FERMENTACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_5_id, quality_param_id, '19 °C', true, true, 1, '2025-08-10 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_5_id, quality_param_id, '1.010 SG', true, true, 1, '2025-08-10 20:30:00'::timestamptz);
    
    -- Production Phase 2.6: MADURACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'MADURACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-17 20:30:00'::timestamptz,
        '2025-08-27 20:30:00'::timestamptz
    ) RETURNING id INTO phase2_6_id;
    
    -- Quality parameters para MADURACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_6_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-08-17 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_6_id, quality_param_id, '5 EBC', true, true, 1, '2025-08-17 20:30:00'::timestamptz);
    
    -- Production Phase 2.7: GASIFICACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'GASIFICACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-27 20:30:00'::timestamptz,
        '2025-08-27 23:30:00'::timestamptz
    ) RETURNING id INTO phase2_7_id;
    
    -- Quality parameters para GASIFICACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_7_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-08-27 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_7_id, quality_param_id, '18 psi', true, true, 1, '2025-08-27 20:30:00'::timestamptz);
    
    -- Production Phase 2.8: ENVASADO (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'ENVASADO';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch2_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-08-27 23:30:00'::timestamptz,
        '2025-08-28 07:30:00'::timestamptz
    ) RETURNING id INTO phase2_8_id;
    
    -- Quality parameters para ENVASADO
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_8_id, quality_param_id, 'OK', true, true, 1, '2025-08-27 23:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase2_8_id, quality_param_id, 'OK', true, true, 1, '2025-08-27 23:30:00'::timestamptz);
    
    -- ============================================================================
    -- ORDEN 3: Septiembre 20, 2025 (SIN DESPERDICIO)
    -- ============================================================================
    
    -- Batch 3
    INSERT INTO batches (id, code, id_packaging, status, quantity, creation_date, planned_date, start_date, completed_date, estimated_completed_date)
    VALUES (
        nextval('batches_seq'),
        NULL,
        packaging3_id,
        'COMPLETADO',
        (SELECT CASE 
            WHEN pkg.quantity = 0.33 THEN FLOOR(p.standard_quantity / 0.33)::integer
            WHEN pkg.quantity = 20.0 THEN FLOOR(p.standard_quantity / 20.0)::integer
            ELSE 1000
         END
         FROM products p, packagings pkg
         WHERE p.id = product3_id AND pkg.id = packaging3_id),
        '2025-09-20 09:00:00'::timestamptz,
        '2025-09-20 09:00:00'::timestamptz,
        '2025-09-20 09:00:00'::timestamptz,
        '2025-10-20 09:00:00'::timestamptz,
        '2025-10-20 09:00:00'::timestamptz
    ) RETURNING id INTO batch3_id;
    
    UPDATE batches SET code = 'LOT-' || batch3_id WHERE id = batch3_id;
    
    -- Production Order 3
    INSERT INTO production_orders (id, id_batch, id_product, status, quantity, creation_date, validation_date, created_by_user_id, approved_by_user_id)
    VALUES (
        nextval('production_orders_seq'),
        batch3_id,
        product3_id,
        'APROBADA',
        (SELECT standard_quantity FROM products WHERE id = product3_id),
        '2025-09-20 09:00:00'::timestamptz,
        '2025-09-20 09:00:00'::timestamptz,
        supervisor_prod_id,
        supervisor_prod_id
    ) RETURNING id INTO order3_id;
    
    SELECT po.quantity / p.standard_quantity INTO multiplier
    FROM production_orders po, products p
    WHERE po.id = order3_id AND p.id = product3_id;
    
    efficiency_factor := 1.0; -- Sin desperdicio
    
    -- Production Phases para Batch 3 (similar a Batch 1, pero para product3_id)
    -- Por brevedad, solo muestro la estructura. Se repetiría el mismo patrón para todas las fases.
    -- Nota: Si el producto 3 tiene DESALCOHOLIZACION, se agregaría esa fase también.
    
    -- Production Phase 3.1: MOLIENDA
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'MOLIENDA';
    
    current_input := standard_input * multiplier;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-09-20 09:00:00'::timestamptz,
        '2025-09-20 11:00:00'::timestamptz
    ) RETURNING id INTO phase3_1_id;
    
    -- Quality parameters para MOLIENDA
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_1_id, quality_param_id, '350 μm', true, true, 1, '2025-09-20 09:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_1_id, quality_param_id, '4.5 %', true, true, 1, '2025-09-20 09:00:00'::timestamptz);
    
    -- Production Phase 3.2: MACERACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'MACERACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-09-20 11:00:00'::timestamptz,
        '2025-09-20 16:00:00'::timestamptz
    ) RETURNING id INTO phase3_2_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_2_id, quality_param_id, '66 °C', true, true, 1, '2025-09-20 11:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_2_id, quality_param_id, '5.4 pH', true, true, 1, '2025-09-20 11:00:00'::timestamptz);
    
    -- Production Phase 3.3: FILTRACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'FILTRACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-09-20 16:00:00'::timestamptz,
        '2025-09-20 18:00:00'::timestamptz
    ) RETURNING id INTO phase3_3_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_3_id, quality_param_id, '15 NTU', true, true, 1, '2025-09-20 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_3_id, quality_param_id, '78 °C', true, true, 1, '2025-09-20 16:00:00'::timestamptz);
    
    -- Production Phase 3.4: COCCION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'COCCION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-09-20 18:00:00'::timestamptz,
        '2025-09-20 20:30:00'::timestamptz
    ) RETURNING id INTO phase3_4_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_4_id, quality_param_id, '12.5 °P', true, true, 1, '2025-09-20 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_4_id, quality_param_id, '10 min', true, true, 1, '2025-09-20 18:00:00'::timestamptz);
    
    -- Production Phase 3.5: FERMENTACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'FERMENTACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-09-20 20:30:00'::timestamptz,
        '2025-09-27 20:30:00'::timestamptz
    ) RETURNING id INTO phase3_5_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_5_id, quality_param_id, '19 °C', true, true, 1, '2025-09-20 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_5_id, quality_param_id, '1.010 SG', true, true, 1, '2025-09-20 20:30:00'::timestamptz);
    
    -- Verificar si el producto tiene DESALCOHOLIZACION
    IF EXISTS (SELECT 1 FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION') THEN
        -- Production Phase 3.6: DESALCOHOLIZACION
        SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
        FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION';
        
        current_input := current_output;
        current_output := current_input * (standard_output / standard_input) * efficiency_factor;
        
        SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'DESALCOHOLIZACION' ORDER BY id LIMIT 1;
        
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'DESALCOHOLIZACION', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-09-27 20:30:00'::timestamptz,
            '2025-09-29 20:30:00'::timestamptz
        ) RETURNING id INTO phase3_6_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'DESALCOHOLIZACION' AND name = 'Alcohol Final';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_6_id, quality_param_id, '0.4 % ABV', true, true, 1, '2025-09-27 20:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'DESALCOHOLIZACION' AND name = 'Temp Columna';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_6_id, quality_param_id, '65 °C', true, true, 1, '2025-09-27 20:30:00'::timestamptz);
    END IF;
    
    -- Production Phase 3.6 o 3.7: MADURACION (depende si hay DESALCOHOLIZACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'MADURACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    IF EXISTS (SELECT 1 FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION') THEN
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-09-29 20:30:00'::timestamptz,
            '2025-10-09 20:30:00'::timestamptz
        ) RETURNING id INTO phase3_7_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_7_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-09-29 20:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_7_id, quality_param_id, '5 EBC', true, true, 1, '2025-09-29 20:30:00'::timestamptz);
    ELSE
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-09-27 20:30:00'::timestamptz,
            '2025-10-07 20:30:00'::timestamptz
        ) RETURNING id INTO phase3_6_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_6_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-09-27 20:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_6_id, quality_param_id, '5 EBC', true, true, 1, '2025-09-27 20:30:00'::timestamptz);
    END IF;
    
    -- Production Phase 3.7 o 3.8: GASIFICACION (depende si hay DESALCOHOLIZACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'GASIFICACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    IF EXISTS (SELECT 1 FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION') THEN
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-10-09 20:30:00'::timestamptz,
            '2025-10-10 00:30:00'::timestamptz
        ) RETURNING id INTO phase3_8_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_8_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-10-09 20:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_8_id, quality_param_id, '18 psi', true, true, 1, '2025-10-09 20:30:00'::timestamptz);
    ELSE
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-10-07 20:30:00'::timestamptz,
            '2025-10-07 23:30:00'::timestamptz
        ) RETURNING id INTO phase3_7_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_7_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-10-07 20:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_7_id, quality_param_id, '18 psi', true, true, 1, '2025-10-07 20:30:00'::timestamptz);
    END IF;
    
    -- Production Phase 3.8 o 3.9: ENVASADO (depende si hay DESALCOHOLIZACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'ENVASADO';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    IF EXISTS (SELECT 1 FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION') THEN
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-10-10 00:30:00'::timestamptz,
            '2025-10-10 08:30:00'::timestamptz
        ) RETURNING id INTO phase3_9_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_9_id, quality_param_id, 'OK', true, true, 1, '2025-10-10 00:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_9_id, quality_param_id, 'OK', true, true, 1, '2025-10-10 00:30:00'::timestamptz);
    ELSE
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 1000.0) / 1000.0,
            ROUND(current_output * 1000.0) / 1000.0,
            standard_input * multiplier,
            standard_output * multiplier,
            output_unit_val,
            '2025-10-07 23:30:00'::timestamptz,
            '2025-10-08 07:30:00'::timestamptz
        ) RETURNING id INTO phase3_8_id;
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_8_id, quality_param_id, 'OK', true, true, 1, '2025-10-07 23:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_8_id, quality_param_id, 'OK', true, true, 1, '2025-10-07 23:30:00'::timestamptz);
    END IF;
    
    -- ============================================================================
    -- ORDEN 4: Octubre 5, 2025 (CON DESPERDICIO - 95% eficiencia)
    -- ============================================================================
    
    -- Batch 4
    INSERT INTO batches (id, code, id_packaging, status, quantity, creation_date, planned_date, start_date, completed_date, estimated_completed_date)
    VALUES (
        nextval('batches_seq'),
        NULL,
        packaging1_id,
        'COMPLETADO',
        (SELECT CASE 
            WHEN pkg.quantity = 0.33 THEN FLOOR(p.standard_quantity / 0.33)::integer
            WHEN pkg.quantity = 20.0 THEN FLOOR(p.standard_quantity / 20.0)::integer
            ELSE 1000
         END
         FROM products p, packagings pkg
         WHERE p.id = product1_id AND pkg.id = packaging1_id),
        '2025-10-05 09:00:00'::timestamptz,
        '2025-10-05 09:00:00'::timestamptz,
        '2025-10-05 09:00:00'::timestamptz,
        '2025-11-04 09:00:00'::timestamptz,
        '2025-11-04 09:00:00'::timestamptz
    ) RETURNING id INTO batch4_id;
    
    UPDATE batches SET code = 'LOT-' || batch4_id WHERE id = batch4_id;
    
    -- Production Order 4
    INSERT INTO production_orders (id, id_batch, id_product, status, quantity, creation_date, validation_date, created_by_user_id, approved_by_user_id)
    VALUES (
        nextval('production_orders_seq'),
        batch4_id,
        product1_id,
        'APROBADA',
        (SELECT standard_quantity FROM products WHERE id = product1_id),
        '2025-10-05 09:00:00'::timestamptz,
        '2025-10-05 09:00:00'::timestamptz,
        supervisor_prod_id,
        supervisor_prod_id
    ) RETURNING id INTO order4_id;
    
    SELECT po.quantity / p.standard_quantity INTO multiplier
    FROM production_orders po, products p
    WHERE po.id = order4_id AND p.id = product1_id;
    
    efficiency_factor := 0.95; -- 5% de desperdicio
    
    -- Production Phase 4.1: MOLIENDA (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'MOLIENDA';
    
    current_input := standard_input * multiplier;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-05 09:00:00'::timestamptz,
        '2025-10-05 11:00:00'::timestamptz
    ) RETURNING id INTO phase4_1_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_1_id, quality_param_id, '350 μm', true, true, 1, '2025-10-05 09:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_1_id, quality_param_id, '4.5 %', true, true, 1, '2025-10-05 09:00:00'::timestamptz);
    
    -- Production Phase 4.2: MACERACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'MACERACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-05 11:00:00'::timestamptz,
        '2025-10-05 16:00:00'::timestamptz
    ) RETURNING id INTO phase4_2_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_2_id, quality_param_id, '66 °C', true, true, 1, '2025-10-05 11:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_2_id, quality_param_id, '5.4 pH', true, true, 1, '2025-10-05 11:00:00'::timestamptz);
    
    -- Production Phase 4.3: FILTRACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'FILTRACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-05 16:00:00'::timestamptz,
        '2025-10-05 18:00:00'::timestamptz
    ) RETURNING id INTO phase4_3_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_3_id, quality_param_id, '15 NTU', true, true, 1, '2025-10-05 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_3_id, quality_param_id, '78 °C', true, true, 1, '2025-10-05 16:00:00'::timestamptz);
    
    -- Production Phase 4.4: COCCION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'COCCION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-05 18:00:00'::timestamptz,
        '2025-10-05 20:30:00'::timestamptz
    ) RETURNING id INTO phase4_4_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_4_id, quality_param_id, '12.5 °P', true, true, 1, '2025-10-05 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_4_id, quality_param_id, '10 min', true, true, 1, '2025-10-05 18:00:00'::timestamptz);
    
    -- Production Phase 4.5: FERMENTACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'FERMENTACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-05 20:30:00'::timestamptz,
        '2025-10-12 20:30:00'::timestamptz
    ) RETURNING id INTO phase4_5_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_5_id, quality_param_id, '19 °C', true, true, 1, '2025-10-05 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_5_id, quality_param_id, '1.010 SG', true, true, 1, '2025-10-05 20:30:00'::timestamptz);
    
    -- Production Phase 4.6: MADURACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'MADURACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-12 20:30:00'::timestamptz,
        '2025-10-22 20:30:00'::timestamptz
    ) RETURNING id INTO phase4_6_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_6_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-10-12 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_6_id, quality_param_id, '5 EBC', true, true, 1, '2025-10-12 20:30:00'::timestamptz);
    
    -- Production Phase 4.7: GASIFICACION (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'GASIFICACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-22 20:30:00'::timestamptz,
        '2025-10-22 23:30:00'::timestamptz
    ) RETURNING id INTO phase4_7_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_7_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-10-22 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_7_id, quality_param_id, '18 psi', true, true, 1, '2025-10-22 20:30:00'::timestamptz);
    
    -- Production Phase 4.8: ENVASADO (con desperdicio)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product1_id AND phase = 'ENVASADO';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch4_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-10-22 23:30:00'::timestamptz,
        '2025-10-23 07:30:00'::timestamptz
    ) RETURNING id INTO phase4_8_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_8_id, quality_param_id, 'OK', true, true, 1, '2025-10-22 23:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase4_8_id, quality_param_id, 'OK', true, true, 1, '2025-10-22 23:30:00'::timestamptz);
    
    -- ============================================================================
    -- ORDEN 5: Noviembre 10, 2025 (SIN DESPERDICIO)
    -- ============================================================================
    
    -- Batch 5
    INSERT INTO batches (id, code, id_packaging, status, quantity, creation_date, planned_date, start_date, completed_date, estimated_completed_date)
    VALUES (
        nextval('batches_seq'),
        NULL,
        packaging2_id,
        'COMPLETADO',
        (SELECT CASE 
            WHEN pkg.quantity = 0.33 THEN FLOOR(p.standard_quantity / 0.33)::integer
            WHEN pkg.quantity = 20.0 THEN FLOOR(p.standard_quantity / 20.0)::integer
            ELSE 1000
         END
         FROM products p, packagings pkg
         WHERE p.id = product2_id AND pkg.id = packaging2_id),
        '2025-11-10 09:00:00'::timestamptz,
        '2025-11-10 09:00:00'::timestamptz,
        '2025-11-10 09:00:00'::timestamptz,
        '2025-12-10 09:00:00'::timestamptz,
        '2025-12-10 09:00:00'::timestamptz
    ) RETURNING id INTO batch5_id;
    
    UPDATE batches SET code = 'LOT-' || batch5_id WHERE id = batch5_id;
    
    -- Production Order 5
    INSERT INTO production_orders (id, id_batch, id_product, status, quantity, creation_date, validation_date, created_by_user_id, approved_by_user_id)
    VALUES (
        nextval('production_orders_seq'),
        batch5_id,
        product2_id,
        'APROBADA',
        (SELECT standard_quantity FROM products WHERE id = product2_id),
        '2025-11-10 09:00:00'::timestamptz,
        '2025-11-10 09:00:00'::timestamptz,
        supervisor_prod_id,
        supervisor_prod_id
    ) RETURNING id INTO order5_id;
    
    SELECT po.quantity / p.standard_quantity INTO multiplier
    FROM production_orders po, products p
    WHERE po.id = order5_id AND p.id = product2_id;
    
    efficiency_factor := 1.0; -- Sin desperdicio
    
    -- Production Phase 5.1: MOLIENDA
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'MOLIENDA';
    
    current_input := standard_input * multiplier;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-10 09:00:00'::timestamptz,
        '2025-11-10 11:00:00'::timestamptz
    ) RETURNING id INTO phase5_1_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_1_id, quality_param_id, '350 μm', true, true, 1, '2025-11-10 09:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_1_id, quality_param_id, '4.5 %', true, true, 1, '2025-11-10 09:00:00'::timestamptz);
    
    -- Production Phase 5.2: MACERACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'MACERACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-10 11:00:00'::timestamptz,
        '2025-11-10 16:00:00'::timestamptz
    ) RETURNING id INTO phase5_2_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_2_id, quality_param_id, '66 °C', true, true, 1, '2025-11-10 11:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_2_id, quality_param_id, '5.4 pH', true, true, 1, '2025-11-10 11:00:00'::timestamptz);
    
    -- Production Phase 5.3: FILTRACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'FILTRACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-10 16:00:00'::timestamptz,
        '2025-11-10 18:00:00'::timestamptz
    ) RETURNING id INTO phase5_3_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_3_id, quality_param_id, '15 NTU', true, true, 1, '2025-11-10 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_3_id, quality_param_id, '78 °C', true, true, 1, '2025-11-10 16:00:00'::timestamptz);
    
    -- Production Phase 5.4: COCCION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'COCCION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-10 18:00:00'::timestamptz,
        '2025-11-10 20:30:00'::timestamptz
    ) RETURNING id INTO phase5_4_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_4_id, quality_param_id, '12.5 °P', true, true, 1, '2025-11-10 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_4_id, quality_param_id, '10 min', true, true, 1, '2025-11-10 18:00:00'::timestamptz);
    
    -- Production Phase 5.5: FERMENTACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'FERMENTACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-10 20:30:00'::timestamptz,
        '2025-11-17 20:30:00'::timestamptz
    ) RETURNING id INTO phase5_5_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_5_id, quality_param_id, '19 °C', true, true, 1, '2025-11-10 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_5_id, quality_param_id, '1.010 SG', true, true, 1, '2025-11-10 20:30:00'::timestamptz);
    
    -- Production Phase 5.6: MADURACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'MADURACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-17 20:30:00'::timestamptz,
        '2025-11-27 20:30:00'::timestamptz
    ) RETURNING id INTO phase5_6_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_6_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-11-17 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_6_id, quality_param_id, '5 EBC', true, true, 1, '2025-11-17 20:30:00'::timestamptz);
    
    -- Production Phase 5.7: GASIFICACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'GASIFICACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-27 20:30:00'::timestamptz,
        '2025-11-27 23:30:00'::timestamptz
    ) RETURNING id INTO phase5_7_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_7_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-11-27 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_7_id, quality_param_id, '18 psi', true, true, 1, '2025-11-27 20:30:00'::timestamptz);
    
    -- Production Phase 5.8: ENVASADO
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product2_id AND phase = 'ENVASADO';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch5_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input * multiplier,
        standard_output * multiplier,
        output_unit_val,
        '2025-11-27 23:30:00'::timestamptz,
        '2025-11-28 07:30:00'::timestamptz
    ) RETURNING id INTO phase5_8_id;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_8_id, quality_param_id, 'OK', true, true, 1, '2025-11-27 23:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase5_8_id, quality_param_id, 'OK', true, true, 1, '2025-11-27 23:30:00'::timestamptz);
    
    RAISE NOTICE 'Datos de ejemplo de órdenes y lotes cargados exitosamente: 5 órdenes creadas';
    
    -- Actualizar secuencias al máximo ID usado + 1 para que las próximas inserciones funcionen correctamente
    PERFORM setval('batches_seq', COALESCE((SELECT MAX(id) FROM batches), 1), true);
    PERFORM setval('production_orders_seq', COALESCE((SELECT MAX(id) FROM production_orders), 1), true);
    PERFORM setval('production_phases_seq', COALESCE((SELECT MAX(id) FROM production_phases), 1), true);
    PERFORM setval('production_phases_qualities_seq', COALESCE((SELECT MAX(id) FROM production_phases_qualities), 1), true);
    
    RAISE NOTICE 'Secuencias actualizadas correctamente';
    
END $$;
