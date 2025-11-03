package com.enigcode.frozen_backend.products.mapper;

import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    private Product buildProduct() {
        return Product.builder()
                .id(5L)
                .name("IPA")
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
    }

    @Test
    void partialUpdate_updates_nonNull_fields() {
        Product existing = Product.builder()
                .id(1L)
                .name("APA")
                .isAlcoholic(false)
                .build();

        ProductUpdateDTO dto = ProductUpdateDTO.builder()
                .name("IPA")
                .isAlcoholic(true)
                .build();

        Product result = mapper.partialUpdate(dto, existing);

        assertThat(result.getName()).isEqualTo("IPA");
        assertThat(result.getIsAlcoholic()).isTrue();
    }

}