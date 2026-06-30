package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.config.RabbitMQConfig;
import com.isaacabarca.devops_dashboard.dto.AlertMessage;
import com.isaacabarca.devops_dashboard.entity.Alert;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.entity.User;
import com.isaacabarca.devops_dashboard.repository.AlertRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import com.isaacabarca.devops_dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final RabbitTemplate rabbitTemplate;
    private final AlertRepository alertRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    public void sendAlert(Long serverId, String serverName, String metric,
                          Double value, Double threshold, String email) {
        AlertMessage message = AlertMessage.builder()
                .serverId(serverId)
                .serverName(serverName)
                .metric(metric)
                .value(value)
                .threshold(threshold)
                .email(email)
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ALERT_QUEUE, message);
        log.info("Alerta enviada: {} en {} ({}%)", metric, serverName, value);

        // Guardar en BD
        Server server = serverRepository.findById(serverId).orElse(null);
        User user = userRepository.findByEmail(email).orElse(null);
        if (server != null && user != null) {
            Alert alert = Alert.builder()
                    .message(metric + " al " + value + "% (umbral: " + threshold + "%) en " + serverName)
                    .metric(metric)
                    .value(value)
                    .threshold(threshold)
                    .viewed(false)
                    .server(server)
                    .user(user)
                    .build();
            alertRepository.save(alert);
        }
    }

    public List<Alert> getAlertsByUser(String email) {
        return alertRepository.findByUserEmailOrderByTimestampDesc(email);
    }

    public void markAsViewed(Long alertId, String email) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        if (!alert.getUser().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permiso");
        }
        alert.setViewed(true);
        alertRepository.save(alert);
    }
}