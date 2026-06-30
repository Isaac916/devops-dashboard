package com.isaacabarca.devops_dashboard.scheduler;

import com.isaacabarca.devops_dashboard.dto.request.MetricRequest;
import com.isaacabarca.devops_dashboard.entity.AlertThreshold;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.repository.AlertThresholdRepository;
import com.isaacabarca.devops_dashboard.repository.ServerCredentialRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import com.isaacabarca.devops_dashboard.service.AlertService;
import com.isaacabarca.devops_dashboard.service.MetricService;
import com.isaacabarca.devops_dashboard.service.ServerService;
import com.isaacabarca.devops_dashboard.service.SshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringScheduler {

    private final ServerRepository serverRepository;
    private final ServerCredentialRepository credentialRepository;
    private final AlertThresholdRepository thresholdRepository;
    private final SshService sshService;
    private final MetricService metricService;
    private final AlertService alertService;
    private final ServerService serverService;

    private static final double DEFAULT_CPU = 0.01;
    private static final double DEFAULT_RAM = 0.01;
    private static final double DEFAULT_DISK = 0.01;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void monitorServers() {
        List<Server> servers = serverRepository.findAllWithUser();

        for (Server server : servers) {
            // Verificar estado online/offline
            try {
                serverService.checkServerStatus(server);
            } catch (Exception e) {
                log.error("Error verificando estado de {}: {}", server.getName(), e.getMessage());
            }

            // Si no tiene credenciales, saltar monitorización de métricas
            if (credentialRepository.findByServerId(server.getId()).isEmpty()) {
                continue;
            }

            try {
                String cpuStr = sshService.getCpuUsage(server);
                String ramStr = sshService.getRamUsage(server);
                String diskStr = sshService.getDiskUsage(server);

                double cpu = parseDouble(cpuStr);
                double ram = parseDouble(ramStr);
                double disk = parseDouble(diskStr);

                MetricRequest metricRequest = MetricRequest.builder()
                        .cpu(cpu)
                        .ram(ram)
                        .disk(disk)
                        .build();

                String userEmail = server.getUser().getEmail();
                metricService.save(server.getId(), metricRequest, userEmail);

                Optional<AlertThreshold> thresholdOpt = thresholdRepository.findByServerId(server.getId());

                double cpuThreshold = thresholdOpt.map(AlertThreshold::getCpuThreshold).orElse(DEFAULT_CPU);
                double ramThreshold = thresholdOpt.map(AlertThreshold::getRamThreshold).orElse(DEFAULT_RAM);
                double diskThreshold = thresholdOpt.map(AlertThreshold::getDiskThreshold).orElse(DEFAULT_DISK);

                if (cpu > cpuThreshold) {
                    alertService.sendAlert(server.getId(), server.getName(), "CPU", cpu, cpuThreshold, userEmail);
                }
                log.info("DEBUG: ram={}, ramThreshold={}, ¿dispara? {}", ram, ramThreshold, ram > ramThreshold);

                if (ram > ramThreshold) {
                    alertService.sendAlert(server.getId(), server.getName(), "RAM", ram, ramThreshold, userEmail);
                }

                if (disk > diskThreshold) {
                    alertService.sendAlert(server.getId(), server.getName(), "DISCO", disk, diskThreshold, userEmail);
                }

                log.info("Métricas {}: CPU={}%, RAM={}%, Disco={}%", server.getName(), cpu, ram, disk);

            } catch (Exception e) {
                log.error("Error monitorizando {}: {}", server.getName(), e.getMessage());
            }
        }
    }

    private double parseDouble(String value) {
        try {
            String clean = value.replace(",", ".").replace("%", "").trim();
            if (clean.isEmpty() || clean.equals("N/A")) return 0.0;
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}