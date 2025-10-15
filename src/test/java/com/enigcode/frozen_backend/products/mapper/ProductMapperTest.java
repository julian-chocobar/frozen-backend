package com.enigcode.frozen_backend.products.mapper;

import com.enigcode.frozen_backend.materials.model.MeasurementUnit;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    private Packaging buildPackaging() {
        return Packaging.builder()
                .id(10L)
                .name("Botella")
                .quantity(0.33)
                .measurementUnit(MeasurementUnit.KG)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
    }

    private Product buildProduct() {
        return Product.builder()
                .id(5L)
                .name("IPA")
                .packaging(buildPackaging())
                .isActive(true)
                .isReady(false)
                .isAlcoholic(true)
                .creationDate(OffsetDateTime.now())
                .build();
    }

    @Test
    void toResponseDto_maps_all_expected_fields() {
        Product product = buildProduct();

        ProductResponseDTO dto = mapper.toResponseDto(product);

        assertThat(dto.getId()).isEqualTo(product.getId());
        assertThat(dto.getName()).isEqualTo(product.getName());
        assertThat(dto.getIsActive()).isEqualTo(product.getIsActive());
        assertThat(dto.getIsReady()).isEqualTo(product.getIsReady());
        assertThat(dto.getIsAlcoholic()).isEqualTo(product.getIsAlcoholic());
        assertThat(dto.getCreationDate()).isEqualTo(product.getCreationDate());

        assertThat(dto.getPackagingName()).isEqualTo(product.getPackaging().getName());
        assertThat(dto.getPackagingQuantity()).isEqualTo(product.getPackaging().getQuantity());
        assertThat(dto.getPackagingMeasurementUnit()).isEqualTo(product.getPackaging().getMeasurementUnit());
    }

    @Test
    void partialUpdate_updates_simple_fields_and_keeps_packaging() {
        Product product = buildProduct();
        Packaging originalPackaging = product.getPackaging();

        ProductUpdateDTO update = ProductUpdateDTO.builder()
                .name("IPA Especial")
                .isAlcoholic(false)
                .packagingStandardId(99L) // no mapea a Packaging en el mapper actual
                .build();

        Product result = mapper.partialUpdate(update, product);

        assertThat(result.getName()).isEqualTo("IPA Especial");
        assertThat(result.getIsAlcoholic()).isFalse();
        // El mapper no convierte packagingStandardId -> packaging
        assertThat(result.getPackaging()).isSameAs(originalPackaging);
    }
}