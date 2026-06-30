package com.isaacabarca.devops_dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerCredentialRequest {

    @NotBlank(message = "El usuario SSH es obligatorio")
    private String sshUser;

    @NotBlank(message = "La clave SSH es obligatoria")
    private String sshKey;

    @NotNull(message = "El puerto SSH es obligatorio")
    private Integer sshPort;
}