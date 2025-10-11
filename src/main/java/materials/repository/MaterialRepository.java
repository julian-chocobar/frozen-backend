package materials.repository;

import materials.model.Material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {
    
    /**
     * Busca todos los materiales con paginación y filtros.
     * 
     * @param spec   Especificación de filtros
     * @param pageable Información de paginación
     * @return Página de materiales
     * 
     * Ejemplo de método (JpaSpecificationExecutor ya lo implementa):
     * Page<Material> page = materialRepository.findAll(spec, pageable);
     */

}
