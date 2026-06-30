package com.isaacabarca.devops_dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verifica tu cuenta - DevOps Dashboard";
        String verificationUrl = "http://localhost:8080/auth/verify?token=" + token;
        String body = """
                Bienvenido a DevOps Dashboard.
                
                Por favor, verifica tu cuenta haciendo clic en el siguiente enlace:
                
                %s
                
                Este enlace expirará en 24 horas.
                """.formatted(verificationUrl);

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Recuperación de contraseña - DevOps Dashboard";
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;
        String body = """
                Has solicitado restablecer tu contraseña.
                
                Haz clic en el siguiente enlace para continuar:
                
                %s
                
                Este enlace expirará en 1 hora.
                
                Si no solicitaste este cambio, ignora este mensaje.
                """.formatted(resetUrl);

        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email enviado correctamente a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
        }
    }
}