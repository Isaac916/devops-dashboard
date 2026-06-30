package com.isaacabarca.devops_dashboard.controller;

import com.isaacabarca.devops_dashboard.dto.request.LoginRequest;
import com.isaacabarca.devops_dashboard.dto.request.SignUpRequest;
import com.isaacabarca.devops_dashboard.dto.response.ErrorResponse;
import com.isaacabarca.devops_dashboard.dto.response.JwtResponse;
import com.isaacabarca.devops_dashboard.service.AuthService;
import com.isaacabarca.devops_dashboard.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de registro, login, verificación, refresh y logout")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/signup")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            authService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario registrado correctamente. Revisa tu email para verificar la cuenta.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.CONFLICT.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar Access Token usando Refresh Token")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            JwtResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

  @PostMapping("/logout")
@Operation(summary = "Cerrar sesión (invalida Refresh Token)")
@SecurityRequirement(name = "bearerAuth")
public ResponseEntity<?> logout(Authentication authentication) {
    try {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("No autenticado")
                            .timestamp(LocalDateTime.now())
                            .build());
        }
        authService.logout(authentication.getName());
        return ResponseEntity.ok("Sesión cerrada correctamente");
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}

    @GetMapping("/verify")
    @Operation(summary = "Verificar email mediante token")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok("Email verificado correctamente. Ya puedes iniciar sesión.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}