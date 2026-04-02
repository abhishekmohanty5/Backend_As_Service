package com.aegis.saas.repository;

import com.aegis.saas.entity.TokenContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenContainerRepository extends JpaRepository<TokenContainer, Long> {

    Optional<TokenContainer> findByToken(String token);

}
