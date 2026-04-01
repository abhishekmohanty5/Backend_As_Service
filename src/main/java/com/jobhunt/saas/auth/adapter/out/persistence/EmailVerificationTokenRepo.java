package com.jobhunt.saas.auth.adapter.out.persistence;

import com.jobhunt.saas.auth.domain.model.EmailVerificationToken;
import com.jobhunt.saas.user.domain.model.Users;
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
