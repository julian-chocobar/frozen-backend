package com.enigcode.frozen_backend.system_configurations.repository;

import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigurationRepository
        extends JpaRepository<SystemConfiguration, Long>, JpaSpecificationExecutor<SystemConfiguration> {
    Optional<SystemConfiguration> findFirstByIsActiveTrueOrderByIdDesc();
}
