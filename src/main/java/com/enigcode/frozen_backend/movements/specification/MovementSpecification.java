package com.enigcode.frozen_backend.movements.specification;

import org.springframework.data.jpa.domain.Specification;

import com.enigcode.frozen_backend.movements.DTO.MovementFilterDTO;
import com.enigcode.frozen_backend.movements.model.Movement;

public class MovementSpecification {

    public static Specification<Movement> createFilter(MovementFilterDTO filterDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (filterDTO.getMaterialId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("material").get("id"), filterDTO.getMaterialId()));
            }
            if (filterDTO.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filterDTO.getType()));
            }
            if (filterDTO.getStartDate() != null) {
                predicates
                        .add(criteriaBuilder.greaterThanOrEqualTo(root.get("realizationDate"),
                                filterDTO.getStartDate()));
            }
            if (filterDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("realizationDate"), filterDTO.getEndDate()));
            }
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

    }

}
