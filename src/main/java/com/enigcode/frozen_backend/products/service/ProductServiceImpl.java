package com.enigcode.frozen_backend.products.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     * Creacion de producto no listo para produccion donde se asignan fases vacias
     * segun si es alcoholica
     * 
     * @param productCreateDTO
     * @return
     */
    @Override
    @Transactional
    public ProductResponseDTO createProduct(@Valid ProductCreateDTO productCreateDTO) {
        OffsetDateTime dateNow = OffsetDateTime.now();
        Product product = Product.builder()
                .name(productCreateDTO.getName())
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

    /**
     * Función que marca como listo para produccion a un producto
     * Deben estar listas todas sus fases para que esto se valide
     * 
     * @param id
     * @return ProductResponseDTO
     */
    @Override
    @Transactional
    public ProductResponseDTO markAsReady(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró producto con id " + id));

        boolean phases_ready = product.getPhases()
                .stream()
                .allMatch(ProductPhase::getIsReady);

        if (!phases_ready)
            throw new BadRequestException("Se requieren completar las fases antes de que el producto este listo");

        product.markAsReady();

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedProduct);
    }

    /**
     * Funcion que modifica parcialmente un producto
     * 
     * @param id
     * @param productUpdateDTO
     * @return
     */
    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, @Valid ProductUpdateDTO productUpdateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no encontrado con ID: " + id));

        if (productUpdateDTO.getName() != null)
            product.setName(productUpdateDTO.getName());
        if (productUpdateDTO.getIsAlcoholic() != null)
            this.changeAlcoholicType(productUpdateDTO.getIsAlcoholic(), product);

        Product savedUpdatedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedUpdatedProduct);
    }

    /**
     * Cambia el estado de alcoholico de un producto eliminando o agregando la fase
     * de desalcoholizacion
     * 
     * @param isAlcoholic
     * @param product
     */
    private void changeAlcoholicType(Boolean isAlcoholic, Product product) {
        if (Objects.equals(isAlcoholic, product.getIsAlcoholic()))
            return;
        product.setIsAlcoholic(isAlcoholic);

        Optional<ProductPhase> desalcoholPhase = product.getPhases().stream()
                .filter(phase -> phase.getPhase() == Phase.DESALCOHOLIZACION)
                .findFirst();

        if (!isAlcoholic) {
            if (desalcoholPhase.isEmpty()) {
                ProductPhase newDesalcoholPhase = ProductPhase.builder()
                        .product(product)
                        .phase(Phase.DESALCOHOLIZACION)
                        .isReady(Boolean.FALSE)
                        .creationDate(OffsetDateTime.now())
                        .build();

                product.getPhases().add(newDesalcoholPhase);
            }
        } else {
            desalcoholPhase.ifPresent(phase -> {
                product.getPhases().remove(phase);
            });
        }
    }

    /**
     * Funcion que altera el estado activo de un producto al contrario
     * 
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ProductResponseDTO toggleActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no encontrado con ID: " + id));
        product.toggleActive();

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Transactional
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
    @Transactional
    public ProductResponseDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no encontrado con ID: " + id));
        return productMapper.toResponseDto(product);
    }

}
