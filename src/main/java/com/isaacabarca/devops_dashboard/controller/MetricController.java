package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.dto.request.MetricRequest;
import com.isaacabarca.devops_dashboard.dto.response.ErrorResponse;
import com.isaacabarca.devops_dashboard.dto.response.MetricResponse;
import com.isaacabarca.devops_dashboard.service.MetricService;
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
import java.util.List;

@RestController
@RequestMapping("/api/servers/{serverId}/metrics")
@RequiredArgsConstructor
@Tag(name = "Métricas", description = "Métricas de servidores (CPU, RAM, Disco, Red)")
@SecurityRequirement(name = "bearerAuth")
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    @Operation(summary = "Enviar métricas del servidor (agente externo)")
    public ResponseEntity<?> save(@PathVariable Long serverId,
                                   @Valid @RequestBody MetricRequest request,
                                   Authentication authentication) {
        try {
            MetricResponse response = metricService.save(serverId, request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    @Operation(summary = "Ver todas las métricas del servidor")
    public ResponseEntity<?> findAll(@PathVariable Long serverId, Authentication authentication) {
        try {
            List<MetricResponse> metrics = metricService.findByServer(serverId, authentication.getName());
            return ResponseEntity.ok(metrics);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/last-hour")
    @Operation(summary = "Ver métricas de la última hora")
    public ResponseEntity<?> findLastHour(@PathVariable Long serverId, Authentication authentication) {
        try {
            List<MetricResponse> metrics = metricService.findLastHour(serverId, authentication.getName());
            return ResponseEntity.ok(metrics);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}