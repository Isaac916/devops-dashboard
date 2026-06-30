package com.isaacabarca.devops_dashboard.service;

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

    public String chat(String message) {
        try {
            String json = "{\"inputs\": \"" + message.replace("\"", "\\\"") + "\"}";
            
            log.info("HF Request URL: https://api-inference.huggingface.co/models/google/flan-t5-small");
            log.info("HF Request Body: {}", json);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api-inference.huggingface.co/models/google/flan-t5-small"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            log.info("HF Status Code: {}", response.statusCode());
            log.info("HF Response Body: {}", response.body());

            if (response.statusCode() == 503) {
                return "El modelo está cargando. Intenta de nuevo en unos segundos.";
            }
            if (response.statusCode() == 200) {
                return response.body();
            }
            return "Error " + response.statusCode() + ": " + response.body();
        } catch (Exception e) {
            log.error("Error HF: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}