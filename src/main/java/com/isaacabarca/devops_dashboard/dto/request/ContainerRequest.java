package com.isaacabarca.devops_dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerRequest {

    @NotBlank(message = "El ID del contenedor es obligatorio")
    @Size(max = 100)
    private String containerId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String name;

    @Size(max = 150)
    private String image;

    @Size(max = 20)
    private String ports;
}