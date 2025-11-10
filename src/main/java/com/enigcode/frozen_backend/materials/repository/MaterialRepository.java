package com.enigcode.frozen_backend.materials.repository;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.WarehouseZone;

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

  List<Material> findTop10ByTypeInAndNameContainingIgnoreCase(List<MaterialType> materialTypes, String name);

  List<Material> findTop10ByTypeInAndNameContainingIgnoreCaseAndIsActiveTrue(List<MaterialType> materialTypes,
      String name);

  List<Material> findTop10ByTypeInAndNameContainingIgnoreCaseAndIsActiveFalse(List<MaterialType> materialTypes,
      String name);

  // Métodos para consultas de ubicación del almacén
  List<Material> findByWarehouseZoneAndIsActiveTrue(WarehouseZone zone);

  List<Material> findByWarehouseZone(WarehouseZone zone);

  List<Material> findByIsActiveTrueAndWarehouseZoneIsNotNull();

  List<Material> findByWarehouseZoneIsNotNull();

  Long countByWarehouseZone(WarehouseZone zone);

  boolean existsByCode(String code);

  @org.springframework.data.jpa.repository.Query("SELECT DISTINCT m.warehouseSection FROM Material m WHERE m.warehouseZone = :zone AND m.warehouseSection IS NOT NULL")
  List<String> findWarehouseSectionsByZone(@org.springframework.data.repository.query.Param("zone") WarehouseZone zone);

}