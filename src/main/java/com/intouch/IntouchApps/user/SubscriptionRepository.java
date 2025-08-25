package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Subscription findBySubscriptionProductNameAndOwnerPublicUserName(String subscriptionName, String ownerPublicUserName);
    List<Subscription> findByOwnerPublicUserName(String ownerPublicUserName);
}
