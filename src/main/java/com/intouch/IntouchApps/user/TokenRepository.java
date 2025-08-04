package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByTokenAndUserEmail(String token, String userEmail);
    Optional<Token> findByCreationReasonAndUserEmail(String token, String userEmail);
    List<Token> findByUserEmail(String email);
}
