package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByTokenAndUserEmail(String token, String userEmail);
    Optional<VerificationToken> findByCreationReasonAndUserEmail(String token, String userEmail);
    List<VerificationToken> findByUserEmail(String email);
}
