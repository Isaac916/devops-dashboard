package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.ThresholdRequest;
import com.isaacabarca.devops_dashboard.entity.AlertThreshold;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.repository.AlertThresholdRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ThresholdService {

    private final AlertThresholdRepository thresholdRepository;
    private final ServerRepository serverRepository;

    public void saveOrUpdate(Long serverId, ThresholdRequest request, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        Optional<AlertThreshold> existing = thresholdRepository.findByServerId(serverId);

        AlertThreshold threshold;
        if (existing.isPresent()) {
            threshold = existing.get();
            threshold.setCpuThreshold(request.getCpuThreshold());
            threshold.setRamThreshold(request.getRamThreshold());
            threshold.setDiskThreshold(request.getDiskThreshold());
        } else {
            threshold = AlertThreshold.builder()
                    .cpuThreshold(request.getCpuThreshold())
                    .ramThreshold(request.getRamThreshold())
                    .diskThreshold(request.getDiskThreshold())
                    .server(server)
                    .build();
        }

        thresholdRepository.save(threshold);
    }

    public AlertThreshold getByServerId(Long serverId, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        return thresholdRepository.findByServerId(serverId)
                .orElse(AlertThreshold.builder()
                        .cpuThreshold(80.0)
                        .ramThreshold(90.0)
                        .diskThreshold(85.0)
                        .build());
    }
}