package com.enigcode.frozen_backend.products.service;

import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.products.specification.ProductSpecification;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.mapper.ProductMapper;
import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.DTO.ProductFilterDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductUpdateDTO;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;
    final PackagingRepository packagingRepository;
    final ProductMapper productMapper;

    /**
     * Creacion de producto no listo para produccion donde se asignan fases vacias segun si es alcoholica
     * @param productCreateDTO
     * @return
     */
    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO productCreateDTO) {
        Packaging packaging = packagingRepository.findById(productCreateDTO.getPackagingStandardID())
                .orElseThrow(() -> new ResourceNotFoundException("Packaging no encontrado con ID: "
                        + productCreateDTO.getPackagingStandardID()));

        OffsetDateTime dateNow = OffsetDateTime.now();
        Product product = Product.builder()
                .name(productCreateDTO.getName())
                .packaging(packaging)
                .isActive(Boolean.TRUE)
                .isReady(Boolean.FALSE)
                .isAlcoholic(productCreateDTO.getIsAlcoholic())
                .creationDate(dateNow)
                .build();

        // Se le asigna una ProductPhase incompleto a cada producto
        List<ProductPhase> phases = product.getApplicablePhases()
                .stream()
                .map(phase -> ProductPhase.builder()
                        .product(product)
                        .phase(phase)
                        .isReady(Boolean.FALSE)
                        .creationDate(dateNow)
                        .build())
                .toList();
        product.setPhases(phases);

        Product savedProduct = productRepository.saveAndFlush(product);

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
        Product originalProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no encontrado con ID: " + id));

        Product updatedProduct = productMapper.partialUpdate(productUpdateDTO,originalProduct);
        Product savedUpdatedProduct = productRepository.save(updatedProduct);

        return productMapper.toResponseDto(savedUpdatedProduct );
    }

    @Override
    public ProductResponseDTO toggleActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no encontrado con ID: " + id));
        product.toggleActive();

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public Page<ProductResponseDTO> findAll(ProductFilterDTO filterDTO, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        Page<Product> products = productRepository.findAll(
                ProductSpecification.createFilter(filterDTO), pageRequest);
        return products.map(productMapper::toResponseDto);
    }

    @Override
    public ProductResponseDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no encontrado con ID: " + id));
        return productMapper.toResponseDto(product);
    }

}
