package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.dto.request.ServerCredentialRequest;
import com.isaacabarca.devops_dashboard.dto.response.ErrorResponse;
import com.isaacabarca.devops_dashboard.service.ServerCredentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/servers/{serverId}/credentials")
@RequiredArgsConstructor
@Tag(name = "Credenciales SSH", description = "Gestión de credenciales SSH por servidor")
@SecurityRequirement(name = "bearerAuth")
public class ServerCredentialController {

    private final ServerCredentialService credentialService;

    @PutMapping
    @Operation(summary = "Guardar o actualizar credenciales SSH del servidor")
    public ResponseEntity<?> save(@PathVariable Long serverId,
                                   @Valid @RequestBody ServerCredentialRequest request,
                                   Authentication authentication) {
        try {
            credentialService.saveOrUpdate(serverId, request, authentication.getName());
            return ResponseEntity.ok("Credenciales SSH guardadas correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}