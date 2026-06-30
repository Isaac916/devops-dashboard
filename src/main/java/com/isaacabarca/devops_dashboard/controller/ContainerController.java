package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.dto.request.ContainerRequest;
import com.isaacabarca.devops_dashboard.dto.response.ContainerResponse;
import com.isaacabarca.devops_dashboard.dto.response.ErrorResponse;
import com.isaacabarca.devops_dashboard.enums.ContainerStatus;
import com.isaacabarca.devops_dashboard.service.ContainerService;
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
@RequestMapping("/api/servers/{serverId}/containers")
@RequiredArgsConstructor
@Tag(name = "Contenedores Docker", description = "Gestión de contenedores por servidor")
@SecurityRequirement(name = "bearerAuth")
public class ContainerController {

    private final ContainerService containerService;

    @PostMapping
    @Operation(summary = "Añadir un contenedor al servidor")
    public ResponseEntity<?> create(@PathVariable Long serverId,
                                     @Valid @RequestBody ContainerRequest request,
                                     Authentication authentication) {
        try {
            ContainerResponse response = containerService.create(serverId, request, authentication.getName());
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
    @Operation(summary = "Listar contenedores del servidor")
    public ResponseEntity<?> findAll(@PathVariable Long serverId, Authentication authentication) {
        try {
            List<ContainerResponse> containers = containerService.findAllByServer(serverId, authentication.getName());
            return ResponseEntity.ok(containers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/{containerId}")
    @Operation(summary = "Ver detalle de un contenedor")
    public ResponseEntity<?> findById(@PathVariable Long serverId,
                                       @PathVariable Long containerId,
                                       Authentication authentication) {
        try {
            return ResponseEntity.ok(containerService.findById(serverId, containerId, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PatchMapping("/{containerId}/status")
    @Operation(summary = "Cambiar estado del contenedor (RUNNING/STOPPED/RESTARTING)")
    public ResponseEntity<?> updateStatus(@PathVariable Long serverId,
                                           @PathVariable Long containerId,
                                           @RequestParam ContainerStatus status,
                                           Authentication authentication) {
        try {
            return ResponseEntity.ok(containerService.updateStatus(serverId, containerId, status, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/{containerId}/logs")
    @Operation(summary = "Ver logs del contenedor")
    public ResponseEntity<?> getLogs(@PathVariable Long serverId,
                                      @PathVariable Long containerId,
                                      Authentication authentication) {
        try {
            String logs = containerService.getLogs(serverId, containerId, authentication.getName());
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @DeleteMapping("/{containerId}")
    @Operation(summary = "Eliminar un contenedor")
    public ResponseEntity<?> delete(@PathVariable Long serverId,
                                     @PathVariable Long containerId,
                                     Authentication authentication) {
        try {
            containerService.delete(serverId, containerId, authentication.getName());
            return ResponseEntity.ok("Contenedor eliminado correctamente");
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