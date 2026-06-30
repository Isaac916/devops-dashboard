package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.dto.request.ThresholdRequest;
import com.isaacabarca.devops_dashboard.dto.response.ErrorResponse;
import com.isaacabarca.devops_dashboard.service.ThresholdService;
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
@RequestMapping("/api/servers/{serverId}/thresholds")
@RequiredArgsConstructor
@Tag(name = "Umbrales de alerta", description = "Configuración de umbrales por servidor")
@SecurityRequirement(name = "bearerAuth")
public class ThresholdController {

    private final ThresholdService thresholdService;

    @PutMapping
    @Operation(summary = "Guardar o actualizar umbrales")
    public ResponseEntity<?> save(@PathVariable Long serverId,
                                   @Valid @RequestBody ThresholdRequest request,
                                   Authentication authentication) {
        try {
            thresholdService.saveOrUpdate(serverId, request, authentication.getName());
            return ResponseEntity.ok("Umbrales guardados correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping
    @Operation(summary = "Ver umbrales del servidor")
    public ResponseEntity<?> get(@PathVariable Long serverId, Authentication authentication) {
        try {
            return ResponseEntity.ok(thresholdService.getByServerId(serverId, authentication.getName()));
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