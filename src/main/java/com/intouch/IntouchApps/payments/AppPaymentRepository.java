package com.intouch.IntouchApps.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPaymentRepository extends JpaRepository<AppPayment, Integer> {
}
