package com.jobhunt.saas.repository;

import com.jobhunt.saas.entity.EmailVerificationToken;
import com.jobhunt.saas.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(Users user);

    void deleteByExpiryTimeBefore(LocalDateTime date);

    void deleteByUser(Users user);
}
