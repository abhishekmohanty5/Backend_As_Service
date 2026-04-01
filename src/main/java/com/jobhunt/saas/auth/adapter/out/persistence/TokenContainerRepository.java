package com.jobhunt.saas.auth.adapter.out.persistence;

import com.jobhunt.saas.auth.domain.model.TokenContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenContainerRepository extends JpaRepository<TokenContainer, Long> {

    Optional<TokenContainer> findByToken(String token);

}
