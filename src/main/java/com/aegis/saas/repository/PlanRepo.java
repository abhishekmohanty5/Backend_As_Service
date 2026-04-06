package com.aegis.saas.repository;

import com.aegis.saas.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepo extends JpaRepository<Plan,Long> {
    Optional<Plan> findById(Long id);
    Optional<Plan> findByName(String name);
    List<Plan> findByActiveTrue();
}
