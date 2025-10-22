package com.enigcode.frozen_backend.products.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.enigcode.frozen_backend.products.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findTop10ByNameContainingIgnoreCase(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsActiveFalse(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsReadyTrue(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsReadyFalse(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsActiveTrueAndIsReadyTrue(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsActiveTrueAndIsReadyFalse(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsActiveFalseAndIsReadyTrue(String name);

    List<Product> findTop10ByNameContainingIgnoreCaseAndIsActiveFalseAndIsReadyFalse(String name);

}
