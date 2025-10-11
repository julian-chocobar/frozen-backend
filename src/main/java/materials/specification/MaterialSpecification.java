package materials.specification;

import materials.model.Material;
import materials.DTO.MaterialFilterDTO;
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
            if (filterDTO.getName() != null) {
                predicates.add(criteriaBuilder.equal(root.get("name"), filterDTO.getName()));
            }
            if (filterDTO.getSupplier() != null) {
                predicates.add(criteriaBuilder.equal(root.get("supplier"), filterDTO.getSupplier()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}