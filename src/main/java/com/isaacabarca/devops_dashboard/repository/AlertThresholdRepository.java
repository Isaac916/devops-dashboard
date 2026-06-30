package com.isaacabarca.devops_dashboard.repository;

import com.isaacabarca.devops_dashboard.entity.AlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Long> {

    Optional<AlertThreshold> findByServerId(Long serverId);
}