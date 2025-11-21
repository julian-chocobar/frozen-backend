-- Script SQL para cargar datos de ejemplo de orden de producción - Marzo 15, 2025
-- Producto: Pale Sin Alcohol - Packaging: Barril 20L
-- Eficiencia: 90% (mucho desperdicio) - Output final esperado: ~531 LT (1000L * 0.90^7 por DESALCOHOLIZACION)
-- IMPORTANTE: Incluye fase DESALCOHOLIZACION después de GASIFICACION

DO $$
DECLARE
    -- Variables para IDs
    batch_id BIGINT;
    order_id BIGINT;
    phase_1_id BIGINT; -- MOLIENDA
    phase_2_id BIGINT; -- MACERACION
    phase_3_id BIGINT; -- FILTRACION
    phase_4_id BIGINT; -- COCCION
    phase_5_id BIGINT; -- FERMENTACION
    phase_6_id BIGINT; -- MADURACION
    phase_7_id BIGINT; -- GASIFICACION
    phase_8_id BIGINT; -- DESALCOHOLIZACION
    phase_9_id BIGINT; -- ENVASADO
    
    -- IDs de referencias
    product_id BIGINT;
    packaging_id BIGINT;
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
    
    -- Variables para materiales
    recipe_material_id BIGINT;
    recipe_quantity NUMERIC;
BEGIN
    -- Obtener IDs de usuarios
    SELECT id INTO supervisor_prod_id FROM users WHERE username = 'supervisorproduccion';
    
    -- Obtener producto Pale Sin Alcohol
    SELECT id INTO product_id FROM products WHERE name = 'Pale Sin Alcohol' AND is_active = true LIMIT 1;
    
    -- Obtener packaging de 20L
    SELECT id INTO packaging_id FROM packagings WHERE quantity = 20.0 AND is_active = true LIMIT 1;
    
    -- Batch
    INSERT INTO batches (id, code, id_packaging, status, quantity, creation_date, planned_date, start_date, completed_date, estimated_completed_date)
    VALUES (
        nextval('batches_seq'),
        NULL,
        packaging_id,
        'COMPLETADO',
        FLOOR(1000.0 / 20.0)::integer,
        '2025-03-15 09:00:00'::timestamptz,
        '2025-03-15 09:00:00'::timestamptz,
        '2025-03-15 09:00:00'::timestamptz,
        '2025-04-13 09:00:00'::timestamptz,
        '2025-04-13 09:00:00'::timestamptz
    ) RETURNING id INTO batch_id;
    
    UPDATE batches SET code = 'LOT-' || batch_id WHERE id = batch_id;
    
    -- Production Order
    INSERT INTO production_orders (id, id_batch, id_product, status, quantity, creation_date, validation_date, created_by_user_id, approved_by_user_id)
    VALUES (
        nextval('production_orders_seq'),
        batch_id,
        product_id,
        'APROBADA',
        1000.0,
        '2025-03-15 09:00:00'::timestamptz,
        '2025-03-15 09:00:00'::timestamptz,
        supervisor_prod_id,
        supervisor_prod_id
    ) RETURNING id INTO order_id;
    
    multiplier := 1.0;
    -- MUCHO DESPERDICIO: 90% eficiencia total (0.9825 por fase = 90% acumulado en 7 fases)
    efficiency_factor := 0.9825;
    
    -- MOLIENDA
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'MOLIENDA';
    
    current_input := standard_input * multiplier;
    current_output := standard_output * multiplier * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MOLIENDA' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'MOLIENDA', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-02-15 09:00:00'::timestamptz,
        '2025-02-15 11:00:00'::timestamptz
    ) RETURNING id INTO phase_1_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'MOLIENDA'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_1_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-15 09:00:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Granulometria';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_1_id, quality_param_id, '345 μm', true, true, 1, '2025-03-15 09:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MOLIENDA' AND name = 'Humedad Malta';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_1_id, quality_param_id, '4.3 %', true, true, 1, '2025-03-15 09:00:00'::timestamptz);
    
    -- MACERACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'MACERACION';
    
    current_input := standard_input * multiplier;
    current_output := standard_output * multiplier * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MACERACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'MACERACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-02-15 11:00:00'::timestamptz,
        '2025-02-15 16:00:00'::timestamptz
    ) RETURNING id INTO phase_2_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'MACERACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_2_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-15 11:00:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'Temp Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_2_id, quality_param_id, '65 °C', true, true, 1, '2025-03-15 11:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MACERACION' AND name = 'pH Macerac';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_2_id, quality_param_id, '5.5 pH', true, true, 1, '2025-03-15 11:00:00'::timestamptz);
    
    -- FILTRACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'FILTRACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FILTRACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'FILTRACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-02-15 16:00:00'::timestamptz,
        '2025-02-15 18:00:00'::timestamptz
    ) RETURNING id INTO phase_3_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'FILTRACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_3_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-15 16:00:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Claridad';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_3_id, quality_param_id, '14 NTU', true, true, 1, '2025-03-15 16:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FILTRACION' AND name = 'Temp Filtrado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_3_id, quality_param_id, '77 °C', true, true, 1, '2025-03-15 16:00:00'::timestamptz);
    
    -- COCCION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'COCCION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'COCCION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'COCCION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-02-15 18:00:00'::timestamptz,
        '2025-02-15 20:30:00'::timestamptz
    ) RETURNING id INTO phase_4_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'COCCION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_4_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-15 18:00:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Plato Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_4_id, quality_param_id, '12.0 °P', true, true, 1, '2025-03-15 18:00:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'COCCION' AND name = 'Tiempo Lupulo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_4_id, quality_param_id, '11 min', true, true, 1, '2025-03-15 18:00:00'::timestamptz);
    
    -- FERMENTACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'FERMENTACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'FERMENTACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'FERMENTACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-02-15 20:30:00'::timestamptz,
        '2025-02-22 20:30:00'::timestamptz
    ) RETURNING id INTO phase_5_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'FERMENTACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_5_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-15 20:30:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Temp Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_5_id, quality_param_id, '18 °C', true, true, 1, '2025-03-15 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'FERMENTACION' AND name = 'Densidad Ferm';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_5_id, quality_param_id, '1.011 SG', true, true, 1, '2025-03-15 20:30:00'::timestamptz);
    
    -- MADURACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'MADURACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'MADURACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'MADURACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-02-22 20:30:00'::timestamptz,
        '2025-03-04 20:30:00'::timestamptz
    ) RETURNING id INTO phase_6_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'MADURACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_6_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-21 20:30:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Diacetilo';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_6_id, quality_param_id, '0.07 ppm', true, true, 1, '2025-03-21 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'MADURACION' AND name = 'Turbidez';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_6_id, quality_param_id, '6 EBC', true, true, 1, '2025-03-21 20:30:00'::timestamptz);
    
    -- GASIFICACION
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'GASIFICACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'GASIFICACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'GASIFICACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-03-06 08:30:00'::timestamptz,
        '2025-03-06 11:30:00'::timestamptz
    ) RETURNING id INTO phase_7_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'GASIFICACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_7_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-26 20:30:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'CO2 Volumen';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_7_id, quality_param_id, '2.5 vol CO2', true, true, 1, '2025-03-26 20:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'GASIFICACION' AND name = 'Presion Tank';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_7_id, quality_param_id, '19 psi', true, true, 1, '2025-03-26 20:30:00'::timestamptz);
    
    -- DESALCOHOLIZACION (fase crítica para Pale Sin Alcohol)
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'DESALCOHOLIZACION';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'DESALCOHOLIZACION' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'DESALCOHOLIZACION', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-03-04 20:30:00'::timestamptz,
        '2025-03-06 08:30:00'::timestamptz
    ) RETURNING id INTO phase_8_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'DESALCOHOLIZACION'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_8_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-27 00:30:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'DESALCOHOLIZACION' AND name = 'Alcohol Final';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_8_id, quality_param_id, '0.3 % ABV', true, true, 1, '2025-03-27 00:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'DESALCOHOLIZACION' AND name = 'Temp Columna';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_8_id, quality_param_id, '64 °C', true, true, 1, '2025-03-27 00:30:00'::timestamptz);
    
    -- ENVASADO
    SELECT input, output, phase_order, output_unit INTO standard_input, standard_output, phase_order_val, output_unit_val
    FROM product_phases WHERE id_product = product_id AND phase = 'ENVASADO';
    
    current_input := current_output;
    current_output := current_input * (standard_output / standard_input) * efficiency_factor;
    
    SELECT id INTO sector_id FROM sectors WHERE is_active = true AND phase = 'ENVASADO' ORDER BY id LIMIT 1;
    
    INSERT INTO production_phases (id, id_batch, id_sector, phase, phase_order, status, input, output, standard_input, standard_output, output_unit, product_waste, start_date, end_date)
    VALUES (
        nextval('production_phases_seq'),
        batch_id, sector_id, 'ENVASADO', phase_order_val, 'COMPLETADA',
        ROUND(current_input * 1000.0) / 1000.0,
        ROUND(current_output * 1000.0) / 1000.0,
        standard_input, standard_output, output_unit_val,
        ROUND((standard_output * (current_input / standard_input) - current_output) * 1000.0) / 1000.0,
        '2025-03-06 11:30:00'::timestamptz,
        '2025-03-14 14:00:00'::timestamptz
    ) RETURNING id INTO phase_9_id;
    
    FOR recipe_material_id, recipe_quantity IN 
        SELECT r.id_material, r.quantity FROM recipes r
        INNER JOIN product_phases pp ON r.id_product_phase = pp.id
        WHERE pp.id_product = product_id AND pp.phase = 'ENVASADO'
    LOOP
        INSERT INTO production_materials (id, id_material, id_production_phase, quantity, creation_date)
        VALUES (nextval('production_materials_seq'), recipe_material_id, phase_9_id,
                ROUND(recipe_quantity * multiplier * 1000000.0) / 1000000.0, '2025-03-29 00:30:00'::timestamptz);
    END LOOP;
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Sellado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_9_id, quality_param_id, 'Hermético', true, true, 1, '2025-03-29 00:30:00'::timestamptz);
    
    SELECT id INTO quality_param_id FROM quality_parameters WHERE is_active = true AND phase = 'ENVASADO' AND name = 'Etiquetado';
    INSERT INTO production_phases_qualities (id, id_production_phase, id_quality_parameter, value, is_approved, is_active, version, realization_date)
    VALUES (nextval('production_phases_qualities_seq'), phase_9_id, quality_param_id, 'OK', true, true, 1, '2025-03-29 00:30:00'::timestamptz);
    
    RAISE NOTICE 'Orden de producción Marzo-15 cargada exitosamente - Output final: % LT', ROUND(current_output * 1000.0) / 1000.0;
    
    -- Actualizar secuencias
    PERFORM setval('batches_seq', COALESCE((SELECT MAX(id) FROM batches), 1), true);
    PERFORM setval('production_orders_seq', COALESCE((SELECT MAX(id) FROM production_orders), 1), true);
    PERFORM setval('production_phases_seq', COALESCE((SELECT MAX(id) FROM production_phases), 1), true);
    PERFORM setval('production_phases_qualities_seq', COALESCE((SELECT MAX(id) FROM production_phases_qualities), 1), true);
    PERFORM setval('production_materials_seq', COALESCE((SELECT MAX(id) FROM production_materials), 1), true);
    
END $$;
