package com.intouch.IntouchApps.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<AppMessage, Integer> {
public List<AppMessage> findBySendingEmailAndReceivingEmail(String sendingEmail, String receivingEmail);
    @Query("From AppMessage am where am.sendingEmail=:sendingEmail OR am.receivingEmail=:sendingEmail")
    public List<AppMessage> findUserContactUsMessages(@Param("sendingEmail") String sendingEmail);
}
