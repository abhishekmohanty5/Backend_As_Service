package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByEmailAndTenant_Id(String email, Long tenantId);

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Users> findByTenantId(Long tenantId);
}
