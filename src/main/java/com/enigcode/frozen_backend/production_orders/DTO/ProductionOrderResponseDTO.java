package com.enigcode.frozen_backend.production_orders.DTO;

import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderResponseDTO {
    private Long id;
    private String batchCode;
    private String packagingName;
    private String productName;
    private OrderStatus status;
    private Double quantity;
}
