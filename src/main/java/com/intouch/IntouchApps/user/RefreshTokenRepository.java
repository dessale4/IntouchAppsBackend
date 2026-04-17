package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByJwtRefreshToken(String token);
    List<RefreshToken> findByUserEmail(String userEmail);
    void deleteByUserEmail(String userEmail);
    void deleteByJwtRefreshToken(String token);
    @Modifying
    @Query("delete from RefreshToken r where r.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
    @Modifying
    @Query("delete from RefreshToken r where r.user = :user")
    void deleteByUser(@Param("user") User user);
}
