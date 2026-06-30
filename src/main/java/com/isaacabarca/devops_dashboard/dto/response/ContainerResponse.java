package com.isaacabarca.devops_dashboard.dto.response;

import com.isaacabarca.devops_dashboard.enums.ContainerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerResponse {

    private Long id;
    private String containerId;
    private String name;
    private String image;
    private ContainerStatus status;
    private String ports;
    private Long serverId;
    private String serverName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}