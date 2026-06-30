package com.isaacabarca.devops_dashboard.repository;

import com.isaacabarca.devops_dashboard.entity.Container;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContainerRepository extends JpaRepository<Container, Long> {

    List<Container> findByServerId(Long serverId);

    boolean existsByIdAndServerId(Long id, Long serverId);
}