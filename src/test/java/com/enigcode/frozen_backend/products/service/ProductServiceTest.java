package com.enigcode.frozen_backend.products.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.products.mapper.ProductMapper;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

        @Mock
        ProductRepository productRepository;
        @Mock
        PackagingRepository packagingRepository;
        @Mock
        ProductMapper productMapper;

        @InjectMocks
        ProductServiceImpl service;

        Packaging packaging;

        @BeforeEach
        void setUp() {
                packaging = Packaging.builder()
                                .id(1L)
                                .name("Botella")
                                .quantity(0.33)
                                .unitMeasurement(UnitMeasurement.LT)
                                .isActive(true)
                                .creationDate(OffsetDateTime.now())
                                .build();

                lenient().when(productMapper.toResponseDto(any(Product.class))).thenAnswer(inv -> {
                        Product p = inv.getArgument(0);
                        return ProductResponseDTO.builder()
                                        .id(p.getId())
                                        .name(p.getName())
                                        .isActive(p.getIsActive())
                                        .isReady(p.getIsReady())
                                        .isAlcoholic(p.getIsAlcoholic())
                                        .build();
                });
        }

        @Test
        void createProduct_isAlcoholic_createsPhases_andPersists() {
                when(packagingRepository.findById(1L)).thenReturn(Optional.of(packaging));
                when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> {
                        Product p = inv.getArgument(0);
                        p.setId(10L);
                        return p;
                });

                var dto = ProductCreateDTO.builder()
                                .name("IPA")
                                .isAlcoholic(true)
                                .build();

                ProductResponseDTO resp = service.createProduct(dto);

                verify(packagingRepository).findById(1L);
                ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).saveAndFlush(captor.capture());

                Product saved = captor.getValue();
                assertThat(saved.getName()).isEqualTo("IPA");
                assertThat(saved.getIsActive()).isTrue();
                assertThat(saved.getIsReady()).isFalse();
                assertThat(saved.getPhases()).isNotNull();
                assertThat(saved.getPhases()).noneMatch(pp -> pp.getPhase() == Phase.DESALCOHOLIZACION);
                assertThat(saved.getPhases())
                                .allMatch(pp -> Boolean.FALSE.equals(pp.getIsReady()) && pp.getProduct() == saved);

                assertThat(resp.getId()).isEqualTo(10L);
                assertThat(resp.getName()).isEqualTo("IPA");
        }

        @Test
        void createProduct_notAlcoholic_addsDesalcoholPhase() {
                when(packagingRepository.findById(1L)).thenReturn(Optional.of(packaging));
                when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> {
                        Product p = inv.getArgument(0);
                        p.setId(11L);
                        return p;
                });

                var dto = ProductCreateDTO.builder()
                                .name("IPA 0.0")
                                .isAlcoholic(false)
                                .build();

                ProductResponseDTO resp = service.createProduct(dto);

                ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).saveAndFlush(captor.capture());
                Product saved = captor.getValue();

                assertThat(saved.getIsAlcoholic()).isFalse();
                assertThat(saved.getPhases()).anyMatch(pp -> pp.getPhase() == Phase.DESALCOHOLIZACION);
                assertThat(resp.getId()).isEqualTo(11L);
        }

        @Test
        void createProduct_packagingNotFound_throws404() {
                when(packagingRepository.findById(99L)).thenReturn(Optional.empty());

                var dto = ProductCreateDTO.builder()
                                .name("X")
                                .isAlcoholic(true)
                                .build();

                assertThatThrownBy(() -> service.createProduct(dto))
                                .isInstanceOf(ResourceNotFoundException.class);
                verify(productRepository, never()).saveAndFlush(any());
        }

        @Test
        void markAsReady_whenPhasesNotReady_throwsBadRequest() {
                List<ProductPhase> phases = new ArrayList<>();
                phases.add(ProductPhase.builder()
                                .product(null)
                                .phase(Phase.MOLIENDA)
                                .isReady(false)
                                .creationDate(OffsetDateTime.now())
                                .build());

                Product product = Product.builder()
                                .id(5L)
                                .name("IPA")
                                .isActive(true)
                                .isReady(false)
                                .isAlcoholic(true)
                                .creationDate(OffsetDateTime.now())
                                .phases(phases)
                                .build();

                when(productRepository.findById(5L)).thenReturn(Optional.of(product));

                assertThatThrownBy(() -> service.markAsReady(5L))
                                .isInstanceOf(BadRequestException.class);
                verify(productRepository, never()).save(any());
        }

        @Test
        void markAsReady_whenAllPhasesReady_setsFlagAndSaves() {
                List<ProductPhase> phases = new ArrayList<>();
                phases.add(ProductPhase.builder().product(null).phase(Phase.MOLIENDA).isReady(true)
                                .creationDate(OffsetDateTime.now()).build());
                phases.add(ProductPhase.builder().product(null).phase(Phase.MACERACION).isReady(true)
                                .creationDate(OffsetDateTime.now()).build());

                Product product = Product.builder()
                                .id(6L)
                                .name("IPA")
                                .isActive(true)
                                .isReady(false)
                                .isAlcoholic(true)
                                .creationDate(OffsetDateTime.now())
                                .phases(phases)
                                .build();
                when(productRepository.findById(6L)).thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

                ProductResponseDTO resp = service.markAsReady(6L);

                ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(captor.capture());
                assertThat(captor.getValue().getIsReady()).isTrue();
                assertThat(resp.getIsReady()).isTrue();
        }

        @Test
        void updateProduct_changesNameAndAlcoholicType() {
                List<ProductPhase> phases = new ArrayList<>();
                phases.add(ProductPhase.builder().product(null).phase(Phase.MOLIENDA).isReady(false)
                                .creationDate(OffsetDateTime.now()).build());

                Product product = Product.builder()
                                .id(7L)
                                .name("Old")
                                .isActive(true)
                                .isReady(false)
                                .isAlcoholic(true)
                                .creationDate(OffsetDateTime.now())
                                .phases(phases)
                                .build();
                when(productRepository.findById(7L)).thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

                var update = ProductUpdateDTO.builder()
                                .name("New")
                                .isAlcoholic(false)
                                .build();

                ProductResponseDTO resp = service.updateProduct(7L, update);

                ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
                verify(productRepository).save(captor.capture());
                Product saved = captor.getValue();
                assertThat(saved.getName()).isEqualTo("New");
                assertThat(saved.getIsAlcoholic()).isFalse();
                assertThat(saved.getPhases()).anyMatch(pp -> pp.getPhase() == Phase.DESALCOHOLIZACION);
                assertThat(resp.getName()).isEqualTo("New");
        }

        @Test
        void toggleActive_invertsFlag() {
                Product product = Product.builder()
                                .id(9L).name("Prod").isActive(true).isReady(false)
                                .isAlcoholic(true).creationDate(OffsetDateTime.now()).phases(new ArrayList<>())
                                .build();
                when(productRepository.findById(9L)).thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

                ProductResponseDTO resp = service.toggleActive(9L);

                verify(productRepository).save(argThat(p -> p.getIsActive() == false));
                assertThat(resp.getIsActive()).isFalse();
        }

        @Test
        void getProduct_returnsMappedDto() {
                Product product = Product.builder()
                                .id(10L).name("Prod").isActive(true).isReady(false)
                                .isAlcoholic(true).creationDate(OffsetDateTime.now()).phases(new ArrayList<>())
                                .build();
                when(productRepository.findById(10L)).thenReturn(Optional.of(product));

                ProductResponseDTO dto = service.getProduct(10L);
                assertThat(dto.getId()).isEqualTo(10L);
                assertThat(dto.getName()).isEqualTo("Prod");
        }

        @Test
        void findAll_mapsPage() {
                Product product = Product.builder()
                                .id(20L).name("A").isActive(true).isReady(false).isAlcoholic(true)
                                .creationDate(OffsetDateTime.now()).phases(new ArrayList<>())
                                .build();

                Specification<Product> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

                when(productRepository.findAll(spec,
                                any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(product)));

                Page<ProductResponseDTO> page = service.findAll(
                                new com.enigcode.frozen_backend.products.DTO.ProductFilterDTO(), PageRequest.of(0, 10));
                assertThat(page.getTotalElements()).isEqualTo(1);
                assertThat(page.getContent().get(0).getId()).isEqualTo(20L);
        }
}