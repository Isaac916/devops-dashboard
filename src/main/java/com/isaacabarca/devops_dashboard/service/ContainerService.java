package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.ContainerRequest;
import com.isaacabarca.devops_dashboard.dto.response.ContainerResponse;
import com.isaacabarca.devops_dashboard.entity.Container;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.enums.ContainerStatus;
import com.isaacabarca.devops_dashboard.repository.ContainerRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final ServerRepository serverRepository;

    public ContainerResponse create(Long serverId, ContainerRequest request, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Container container = Container.builder()
                .containerId(request.getContainerId())
                .name(request.getName())
                .image(request.getImage())
                .status(ContainerStatus.UNKNOWN)
                .ports(request.getPorts())
                .server(server)
                .build();

        container = containerRepository.save(container);
        return mapToResponse(container);
    }

    public List<ContainerResponse> findAllByServer(Long serverId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        return containerRepository.findByServerId(serverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContainerResponse findById(Long serverId, Long containerId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Container container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));

        if (!container.getServer().getId().equals(serverId)) {
            throw new RuntimeException("El contenedor no pertenece a este servidor");
        }

        return mapToResponse(container);
    }

    public ContainerResponse updateStatus(Long serverId, Long containerId, ContainerStatus status, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Container container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));

        if (!container.getServer().getId().equals(serverId)) {
            throw new RuntimeException("El contenedor no pertenece a este servidor");
        }

        container.setStatus(status);
        container = containerRepository.save(container);
        return mapToResponse(container);
    }

    public String getLogs(Long serverId, Long containerId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Container container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));

        if (!container.getServer().getId().equals(serverId)) {
            throw new RuntimeException("El contenedor no pertenece a este servidor");
        }

        // Simulación de logs. En producción conectaría con Docker Engine API
        return """
            [2026-06-29 12:00:01] INFO  Starting container %s
            [2026-06-29 12:00:02] INFO  Container started successfully
            [2026-06-29 12:00:03] INFO  Listening on port %s
            [2026-06-29 12:01:00] INFO  Health check passed
            [2026-06-29 12:05:00] INFO  Health check passed
            """.formatted(container.getName(), container.getPorts() != null ? container.getPorts() : "8080");
    }

    public void delete(Long serverId, Long containerId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Container container = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));

        if (!container.getServer().getId().equals(serverId)) {
            throw new RuntimeException("El contenedor no pertenece a este servidor");
        }

        containerRepository.delete(container);
    }

    private ContainerResponse mapToResponse(Container container) {
        return ContainerResponse.builder()
                .id(container.getId())
                .containerId(container.getContainerId())
                .name(container.getName())
                .image(container.getImage())
                .status(container.getStatus())
                .ports(container.getPorts())
                .serverId(container.getServer().getId())
                .serverName(container.getServer().getName())
                .createdAt(container.getCreatedAt())
                .updatedAt(container.getUpdatedAt())
                .build();
    }
}