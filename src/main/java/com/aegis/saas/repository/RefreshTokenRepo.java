package com.aegis.saas.repository;

import com.aegis.saas.entity.RefreshToken;
import com.aegis.saas.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Page<RefreshToken> findByUser(Users user, Pageable pageable);

    Page<RefreshToken> findByUserAndRevokedFalse(Users user, Pageable pageable);

    List<RefreshToken> findByUserAndExpiresAtBefore(Users user, LocalDateTime dateTime);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUser(Users user);

    long countByUserAndRevokedFalse(Users user);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = ?1 AND rt.issuedAt > ?2 AND rt.revoked = false ORDER BY rt.issuedAt DESC")
    List<RefreshToken> findRecentValidTokens(Users user, LocalDateTime since);
}
