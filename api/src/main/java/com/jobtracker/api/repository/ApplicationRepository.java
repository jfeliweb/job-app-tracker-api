package com.jobtracker.api.repository;

import com.jobtracker.api.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);
    Optional<Application> findByIdAndUserId(Long id, Long userId);
}