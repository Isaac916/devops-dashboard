package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.entity.Alert;
import com.isaacabarca.devops_dashboard.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Gestión de alertas")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Listar alertas del usuario")
    public ResponseEntity<?> getAlerts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado"));
        }
        List<Alert> alerts = alertService.getAlertsByUser(authentication.getName());
        List<Map<String, Object>> result = alerts.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("message", a.getMessage());
            map.put("metric", a.getMetric());
            map.put("value", a.getValue());
            map.put("threshold", a.getThreshold());
            map.put("viewed", a.isViewed());
            map.put("serverName", a.getServer().getName());
            map.put("timestamp", a.getTimestamp().toString());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Marcar alerta como leída")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No autenticado"));
        }
        alertService.markAsViewed(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Alerta marcada como leída"));
    }
}