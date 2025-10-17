package com.enigcode.frozen_backend.production_orders.Repository;

import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
}
