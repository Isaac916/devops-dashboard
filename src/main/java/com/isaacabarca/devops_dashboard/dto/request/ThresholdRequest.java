package com.isaacabarca.devops_dashboard.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdRequest {

    @NotNull(message = "Umbral de CPU es obligatorio")
    private Double cpuThreshold;

    @NotNull(message = "Umbral de RAM es obligatorio")
    private Double ramThreshold;

    @NotNull(message = "Umbral de Disco es obligatorio")
    private Double diskThreshold;
}