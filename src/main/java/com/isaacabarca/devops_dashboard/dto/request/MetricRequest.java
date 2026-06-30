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
public class MetricRequest {

    @NotNull(message = "CPU es obligatorio")
    private Double cpu;

    @NotNull(message = "RAM es obligatorio")
    private Double ram;

    @NotNull(message = "Disco es obligatorio")
    private Double disk;

    private Double networkIn;
    private Double networkOut;
}