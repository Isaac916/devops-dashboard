package com.isaacabarca.devops_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage implements Serializable {

    private Long serverId;
    private String serverName;
    private String metric;
    private Double value;
    private Double threshold;
    private String email;
    private LocalDateTime timestamp;
}