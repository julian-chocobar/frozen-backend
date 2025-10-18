package com.enigcode.frozen_backend.production_orders.DTO;

import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderFilterDTO {
    private Long productId;
    private OrderStatus status;
}