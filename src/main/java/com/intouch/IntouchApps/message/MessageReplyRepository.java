package com.intouch.IntouchApps.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReplyRepository extends JpaRepository<MessageReply, Integer> {
}
