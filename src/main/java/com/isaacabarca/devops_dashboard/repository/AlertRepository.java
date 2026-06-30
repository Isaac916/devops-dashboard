package com.isaacabarca.devops_dashboard.repository;

import com.isaacabarca.devops_dashboard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserEmailOrderByTimestampDesc(String email);
    List<Alert> findByUserEmailAndViewedFalseOrderByTimestampDesc(String email);
}