package com.isaacabarca.devops_dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaacabarca.devops_dashboard.dto.request.LoginRequest;
import com.isaacabarca.devops_dashboard.dto.request.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signUp_ShouldReturn201() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .fullName("Test User")
                .email("test@integration.com")
                .password("Test12345")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void signUp_ShouldReturn409_WhenEmailExists() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .fullName("Test User")
                .email("duplicate@integration.com")
                .password("Test12345")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ShouldReturn200_WhenCredentialsValid() throws Exception {
        SignUpRequest signUp = SignUpRequest.builder()
                .fullName("Login Test")
                .email("login@integration.com")
                .password("Test12345")
                .build();

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUp)));

        LoginRequest login = LoginRequest.builder()
                .email("login@integration.com")
                .password("Test12345")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("login@integration.com"));
    }

    @Test
    void login_ShouldReturn401_WhenPasswordInvalid() throws Exception {
        SignUpRequest signUp = SignUpRequest.builder()
                .fullName("Bad Login")
                .email("badlogin@integration.com")
                .password("Test12345")
                .build();

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUp)));

        LoginRequest login = LoginRequest.builder()
                .email("badlogin@integration.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    void protectedEndpoint_ShouldReturn403_WithoutToken() throws Exception {
    mockMvc.perform(get("/api/user/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_ShouldReturn200_WithValidToken() throws Exception {
        SignUpRequest signUp = SignUpRequest.builder()
                .fullName("Token Test")
                .email("token@integration.com")
                .password("Test12345")
                .build();

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUp)));

        LoginRequest login = LoginRequest.builder()
                .email("token@integration.com")
                .password("Test12345")
                .build();

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("token@integration.com"));
    }
}
