CREATE SEQUENCE IF NOT EXISTS MATERIALS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS RECIPES_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS PRODUCT_PHASES_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS PRODUCTS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS PACKAGINGS_SEQ START WITH 1 INCREMENT BY 1;

-- Quoted lowercase aliases to satisfy Hibernate when globally_quoted_identifiers=true
CREATE SEQUENCE IF NOT EXISTS "materials_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "recipes_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "product_phases_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "products_seq" START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS "packagings_seq" START WITH 1 INCREMENT BY 1;

-- Table for products as per JPA entity mapping
CREATE TABLE IF NOT EXISTS products (
	id BIGINT DEFAULT NEXT VALUE FOR PRODUCTS_SEQ PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	is_active BOOLEAN NOT NULL,
	is_ready BOOLEAN NOT NULL,
	is_alcoholic BOOLEAN NOT NULL,
	creation_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Quoted version for Hibernate globally quoted identifiers
CREATE TABLE IF NOT EXISTS "products" (
	"id" BIGINT DEFAULT NEXT VALUE FOR "products_seq" PRIMARY KEY,
	"name" VARCHAR(255) NOT NULL,
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
