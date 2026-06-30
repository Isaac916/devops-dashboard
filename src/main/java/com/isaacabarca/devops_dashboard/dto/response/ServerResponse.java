package com.isaacabarca.devops_dashboard.dto.response;

import com.isaacabarca.devops_dashboard.enums.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerResponse {

    private Long id;
    private String name;
    private String ip;
    private String operatingSystem;
    private ServerStatus status;
    private String notes;
    private Double cpu;
    private Double ram;
    private Double disk;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}