package com.enigcode.frozen_backend.materials.repository;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {

  /*
   * Busca todos los materiales con paginación y filtros.
   * 
   * @param spec Especificación de filtros
   * 
   * @param pageable Información de paginación
   * 
   * @return Página de materiales
   * 
   * Ejemplo de método (JpaSpecificationExecutor ya lo implementa):
   * Page<Material> page = materialRepository.findAll(spec, pageable);
   */

  List<Material> findTop10ByNameContainingIgnoreCase(String name);

  List<Material> findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(String name);

  List<Material> findTop10ByNameContainingIgnoreCaseAndIsActiveFalse(String name);
  
  List<Material> findTop10ByMaterialTypeInAndNameContainingIgnoreCase(List<MaterialType> materialTypes, String name);
  
  List<Material> findTop10ByMaterialTypeInAndNameContainingIgnoreCaseAndIsActiveTrue(List<MaterialType> materialTypes, String name);
  
  List<Material> findTop10ByMaterialTypeInAndNameContainingIgnoreCaseAndIsActiveFalse(List<MaterialType> materialTypes, String name);

}