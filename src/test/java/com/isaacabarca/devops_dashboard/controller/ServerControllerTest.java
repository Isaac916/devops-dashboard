package com.isaacabarca.devops_dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaacabarca.devops_dashboard.dto.request.LoginRequest;
import com.isaacabarca.devops_dashboard.dto.request.ServerRequest;
import com.isaacabarca.devops_dashboard.dto.request.SignUpRequest;
import com.isaacabarca.devops_dashboard.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
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
class ServerControllerTest {

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

    @Autowired
    private RateLimitService rateLimitService;

    private String userToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() throws Exception {
        rateLimitService.clearBucket("login:127.0.0.1");

        SignUpRequest user1 = SignUpRequest.builder()
                .fullName("User One")
                .email("user1@test.com")
                .password("Test12345")
                .build();
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));

        LoginRequest login1 = LoginRequest.builder()
                .email("user1@test.com")
                .password("Test12345")
                .build();
        String response1 = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login1)))
                .andReturn().getResponse().getContentAsString();
        userToken = objectMapper.readTree(response1).get("accessToken").asText();

        rateLimitService.clearBucket("login:127.0.0.1");

        SignUpRequest user2 = SignUpRequest.builder()
                .fullName("User Two")
                .email("user2@test.com")
                .password("Test12345")
                .build();
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)));

        LoginRequest login2 = LoginRequest.builder()
                .email("user2@test.com")
                .password("Test12345")
                .build();
        String response2 = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login2)))
                .andReturn().getResponse().getContentAsString();
        otherUserToken = objectMapper.readTree(response2).get("accessToken").asText();
    }

    @Test
    void createServer_ShouldReturn201() throws Exception {
        ServerRequest request = ServerRequest.builder()
                .name("Production Server")
                .ip("10.0.0.1")
                .operatingSystem("Ubuntu 22.04")
                .notes("Main server")
                .build();

        mockMvc.perform(post("/api/servers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Production Server"))
                .andExpect(jsonPath("$.ip").value("10.0.0.1"));
    }

    @Test
    void createServer_ShouldReturn403_WithoutToken() throws Exception {
        ServerRequest request = ServerRequest.builder()
                .name("Test")
                .ip("10.0.0.1")
                .build();

        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listServers_ShouldReturnOnlyUserServers() throws Exception {
        ServerRequest request = ServerRequest.builder()
                .name("User1 Server")
                .ip("10.0.0.1")
                .build();

        mockMvc.perform(post("/api/servers")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        ServerRequest request2 = ServerRequest.builder()
                .name("User2 Server")
                .ip("10.0.0.2")
                .build();

        mockMvc.perform(post("/api/servers")
                .header("Authorization", "Bearer " + otherUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/servers")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("User1 Server"));
    }

    @Test
    void getServerById_ShouldReturn403_WhenNotOwner() throws Exception {
        ServerRequest request = ServerRequest.builder()
                .name("User2 Server")
                .ip("10.0.0.2")
                .build();

        String response = mockMvc.perform(post("/api/servers")
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long serverId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/servers/" + serverId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateServer_ShouldReturn200_WhenOwner() throws Exception {
        ServerRequest create = ServerRequest.builder()
                .name("Original Name")
                .ip("10.0.0.1")
                .build();

        String response = mockMvc.perform(post("/api/servers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andReturn().getResponse().getContentAsString();

        Long serverId = objectMapper.readTree(response).get("id").asLong();

        ServerRequest update = ServerRequest.builder()
                .name("Updated Name")
                .ip("10.0.0.1")
                .build();

        mockMvc.perform(put("/api/servers/" + serverId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteServer_ShouldReturn200_WhenOwner() throws Exception {
        ServerRequest create = ServerRequest.builder()
                .name("To Delete")
                .ip("10.0.0.1")
                .build();

        String response = mockMvc.perform(post("/api/servers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andReturn().getResponse().getContentAsString();

        Long serverId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/servers/" + serverId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }
}
