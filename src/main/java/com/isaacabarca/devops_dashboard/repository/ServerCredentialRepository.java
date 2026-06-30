package com.isaacabarca.devops_dashboard.repository;

import com.isaacabarca.devops_dashboard.entity.ServerCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerCredentialRepository extends JpaRepository<ServerCredential, Long> {

    Optional<ServerCredential> findByServerId(Long serverId);
}