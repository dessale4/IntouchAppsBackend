package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.auth.AuthenticationService;
import com.intouch.IntouchApps.utils.AppDateUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final AuthenticationService authenticationService;

    public Subscription findByNameAndUsername(String subscription, String pubUsername) {
        return subscriptionRepository.findBySubscriptionProductNameAndOwnerPublicUserName(subscription, pubUsername);
    }

    public Subscription createOrUpdateSubscription(String beneficiary, String subProductName, boolean isGift, String offeredBy, Integer addedDays) {
        User existingAppUser = authenticationService.existingAppUser(beneficiary);
        Subscription subscription = findByNameAndUsername(subProductName, existingAppUser.getUserName());
        if (subscription == null) {
            subscription = Subscription.builder()
                    .ownerPublicUserName(existingAppUser.getUserName())
                    .subscriptionProductName(subProductName)
                    .expirationDate(AppDateUtil.getCurrentUTCLocalDateTime().plus(addedDays, ChronoUnit.DAYS))
                    .noteOnUpdate(isGift ? "Gift from " + offeredBy : "First Self Subscription")
                    .productPurchaseCount(1)
                    .build();
            subscriptionRepository.save(subscription);
        }else{
            Instant subExpirationDate = subscription.getExpirationDate().isAfter(AppDateUtil.getCurrentUTCLocalDateTime()) ? subscription.getExpirationDate().plus(addedDays, ChronoUnit.DAYS) : AppDateUtil.getCurrentUTCLocalDateTime().plus(addedDays, ChronoUnit.DAYS);
            subscription.setExpirationDate(subExpirationDate);
            subscription.setNoteOnUpdate(isGift ? "Gift from " + offeredBy : "Self Subscription Renewal");
            subscription.setProductPurchaseCount(subscription.getProductPurchaseCount() + 1);
            subscriptionRepository.save(subscription);
        }
    return subscription;
    }
    public Set<String> getUserActiveSubscriptions(String publicUserName){
        List<Subscription> subscriptions = subscriptionRepository.findByOwnerPublicUserName(publicUserName);
        return subscriptions.stream()
                .filter(s->s.getExpirationDate().isAfter(AppDateUtil.getCurrentUTCLocalDateTime()))
                .map(s->s.getSubscriptionProductName())
                .collect(Collectors.toSet());
    }
}
