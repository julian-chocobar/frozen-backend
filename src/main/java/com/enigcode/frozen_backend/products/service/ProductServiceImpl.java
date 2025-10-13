package com.enigcode.frozen_backend.products.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
    final ProductMapper productMapper;

    @Override
    public ProductResponseDTO saveProduct(ProductCreateDTO productCreateDTO) {
        Product product = productMapper.toEntity(productCreateDTO);
        product.setCreationDate(OffsetDateTime.now(ZoneOffset.UTC));
        product.setIsActive(Boolean.TRUE);

        Product savedProduct = productRepository.save(product);

        Product finalProduct = productRepository.saveAndFlush(savedProduct);

        return productMapper.toResponseDto(finalProduct);
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
