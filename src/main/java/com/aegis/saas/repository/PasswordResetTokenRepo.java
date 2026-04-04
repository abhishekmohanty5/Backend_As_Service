package com.aegis.saas.repository;

import com.aegis.saas.entity.PasswordResetToken;
import com.aegis.saas.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUser(Users user);
}
