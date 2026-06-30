package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.ServerRequest;
import com.isaacabarca.devops_dashboard.dto.response.ServerResponse;
import com.isaacabarca.devops_dashboard.entity.Metric;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.entity.User;
import com.isaacabarca.devops_dashboard.enums.ServerStatus;
import com.isaacabarca.devops_dashboard.repository.MetricRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import com.isaacabarca.devops_dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final SshService sshService;
    private final MetricRepository metricRepository;

    public ServerResponse create(ServerRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Server server = Server.builder()
                .name(request.getName())
                .ip(request.getIp())
                .operatingSystem(request.getOperatingSystem())
                .status(ServerStatus.UNKNOWN)
                .notes(request.getNotes())
                .user(user)
                .build();

        server = serverRepository.save(server);
        return mapToResponse(server);
    }

    public List<ServerResponse> findAllByUser(String userEmail) {
        return serverRepository.findByUserEmail(userEmail)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ServerResponse findById(Long id, String userEmail) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso para ver este servidor");
        }

        return mapToResponse(server);
    }

    public ServerResponse update(Long id, ServerRequest request, String userEmail) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso para editar este servidor");
        }

        server.setName(request.getName());
        server.setIp(request.getIp());
        server.setOperatingSystem(request.getOperatingSystem());
        server.setNotes(request.getNotes());

        server = serverRepository.save(server);
        return mapToResponse(server);
    }

    public void delete(Long id, String userEmail) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso para eliminar este servidor");
        }

        serverRepository.delete(server);
    }

    public ServerResponse updateStatus(Long id, ServerStatus status, String userEmail) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso para modificar este servidor");
        }

        server.setStatus(status);
        server = serverRepository.save(server);
        return mapToResponse(server);
    }

    public void checkServerStatus(Server server) {
        try {
            String result = sshService.executeCommand(server, "echo OK");
            server.setStatus(result.contains("OK") ? ServerStatus.ONLINE : ServerStatus.OFFLINE);
        } catch (Exception e) {
            server.setStatus(ServerStatus.OFFLINE);
        }
        serverRepository.save(server);
    }

    private ServerResponse mapToResponse(Server server) {
        List<Metric> metrics = metricRepository.findByServerIdOrderByTimestampDesc(server.getId());
        Metric latestMetric = metrics.isEmpty() ? null : metrics.get(0);

        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .ip(server.getIp())
                .operatingSystem(server.getOperatingSystem())
                .status(server.getStatus())
                .notes(server.getNotes())
                .cpu(latestMetric != null ? latestMetric.getCpu() : null)
                .ram(latestMetric != null ? latestMetric.getRam() : null)
                .disk(latestMetric != null ? latestMetric.getDisk() : null)
                .createdAt(server.getCreatedAt())
                .updatedAt(server.getUpdatedAt())
                .build();
    }
}