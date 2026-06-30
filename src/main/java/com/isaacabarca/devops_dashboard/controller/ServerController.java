package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.dto.request.ServerRequest;
import com.isaacabarca.devops_dashboard.dto.response.ErrorResponse;
import com.isaacabarca.devops_dashboard.dto.response.ServerResponse;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.entity.User;
import com.isaacabarca.devops_dashboard.enums.ServerStatus;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import com.isaacabarca.devops_dashboard.repository.UserRepository;
import com.isaacabarca.devops_dashboard.service.AlertService;
import com.isaacabarca.devops_dashboard.service.ServerService;
import com.isaacabarca.devops_dashboard.service.SshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
@Tag(name = "Servidores", description = "Gestión de servidores")
@SecurityRequirement(name = "bearerAuth")
public class ServerController {

    private final ServerService serverService;
    private final ServerRepository serverRepository;
    private final SshService sshService;
    private final AlertService alertService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Añadir un nuevo servidor")
    public ResponseEntity<?> create(@Valid @RequestBody ServerRequest request, Authentication authentication) {
        try {
            ServerResponse response = serverService.create(request, authentication.getName());
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
    @Operation(summary = "Listar todos mis servidores")
    public ResponseEntity<List<ServerResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(serverService.findAllByUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un servidor por ID")
    public ResponseEntity<?> findById(@PathVariable Long id, Authentication authentication) {
        try {
            return ResponseEntity.ok(serverService.findById(id, authentication.getName()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un servidor")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ServerRequest request,
                                     Authentication authentication) {
        try {
            return ResponseEntity.ok(serverService.update(id, request, authentication.getName()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un servidor")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        try {
            serverService.delete(id, authentication.getName());
            return ResponseEntity.ok("Servidor eliminado correctamente");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado del servidor (ONLINE/OFFLINE)")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam ServerStatus status,
                                           Authentication authentication) {
        try {
            return ResponseEntity.ok(serverService.updateStatus(id, status, authentication.getName()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PostMapping("/{id}/test-ssh")
    @Operation(summary = "Probar conexión SSH y obtener CPU")
    public ResponseEntity<?> testSsh(@PathVariable Long id, Authentication authentication) {
        try {
            ServerResponse serverResponse = serverService.findById(id, authentication.getName());
            Server server = serverRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

            String cpu = sshService.getCpuUsage(server);

            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "server", serverResponse.getName(),
                    "cpu", cpu,
                    "message", "Conexión SSH exitosa"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Error SSH: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PostMapping("/{id}/test-alert")
    @Operation(summary = "Probar alerta manual")
    public ResponseEntity<?> testAlert(@PathVariable Long id, Authentication authentication) {
        try {
            ServerResponse server = serverService.findById(id, authentication.getName());
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            alertService.sendAlert(
                    server.getId(),
                    server.getName(),
                    "CPU",
                    95.0,
                    80.0,
                    user.getEmail()
            );

            return ResponseEntity.ok(Map.of("message", "Alerta enviada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
    @GetMapping("/{id}/system-info")
@Operation(summary = "Obtener información del sistema por SSH")
public ResponseEntity<?> getSystemInfo(@PathVariable Long id, Authentication authentication) {
    try {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));
        
        Map<String, String> info = new HashMap<>();
        info.put("system", sshService.getSystemInfo(server));
        info.put("processes", sshService.getTopProcesses(server));
        info.put("ports", sshService.getOpenPorts(server));
        info.put("updates", sshService.getSystemUpdates(server));
        
        return ResponseEntity.ok(info);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
}