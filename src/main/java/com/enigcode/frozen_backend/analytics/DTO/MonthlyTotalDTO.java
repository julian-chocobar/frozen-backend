package com.enigcode.frozen_backend.analytics.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyTotalDTO {
    private String month;
    private Double total;
}
