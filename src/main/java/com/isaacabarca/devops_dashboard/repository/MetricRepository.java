package com.isaacabarca.devops_dashboard.repository;

import com.isaacabarca.devops_dashboard.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findByServerIdOrderByTimestampDesc(Long serverId);

    List<Metric> findByServerIdAndTimestampAfterOrderByTimestampDesc(Long serverId, LocalDateTime after);

    void deleteByTimestampBefore(LocalDateTime before);
}