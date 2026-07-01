package com.isaacabarca.devops_dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class AiChatService {

    @Value("${hf.api-key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String HF_URL =
            "https://api-inference.huggingface.co/models/google/flan-t5-small";

    public String chat(String message) {

        try {
            // 1. VALIDACIÓN OBLIGATORIA
            if (message == null || message.isBlank()) {
                return "Error: mensaje vacío";
            }

            if (apiKey == null || apiKey.isBlank()) {
                log.error("HF API KEY NO CONFIGURADA");
                return "Error: API key no configurada";
            }

            // 2. SANITIZAR INPUT
            String safeMessage = message.replace("\"", "\\\"");

            String jsonBody = "{\"inputs\": \"" + safeMessage + "\"}";

            log.info("HF Request URL: {}", HF_URL);
            log.info("HF Request Body: {}", jsonBody);

            // 3. REQUEST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HF_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            log.info("HF Status Code: {}", response.statusCode());
            log.info("HF Response Body: {}", response.body());

            // 4. MANEJO DE ERRORES HF
            if (response.statusCode() == 503) {
                return "El modelo está cargando en HuggingFace. Inténtalo en unos segundos.";
            }

            if (response.statusCode() == 401) {
                return "Error HF: API key inválida o sin permisos.";
            }

            if (response.statusCode() != 200) {
                return "Error HF " + response.statusCode() + ": " + response.body();
            }

            // 5. PARSEO SEGURO
            JsonNode root = objectMapper.readTree(response.body());

            // Caso típico: ARRAY
            if (root.isArray()) {

                if (root.size() == 0) {
                    return "Respuesta vacía del modelo.";
                }

                JsonNode first = root.get(0);

                if (first.has("generated_text")) {
                    return first.get("generated_text").asText();
                }

                return first.toString();
            }

            // Caso raro: objeto directo
            if (root.has("generated_text")) {
                return root.get("generated_text").asText();
            }

            return root.toString();

        } catch (Exception e) {
            // 6. ERROR REAL (sin ocultar información)
            log.error("ERROR COMPLETO HF CHAT", e);
            return "Error interno del chat: " + e.toString();
        }
    }
}
