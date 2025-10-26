CREATE SEQUENCE IF NOT EXISTS MATERIALS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS RECIPES_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS PRODUCT_PHASES_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS PRODUCTS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS PACKAGINGS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS MOVEMENTS_SEQ START WITH 1 INCREMENT BY 1;

-- Quoted lowercase aliases to satisfy Hibernate when globally_quoted_identifiers=true
CREATE SEQUENCE IF NOT EXISTS "materials_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "recipes_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "product_phases_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "products_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "packagings_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "movements_seq" START WITH 1 INCREMENT BY 1;

-- Table for products as per JPA entity mapping
CREATE TABLE IF NOT EXISTS products (
	id BIGINT DEFAULT NEXT VALUE FOR PRODUCTS_SEQ PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	standard_quantity DOUBLE NOT NULL,
	unit_measurement VARCHAR(50) NOT NULL,
	is_active BOOLEAN NOT NULL,
	is_ready BOOLEAN NOT NULL,
	is_alcoholic BOOLEAN NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "products" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "products_seq" PRIMARY KEY,
	"name" VARCHAR(255) NOT NULL,
	"standard_quantity" DOUBLE NOT NULL,
	"unit_measurement" VARCHAR(50) NOT NULL,
	"is_active" BOOLEAN NOT NULL,
	"is_ready" BOOLEAN NOT NULL,
	"is_alcoholic" BOOLEAN NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Table for product_phases as per JPA entity mapping
CREATE TABLE IF NOT EXISTS product_phases (
	id BIGINT DEFAULT NEXT VALUE FOR PRODUCT_PHASES_SEQ PRIMARY KEY,
	id_product BIGINT NOT NULL,
	phase VARCHAR(255) NOT NULL,
	input DOUBLE,
	output DOUBLE,
	output_unit VARCHAR(50),
	estimated_hours DOUBLE,
	is_ready BOOLEAN NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_product_phases_product FOREIGN KEY (id_product) REFERENCES products(id)
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "product_phases" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "product_phases_seq" PRIMARY KEY,
	"id_product" BIGINT NOT NULL,
	"phase" VARCHAR(255) NOT NULL,
	"input" DOUBLE,
	"output" DOUBLE,
	"output_unit" VARCHAR(50),
	"estimated_hours" DOUBLE,
	"is_ready" BOOLEAN NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT "fk_product_phases_product_q" FOREIGN KEY ("id_product") REFERENCES "products"("id")
);

-- Table for materials as per JPA entity mapping (note: "value" is a reserved word in H2, quoted)
CREATE TABLE IF NOT EXISTS materials (
	id BIGINT DEFAULT NEXT VALUE FOR MATERIALS_SEQ PRIMARY KEY,
	code VARCHAR(255),
	name VARCHAR(255) NOT NULL,
	type INT NOT NULL,
	supplier VARCHAR(255),
	"value" DOUBLE,
	stock DOUBLE DEFAULT 0.0 NOT NULL,
	reserved_stock DOUBLE DEFAULT 0.0 NOT NULL,
	unit_measurement VARCHAR(50) NOT NULL,
	threshold DOUBLE NOT NULL,
	is_active BOOLEAN NOT NULL,
	last_update_date TIMESTAMP WITH TIME ZONE,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "materials" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "materials_seq" PRIMARY KEY,
	"code" VARCHAR(255),
	"name" VARCHAR(255) NOT NULL,
	"type" INT NOT NULL,
	"supplier" VARCHAR(255),
	"value" DOUBLE,
	"stock" DOUBLE DEFAULT 0.0 NOT NULL,
	"reserved_stock" DOUBLE DEFAULT 0.0 NOT NULL,
	"unit_measurement" VARCHAR(50) NOT NULL,
	"threshold" DOUBLE NOT NULL,
	"is_active" BOOLEAN NOT NULL,
	"last_update_date" TIMESTAMP WITH TIME ZONE,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Table for packagings as per JPA entity mapping
CREATE TABLE IF NOT EXISTS packagings (
	id BIGINT DEFAULT NEXT VALUE FOR PACKAGINGS_SEQ PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	id_material BIGINT NOT NULL,
	quantity DOUBLE NOT NULL,
	unit_measurement VARCHAR(50) NOT NULL,
	is_active BOOLEAN NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_packagings_material FOREIGN KEY (id_material) REFERENCES materials(id)
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "packagings" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "packagings_seq" PRIMARY KEY,
	"name" VARCHAR(255) NOT NULL,
	"id_material" BIGINT NOT NULL,
	"quantity" DOUBLE NOT NULL,
	"unit_measurement" VARCHAR(50) NOT NULL,
	"is_active" BOOLEAN NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT "fk_packagings_material_q" FOREIGN KEY ("id_material") REFERENCES "materials"("id")
);

-- Table for movements as per JPA entity mapping
CREATE TABLE IF NOT EXISTS movements (
	id BIGINT DEFAULT NEXT VALUE FOR MOVEMENTS_SEQ PRIMARY KEY,
	id_material BIGINT NOT NULL,
	id_usuario BIGINT,
	type VARCHAR(50) NOT NULL,
	realization_date TIMESTAMP WITH TIME ZONE NOT NULL,
	stock DOUBLE NOT NULL,
	reason VARCHAR(255),
	CONSTRAINT fk_movements_material FOREIGN KEY (id_material) REFERENCES materials(id)
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "movements" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "movements_seq" PRIMARY KEY,
	"id_material" BIGINT NOT NULL,
	"id_usuario" BIGINT,
	"type" VARCHAR(50) NOT NULL,
	"realization_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	"stock" DOUBLE NOT NULL,
	"reason" VARCHAR(255),
	CONSTRAINT "fk_movements_material_q" FOREIGN KEY ("id_material") REFERENCES "materials"("id")
);


-- Table for recipes as per JPA entity mapping
CREATE TABLE IF NOT EXISTS recipes (
	id BIGINT DEFAULT NEXT VALUE FOR RECIPES_SEQ PRIMARY KEY,
	id_product_phase BIGINT NOT NULL,
	id_material BIGINT NOT NULL,
	quantity DOUBLE NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT fk_recipes_product_phase FOREIGN KEY (id_product_phase) REFERENCES product_phases(id),
	CONSTRAINT fk_recipes_material FOREIGN KEY (id_material) REFERENCES materials(id),
	CONSTRAINT UK_recipe_phase_material UNIQUE (id_product_phase, id_material)
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "recipes" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "recipes_seq" PRIMARY KEY,
	"id_product_phase" BIGINT NOT NULL,
	"id_material" BIGINT NOT NULL,
	"quantity" DOUBLE NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT "fk_recipes_product_phase_q" FOREIGN KEY ("id_product_phase") REFERENCES "product_phases"("id"),
	CONSTRAINT "fk_recipes_material_q" FOREIGN KEY ("id_material") REFERENCES "materials"("id"),
	CONSTRAINT "UK_recipe_phase_material_q" UNIQUE ("id_product_phase", "id_material")
);

-- Sequence for batches
CREATE SEQUENCE IF NOT EXISTS BATCHES_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "batches_seq" START WITH 1 INCREMENT BY 1;

-- Sequence for production_orders
CREATE SEQUENCE IF NOT EXISTS PRODUCTION_ORDERS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "production_orders_seq" START WITH 1 INCREMENT BY 1;

-- Table for batches as per JPA entity mapping
CREATE TABLE IF NOT EXISTS batches (
	id BIGINT DEFAULT NEXT VALUE FOR BATCHES_SEQ PRIMARY KEY,
	code VARCHAR(255),
	id_packaging BIGINT NOT NULL,
	status VARCHAR(50) NOT NULL,
	quantity INTEGER NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
	planned_date TIMESTAMP WITH TIME ZONE NOT NULL,
	start_date TIMESTAMP WITH TIME ZONE,
	completed_date TIMESTAMP WITH TIME ZONE,
	estimated_completed_date TIMESTAMP WITH TIME ZONE,
	CONSTRAINT fk_batches_packaging FOREIGN KEY (id_packaging) REFERENCES packagings(id)
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "batches" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "batches_seq" PRIMARY KEY,
	"code" VARCHAR(255),
	"id_packaging" BIGINT NOT NULL,
	"status" VARCHAR(50) NOT NULL,
	"quantity" INTEGER NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	"planned_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	"start_date" TIMESTAMP WITH TIME ZONE,
	"completed_date" TIMESTAMP WITH TIME ZONE,
	"estimated_completed_date" TIMESTAMP WITH TIME ZONE,
	CONSTRAINT "fk_batches_packaging_q" FOREIGN KEY ("id_packaging") REFERENCES "packagings"("id")
);

-- Table for production_orders as per JPA entity mapping
CREATE TABLE IF NOT EXISTS production_orders (
	id BIGINT DEFAULT NEXT VALUE FOR PRODUCTION_ORDERS_SEQ PRIMARY KEY,
	id_product BIGINT NOT NULL,
	id_batch BIGINT,
	quantity DOUBLE NOT NULL,
	status VARCHAR(50) NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
	validation_date TIMESTAMP WITH TIME ZONE,
	CONSTRAINT fk_production_orders_product FOREIGN KEY (id_product) REFERENCES products(id),
	CONSTRAINT fk_production_orders_batch FOREIGN KEY (id_batch) REFERENCES batches(id)
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "production_orders" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "production_orders_seq" PRIMARY KEY,
	"id_product" BIGINT NOT NULL,
	"id_batch" BIGINT,
	"quantity" DOUBLE NOT NULL,
	"status" VARCHAR(50) NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	"validation_date" TIMESTAMP WITH TIME ZONE,
	CONSTRAINT "fk_production_orders_product_q" FOREIGN KEY ("id_product") REFERENCES "products"("id"),
	CONSTRAINT "fk_production_orders_batch_q" FOREIGN KEY ("id_batch") REFERENCES "batches"("id")
);

-- Sequence for users
CREATE SEQUENCE IF NOT EXISTS USER_SEQU START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "user_sequ" START WITH 1 INCREMENT BY 1;

-- Table for users as per JPA entity mapping
CREATE TABLE IF NOT EXISTS users (
	id BIGINT DEFAULT NEXT VALUE FOR USER_SEQU PRIMARY KEY,
	username VARCHAR(255) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	email VARCHAR(255),
	phone_number VARCHAR(50),
	role VARCHAR(50) NOT NULL,
	is_active BOOLEAN DEFAULT TRUE NOT NULL,
	enabled BOOLEAN DEFAULT TRUE NOT NULL,
	account_non_expired BOOLEAN DEFAULT TRUE NOT NULL,
	account_non_locked BOOLEAN DEFAULT TRUE NOT NULL,
	credentials_non_expired BOOLEAN DEFAULT TRUE NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
	last_login_date TIMESTAMP WITH TIME ZONE,
	last_update_date TIMESTAMP WITH TIME ZONE
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "users" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "user_sequ" PRIMARY KEY,
	"username" VARCHAR(255) NOT NULL UNIQUE,
	"password" VARCHAR(255) NOT NULL,
	"name" VARCHAR(255) NOT NULL,
	"email" VARCHAR(255),
	"phone_number" VARCHAR(50),
	"role" VARCHAR(50) NOT NULL,
	"is_active" BOOLEAN DEFAULT TRUE NOT NULL,
	"enabled" BOOLEAN DEFAULT TRUE NOT NULL,
	"account_non_expired" BOOLEAN DEFAULT TRUE NOT NULL,
	"account_non_locked" BOOLEAN DEFAULT TRUE NOT NULL,
	"credentials_non_expired" BOOLEAN DEFAULT TRUE NOT NULL,
	"creation_date" TIMESTAMP WITH TIME ZONE NOT NULL,
	"last_login_date" TIMESTAMP WITH TIME ZONE,
	"last_update_date" TIMESTAMP WITH TIME ZONE
);
