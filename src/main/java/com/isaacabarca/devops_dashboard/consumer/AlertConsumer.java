package com.isaacabarca.devops_dashboard.consumer;

import com.isaacabarca.devops_dashboard.config.RabbitMQConfig;
import com.isaacabarca.devops_dashboard.dto.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.ALERT_QUEUE)
    public void consumeAlert(AlertMessage message) {
        log.info("🚨 Alerta recibida: {} en {} = {}% (umbral: {}%)",
                message.getMetric(), message.getServerName(),
                message.getValue(), message.getThreshold());

        try {
            sendEmail(message);
        } catch (Exception e) {
            log.error("Error enviando email de alerta: {}", e.getMessage());
        }
    }

    private void sendEmail(AlertMessage message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(message.getEmail());
        email.setSubject("🚨 Alerta: " + message.getMetric() + " en " + message.getServerName());
        email.setText(String.format("""
                Alerta de monitorización - DevOps Dashboard
                
                Servidor: %s
                Métrica: %s
                Valor actual: %.1f%%
                Umbral: %.1f%%
                Fecha: %s
                
                Revisa tu dashboard para más detalles.
                """,
                message.getServerName(),
                message.getMetric(),
                message.getValue(),
                message.getThreshold(),
                message.getTimestamp()));

        mailSender.send(email);
        log.info("Email de alerta enviado a: {}", message.getEmail());
    }
}