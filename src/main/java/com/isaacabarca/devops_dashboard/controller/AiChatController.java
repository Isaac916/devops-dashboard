package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.service.AiChatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String response = aiChatService.chat(request.get("message"));
        return ResponseEntity.ok(Map.of("response", response));
    }
}