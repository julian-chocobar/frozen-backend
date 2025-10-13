package com.enigcode.frozen_backend.packagings.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enigcode.frozen_backend.packagings.model.Packaging;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {

}
