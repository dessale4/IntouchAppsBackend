package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByJwtRefreshToken(String token);
    List<RefreshToken> findByUserEmail(String userEmail);
    void deleteByUserEmail(String userEmail);
    void deleteByJwtRefreshToken(String token);
}
