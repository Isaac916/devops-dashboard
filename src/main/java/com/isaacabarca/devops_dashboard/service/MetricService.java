package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.MetricRequest;
import com.isaacabarca.devops_dashboard.dto.response.MetricResponse;
import com.isaacabarca.devops_dashboard.entity.Metric;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.repository.MetricRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;
    private final ServerRepository serverRepository;

    public MetricResponse save(Long serverId, MetricRequest request, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Metric metric = Metric.builder()
                .cpu(request.getCpu())
                .ram(request.getRam())
                .disk(request.getDisk())
                .networkIn(request.getNetworkIn())
                .networkOut(request.getNetworkOut())
                .server(server)
                .build();

        metric = metricRepository.save(metric);
        return mapToResponse(metric);
    }

    public List<MetricResponse> findByServer(Long serverId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        return metricRepository.findByServerIdOrderByTimestampDesc(serverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<MetricResponse> findLastHour(Long serverId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return metricRepository.findByServerIdAndTimestampAfterOrderByTimestampDesc(serverId, oneHourAgo)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MetricResponse mapToResponse(Metric metric) {
        return MetricResponse.builder()
                .id(metric.getId())
                .cpu(metric.getCpu())
                .ram(metric.getRam())
                .disk(metric.getDisk())
                .networkIn(metric.getNetworkIn())
                .networkOut(metric.getNetworkOut())
                .serverId(metric.getServer().getId())
                .serverName(metric.getServer().getName())
                .timestamp(metric.getTimestamp())
                .build();
    }
}