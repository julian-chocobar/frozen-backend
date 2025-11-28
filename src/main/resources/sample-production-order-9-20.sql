-- Script SQL para cargar datos de ejemplo de orden de producción 3 (Septiembre 20, 2025)
-- Este script asume que ya existen: productos, packagings, usuarios, sectores, quality_parameters y recipes
-- Orden 3: Septiembre 20, 2025 (SIN DESPERDICIO - eficiencia 100%)
-- IMPORTANTE: Si el producto tiene DESALCOHOLIZACION, va DESPUÉS de GASIFICACION y ANTES de ENVASADO

DO $$
DECLARE
    -- Variables para IDs
    batch3_id BIGINT;
    order3_id BIGINT;
    phase3_1_id BIGINT; -- MOLIENDA
    phase3_2_id BIGINT; -- MACERACION
    phase3_3_id BIGINT; -- FILTRACION
    phase3_4_id BIGINT; -- COCCION
    phase3_5_id BIGINT; -- FERMENTACION
    phase3_6_id BIGINT; -- MADURACION
    phase3_7_id BIGINT; -- GASIFICACION
    phase3_8_id BIGINT; -- DESALCOHOLIZACION (opcional)
    phase3_9_id BIGINT; -- ENVASADO
    
    -- IDs de referencias
    product3_id BIGINT;
    packaging3_id BIGINT;
    supervisor_prod_id BIGINT;
    
    -- Variables para cálculos
    multiplier NUMERIC;
    standard_input NUMERIC;
    standard_output NUMERIC;
    current_input NUMERIC;
    current_output NUMERIC;
    efficiency_factor NUMERIC;
    sector_id BIGINT;
    quality_param_id BIGINT;
    phase_order_val INTEGER;
    output_unit_val VARCHAR;
    has_dealcoholization BOOLEAN;
    
    -- Variables para materiales
    recipe_material_id BIGINT;
    recipe_quantity NUMERIC;
    
    -- Variables para totales de ingredientes
    total_ingredients NUMERIC;
BEGIN
    -- Obtener IDs de usuarios
    SELECT id INTO supervisor_prod_id FROM users WHERE username = 'supervisorproduccion';
    
    -- Obtener producto
    SELECT id INTO product3_id FROM products WHERE is_active = true ORDER BY id LIMIT 1;
    
    -- Verificar si el producto tiene DESALCOHOLIZACION
    SELECT EXISTS (SELECT 1 FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION') INTO has_dealcoholization;
    
    -- Obtener packaging compatible
    SELECT pkg.id INTO packaging3_id
    FROM packagings pkg
    INNER JOIN products p ON p.unit_measurement = pkg.unit_measurement
    WHERE p.id = product3_id AND pkg.is_active = true ORDER BY pkg.id LIMIT 1;
    
    -- Batch 1
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
        CASE WHEN has_dealcoholization THEN '2025-10-10 09:00:00'::timestamptz ELSE '2025-10-08 09:00:00'::timestamptz END,
        CASE WHEN has_dealcoholization THEN '2025-10-10 09:00:00'::timestamptz ELSE '2025-10-08 09:00:00'::timestamptz END
    ) RETURNING id INTO batch3_id;
    
    UPDATE batches SET code = 'LOT-' || batch3_id WHERE id = batch3_id;
    
    -- Production Order 1
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
    
    -- Calcular multiplicador
    SELECT po.quantity / p.standard_quantity INTO multiplier
    FROM production_orders po, products p
    WHERE po.id = order3_id AND p.id = product3_id;
    
    -- SIN DESPERDICIO: 100% eficiencia
    efficiency_factor := 1.0;
    
    -- Production Phase 3.1: MOLIENDA (primera fase: input = 0.0)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'MOLIENDA';
    
    -- Calcular total de ingredientes para MOLIENDA
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'MOLIENDA';
    
    -- Input siempre es 0.0 para MOLIENDA (primera fase)
    current_input := 0.0;
    -- Output debe ser <= input (0.0) + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        standard_output * multiplier * efficiency_factor,
        total_ingredients * multiplier * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier - current_output) * 100.0) / 100.0,
        '2025-09-20 09:00:00'::timestamptz,
        '2025-09-20 11:00:00'::timestamptz
    ) RETURNING id INTO phase3_1_id;
    
    -- Production Materials para MOLIENDA
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'MOLIENDA'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_1_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-09-20 09:00:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para MOLIENDA
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_1_id, quality_param_id, '350 μm', true, true, 1, '2025-09-20 09:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_1_id, quality_param_id, '4.5 %', true, true, 1, '2025-09-20 09:00:00'::timestamptz);
    
    -- Production Phase 3.2: MACERACION (input = output de MOLIENDA)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'MACERACION';
    
    -- Calcular total de ingredientes para MACERACION
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'MACERACION';
    
    -- Input es el output de la fase anterior (MOLIENDA)
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        '2025-09-20 11:00:00'::timestamptz,
        '2025-09-20 16:00:00'::timestamptz
    ) RETURNING id INTO phase3_2_id;
    
    -- Production Materials para MACERACION
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'MACERACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_2_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-09-20 11:00:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para MACERACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_2_id, quality_param_id, '66 °C', true, true, 1, '2025-09-20 11:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_2_id, quality_param_id, '5.4 pH', true, true, 1, '2025-09-20 11:00:00'::timestamptz);
    
    -- Production Phase 3.3: FILTRACION (input = output de MACERACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'FILTRACION';
    
    -- Calcular total de ingredientes para FILTRACION
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'FILTRACION';
    
    -- Input es el output de la fase anterior (MACERACION)
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        '2025-09-20 16:00:00'::timestamptz,
        '2025-09-20 18:00:00'::timestamptz
    ) RETURNING id INTO phase3_3_id;
    
    -- Production Materials para FILTRACION
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'FILTRACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_3_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-09-20 16:00:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para FILTRACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_3_id, quality_param_id, '15 NTU', true, true, 1, '2025-09-20 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_3_id, quality_param_id, '78 °C', true, true, 1, '2025-09-20 16:00:00'::timestamptz);
    
    -- Production Phase 3.4: COCCION (input = output de FILTRACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'COCCION';
    
    -- Calcular total de ingredientes para COCCION
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'COCCION';
    
    -- Input es el output de la fase anterior (FILTRACION)
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        '2025-09-20 18:00:00'::timestamptz,
        '2025-09-20 20:30:00'::timestamptz
    ) RETURNING id INTO phase3_4_id;
    
    -- Production Materials para COCCION
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'COCCION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_4_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-09-20 18:00:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para COCCION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_4_id, quality_param_id, '12.5 °P', true, true, 1, '2025-09-20 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_4_id, quality_param_id, '10 min', true, true, 1, '2025-09-20 18:00:00'::timestamptz);
    
    -- Production Phase 3.5: FERMENTACION (input = output de COCCION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'FERMENTACION';
    
    -- Calcular total de ingredientes para FERMENTACION
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'FERMENTACION';
    
    -- Input es el output de la fase anterior (COCCION)
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        '2025-09-20 20:30:00'::timestamptz,
        '2025-09-27 20:30:00'::timestamptz
    ) RETURNING id INTO phase3_5_id;
    
    -- Production Materials para FERMENTACION
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'FERMENTACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_5_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-09-20 20:30:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para FERMENTACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_5_id, quality_param_id, '19 °C', true, true, 1, '2025-09-20 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_5_id, quality_param_id, '1.010 SG', true, true, 1, '2025-09-20 20:30:00'::timestamptz);
    
    -- Production Phase 3.6: MADURACION (input = output de FERMENTACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'MADURACION';
    
    -- Calcular total de ingredientes para MADURACION
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'MADURACION';
    
    -- Input es el output de la fase anterior (FERMENTACION)
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        '2025-09-27 20:30:00'::timestamptz,
        '2025-10-07 20:30:00'::timestamptz
    ) RETURNING id INTO phase3_6_id;
    
    -- Production Materials para MADURACION
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'MADURACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_6_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-09-27 20:30:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para MADURACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_6_id, quality_param_id, '0.08 ppm', true, true, 1, '2025-09-27 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_6_id, quality_param_id, '5 EBC', true, true, 1, '2025-09-27 20:30:00'::timestamptz);
    
    -- Production Phase 3.7: GASIFICACION (input = output de MADURACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'GASIFICACION';
    
    -- Calcular total de ingredientes para GASIFICACION
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'GASIFICACION';
    
    -- Input es el output de la fase anterior (MADURACION)
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        '2025-10-07 20:30:00'::timestamptz,
        '2025-10-08 00:30:00'::timestamptz
    ) RETURNING id INTO phase3_7_id;
    
    -- Production Materials para GASIFICACION
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'GASIFICACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_7_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            '2025-10-07 20:30:00'::timestamptz
        );
    END LOOP;
    
    -- Quality parameters para GASIFICACION
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_7_id, quality_param_id, '2.4 vol CO2', true, true, 1, '2025-10-07 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_7_id, quality_param_id, '18 psi', true, true, 1, '2025-10-07 20:30:00'::timestamptz);
    
    -- Production Phase 3.8: DESALCOHOLIZACION (si aplica) (input = output de GASIFICACION)
    IF has_dealcoholization THEN
        SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
        FROM product_phases WHERE id_product = product3_id AND phase = 'DESALCOHOLIZACION';
        
        -- Calcular total de ingredientes para DESALCOHOLIZACION
        SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'DESALCOHOLIZACION';
        
        -- Input es el output de la fase anterior (GASIFICACION)
        current_input := current_output;
        -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
        current_output := LEAST(
            current_input * (standard_output / standard_input) * efficiency_factor,
            (current_input + total_ingredients * multiplier) * efficiency_factor
        );
        
        SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'DESALCOHOLIZACION' ORDER BY id LIMIT 1;
        
        INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
        VALUES (
            nextval('production_phases_seq'),
            batch3_id, sector_id, 'DESALCOHOLIZACION', phase_order_val, 'COMPLETADA',
            ROUND(current_input * 100.0) / 100.0,
            ROUND(current_output * 100.0) / 100.0,
            ROUND(standard_input * multiplier * 100.0) / 100.0,
            ROUND(standard_output * multiplier * 100.0) / 100.0,
            output_unit_val,
            ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
            '2025-10-08 00:30:00'::timestamptz,
            '2025-10-09 00:30:00'::timestamptz
        ) RETURNING id INTO phase3_8_id;
        
        -- Production Materials para DESALCOHOLIZACION
        FOR recipe_material_id, recipe_quantity IN 
            SELECT r.id_material, r.quantity
            FROM recipes r
            INNER JOIN product_phases pp ON r.id_product_phase = pp.id
            WHERE pp.id_product = product3_id AND pp.phase = 'DESALCOHOLIZACION'
        LOOP
            INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
            VALUES (
                nextval('production_materials_seq'),
                recipe_material_id,
                phase3_8_id,
                ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
                '2025-10-08 00:30:00'::timestamptz
            );
        END LOOP;
        
        -- Quality parameters para DESALCOHOLIZACION
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'DESALCOHOLIZACION' AND name = 'Alcohol Final';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_8_id, quality_param_id, '0.4 % ABV', true, true, 1, '2025-10-08 00:30:00'::timestamptz);
        
        SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'DESALCOHOLIZACION' AND name = 'Temp Columna';
        INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
        VALUES (nextval('production_phases_qualities_seq'), phase3_8_id, quality_param_id, '65 °C', true, true, 1, '2025-10-08 00:30:00'::timestamptz);
    END IF;
    
    -- Production Phase 3.9: ENVASADO (input = output de DESALCOHOLIZACION si existe, sino de GASIFICACION)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product3_id AND phase = 'ENVASADO';
    
    -- Calcular total de ingredientes para ENVASADO
    SELECT COALESCE(SUM(r.quantity), 0.0) INTO total_ingredients
    FROM recipes r
    INNER JOIN product_phases pp ON r.id_product_phase = pp.id
    WHERE pp.id_product = product3_id AND pp.phase = 'ENVASADO';
    
    -- Input es el output de la fase anterior (DESALCOHOLIZACION si existe, sino GASIFICACION)
    -- Ya está en current_output de la fase anterior, así que lo asignamos a current_input
    current_input := current_output;
    -- Output debe ser <= input + ingredientes, sin desperdicio (100% eficiencia)
    current_output := LEAST(
        current_input * (standard_output / standard_input) * efficiency_factor,
        (current_input + total_ingredients * multiplier) * efficiency_factor
    );
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch3_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 100.0) / 100.0,
        ROUND(current_output * 100.0) / 100.0,
        ROUND(standard_input * multiplier * 100.0) / 100.0,
        ROUND(standard_output * multiplier * 100.0) / 100.0,
        output_unit_val,
        ROUND((standard_output * multiplier * (current_input / (standard_input * multiplier)) - current_output) * 100.0) / 100.0,
        CASE WHEN has_dealcoholization THEN '2025-10-09 00:30:00'::timestamptz ELSE '2025-10-08 00:30:00'::timestamptz END,
        CASE WHEN has_dealcoholization THEN '2025-10-09 08:30:00'::timestamptz ELSE '2025-10-08 08:30:00'::timestamptz END
    ) RETURNING id INTO phase3_9_id;
    
    -- Production Materials para ENVASADO
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity
        FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product3_id AND pp.phase = 'ENVASADO'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (
            nextval('production_materials_seq'),
            recipe_material_id,
            phase3_9_id,
            ROUND(recipe_quantity * multiplier * 100.0) / 100.0,
            CASE WHEN has_dealcoholization THEN '2025-10-09 00:30:00'::timestamptz ELSE '2025-10-08 00:30:00'::timestamptz END
        );
    END LOOP;
    
    -- Quality parameters para ENVASADO
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_9_id, quality_param_id, 'OK', true, true, 1, 
            CASE WHEN has_dealcoholization THEN '2025-10-09 00:30:00'::timestamptz ELSE '2025-10-08 00:30:00'::timestamptz END);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase3_9_id, quality_param_id, 'OK', true, true, 1,
            CASE WHEN has_dealcoholization THEN '2025-10-09 00:30:00'::timestamptz ELSE '2025-10-08 00:30:00'::timestamptz END);
    
    RAISE NOTICE 'Orden de producción 3 (Septiembre) cargada exitosamente';
    
    -- Actualizar secuencias
    PERFORM setval('batches_seq', COALESCE((SELECT MAX(id) FROM batches), 1), true);
    PERFORM setval('production_orders_seq', COALESCE((SELECT MAX(id) FROM production_orders), 1), true);
    PERFORM setval('production_phases_seq', COALESCE((SELECT MAX(id) FROM production_phases), 1), true);
    PERFORM setval('production_phases_qualities_seq', COALESCE((SELECT MAX(id) FROM production_phases_qualities), 1), true);
    PERFORM setval('production_materials_seq', COALESCE((SELECT MAX(id) FROM production_materials), 1), true);
    
END $$;
