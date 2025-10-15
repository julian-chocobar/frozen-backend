package com.enigcode.frozen_backend.materials.specification;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.DTO.MaterialFilterDTO;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class MaterialSpecification {
    /**
     * Crea una especificación de filtros para la entidad Material.
     * 
     * @param filterDTO DTO con los filtros de búsqueda
     * @return Especificación de filtros
     * 
     */
    public static Specification<Material> createFilter(MaterialFilterDTO filterDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDTO.getName() != null && !filterDTO.getName().trim().isEmpty()) {
                String searchTerm = "%" + filterDTO.getName().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchTerm));
            }
            if (filterDTO.getSupplier() != null && !filterDTO.getSupplier().trim().isEmpty()) {
                String searchTerm = "%" + filterDTO.getSupplier().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("supplier")),
                        searchTerm));
            }
            if (filterDTO.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filterDTO.getType()));
            }
            if (filterDTO.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filterDTO.getIsActive()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}