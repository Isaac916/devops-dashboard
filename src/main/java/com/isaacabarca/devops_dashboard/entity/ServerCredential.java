package com.isaacabarca.devops_dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "server_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sshUser;

    @Column(nullable = false, length = 5000)
    private String sshKey;

    @Column(nullable = false)
    private Integer sshPort;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, unique = true)
    private Server server;
}