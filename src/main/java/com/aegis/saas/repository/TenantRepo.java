package com.aegis.saas.repository;

import com.aegis.saas.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantRepo extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByClientId(String clientId);

    /**
     * Atomically increments API call counter with a single SQL UPDATE.
     * Fixes the read-modify-write race condition.
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.apiCallCount = t.apiCallCount + 1 WHERE t.id = :id")
    void incrementApiCallCount(@Param("id") Long id);
}
