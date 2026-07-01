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
            // Escape básico de comillas
            String safeMessage = message.replace("\"", "\\\"");

            String jsonBody = "{\"inputs\": \"" + safeMessage + "\"}";

            log.info("HF Request URL: {}", HF_URL);
            log.info("HF Request Body: {}", jsonBody);

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

            // Modelo cargando
            if (response.statusCode() == 503) {
                return "El modelo está cargando en HuggingFace. Inténtalo en unos segundos.";
            }

            // OK
            if (response.statusCode() == 200) {

                JsonNode root = objectMapper.readTree(response.body());

                // HF devuelve ARRAY -> [{"generated_text": "..."}]
                if (root.isArray() && root.size() > 0) {
                    JsonNode first = root.get(0);

                    if (first.has("generated_text")) {
                        return first.get("generated_text").asText();
                    }

                    return first.toString();
                }

                return root.toString();
            }

            // Error general
            return "Error HF " + response.statusCode() + ": " + response.body();

        } catch (Exception e) {
            log.error("Error HF Chat: {}", e.getMessage(), e);
            return "Error interno del chat: " + e.getMessage();
        }
    }
}
