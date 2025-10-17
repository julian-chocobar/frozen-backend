package com.enigcode.frozen_backend.products.specification;

import java.util.ArrayList;
import java.util.List;

import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.DTO.ProductFilterDTO;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    public static Specification<Product> createFilter(ProductFilterDTO filterDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDTO.getName() != null && !filterDTO.getName().trim().isEmpty()) {
                String searchTerm = "%" + filterDTO.getName().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchTerm));
            }
            if (filterDTO.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filterDTO.getIsActive()));
            }
            if (filterDTO.getIsAlcoholic() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAlcoholic"), filterDTO.getIsAlcoholic()));
            }
            if (filterDTO.getIsReady() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isReady"), filterDTO.getIsReady()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
