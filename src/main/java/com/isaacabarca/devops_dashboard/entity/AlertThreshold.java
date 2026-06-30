package com.isaacabarca.devops_dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_thresholds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double cpuThreshold;

    @Column(nullable = false)
    private Double ramThreshold;

    @Column(nullable = false)
    private Double diskThreshold;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, unique = true)
    private Server server;
}