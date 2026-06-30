package com.isaacabarca.devops_dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponse {

    private Long id;
    private Double cpu;
    private Double ram;
    private Double disk;
    private Double networkIn;
    private Double networkOut;
    private Long serverId;
    private String serverName;
    private LocalDateTime timestamp;
}