package com.enigcode.frozen_backend.production_orders.specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderFilterDTO;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;

public class ProductionOrderSpecification {
    
    public static Specification<ProductionOrder> createFilter (ProductionOrderFilterDTO filterDTO){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDTO.getProductId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("material").get("id"), filterDTO.getProductId()));
            }
            if (filterDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filterDTO.getStatus()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}