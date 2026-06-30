package com.isaacabarca.devops_dashboard.repository;

import com.isaacabarca.devops_dashboard.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {

    List<Server> findByUserEmail(String email);

    boolean existsByIdAndUserEmail(Long id, String email);

    @Query("SELECT s FROM Server s JOIN FETCH s.user")
    List<Server> findAllWithUser();
}