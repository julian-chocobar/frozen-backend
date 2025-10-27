package com.enigcode.frozen_backend.system_configurations.DTO;

import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigurationResponseDTO {
    List<WorkingDay> workingDays;
    Boolean isActive;
}
