package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String paymentUserName);
    //    @Query("select u from User u left join fetch u.roles where u.email = :email")
//    Optional<User> findByEmailWithRoles(@Param("email") String email);
    @Query("""
       select u from User u
       left join fetch u.userRoles ur
       left join fetch ur.role
       where u.email = :userEmail
       """)
    Optional<User> findByEmailWithRoles(String userEmail);
    @Query("""
           select distinct u
           from User u
           left join fetch u.userRoles ur
           left join fetch ur.role
           where u.email = :email
           and (ur is null or ur.active = true)
           """)
    Optional<User> findByEmailWithActiveRoles(String email);
    //    @EntityGraph(attributePaths = "userRoles")//to bypass lazy loading on roles field of User entity
    Optional<User> findByEmail(String userEmail);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
}
