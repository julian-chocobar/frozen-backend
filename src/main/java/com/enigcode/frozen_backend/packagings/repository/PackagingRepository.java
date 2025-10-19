package com.enigcode.frozen_backend.packagings.repository;

import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PackagingRepository extends JpaRepository<Packaging, Long>, JpaSpecificationExecutor<Packaging> {
    List<Packaging> findTop10ByNameContainingIgnoreCase(String name);
    List<Packaging> findTop10ByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    List<Packaging> findTop10ByNameContainingIgnoreCaseAndIsActiveFalse(String name);
    List<Packaging> findTop10ByUnitMeasurementAndNameContainingIgnoreCase(UnitMeasurement unitMeasurement, String name);
    List<Packaging> findTop10ByUnitMeasurementAndNameContainingIgnoreCaseAndIsActiveTrue(UnitMeasurement unitMeasurement, String name);
    List<Packaging> findTop10ByUnitMeasurementAndNameContainingIgnoreCaseAndIsActiveFalse(UnitMeasurement unitMeasurement, String name);
}
