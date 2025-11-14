package com.enigcode.frozen_backend.batches.specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.enigcode.frozen_backend.batches.DTO.BatchFilterDTO;
import com.enigcode.frozen_backend.batches.model.Batch;

public class BatchSpecification {

    public static Specification<Batch> createFilter(BatchFilterDTO filterDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDTO.getProductId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("productionOrder").get("product").get("id"),
                        filterDTO.getProductId()));
            }
            if (filterDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterDTO.getStatus()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
