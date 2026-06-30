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
public class ServerRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "La IP es obligatoria")
    @Size(max = 45, message = "La IP no puede superar los 45 caracteres")
    private String ip;

    @Size(max = 50, message = "El sistema operativo no puede superar los 50 caracteres")
    private String operatingSystem;

    @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
    private String notes;
}