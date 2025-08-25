package com.intouch.IntouchApps.payments.mobile_store;

import com.intouch.IntouchApps.user.SubscriptionService;
import com.intouch.IntouchApps.utils.AppDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntitlementService {
    private final UserEntitlementRepository userEntitlementRepository;
    private final SubscriptionService subscriptionService;
    public void recordTransactionAttempt(boolean isValidTransaction, String transactionInfo, IAPRequest payload, String rcTransactionStatus){
        System.out.println("isValidTransaction===>" + isValidTransaction);

//        // Check if the productId is active for this subscriber
//        Map<String, Object> entitlements = (Map<String, Object>) subscriberInfo.get("subscriber");
//        if (entitlements != null && entitlements.containsKey("entitlements")) {
//            Map<String, Object> activeEntitlements = (Map<String, Object>) ((Map<String, Object>) entitlements.get("entitlements")).get("active");
//            if (activeEntitlements.containsKey(productId)) {
//                // Grant access in DB
//                entitlementService.grantEntitlement(appUserId, productId, platform);
//                return ResponseEntity.ok("Purchase verified and entitlement granted");
//            }
//        }
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Purchase not verified");

        UserEntitlement userEntitlement = UserEntitlement.builder()
                .platform(payload.getPlatform())
                .productId(payload.getProductId())
                .rcTransactionId(payload.getTransactionId())
                .rcAppUserId(payload.getRcAppUserId())
                .purchaserPublicUserName(payload.getPurchaserPublicUserName())
                .isAGift(payload.getIsAGift())
                .beneficiary(payload.getBeneficiary())
                .expirationDate(AppDateUtil.getCurrentUTCLocalDateTime().plusDays(payload.getNoOfDaysToAccess()))
                .active(true)
                .build();
        if(isValidTransaction){
            userEntitlement.setVerifiedWithRc(true);
            userEntitlement.setStatusInRc(rcTransactionStatus);//parse transactionInfo and set
            userEntitlementRepository.save(userEntitlement);
            subscriptionService.createOrUpdateSubscription(payload.getBeneficiary(), payload.getProductId(), payload.getIsAGift(), payload.getPurchaserPublicUserName(), payload.getNoOfDaysToAccess());
        }else{
            //keep record of illegal purchase attempt
            userEntitlement.setVerifiedWithRc(false);
            userEntitlement.setStatusInRc(rcTransactionStatus);//parse transactionInfo and set
            userEntitlementRepository.save(userEntitlement);
        }
    }
//    public void grantEntitlement(String userId, String productId, String platform) {
//        UserEntitlement entitlement = new UserEntitlement();
////        entitlement.setUserId(userId);
//        entitlement.setProductId(productId);
//        entitlement.setPlatform(platform);
//        entitlement.setActive(true);
////        entitlement.setPurchaseDate(Instant.now());
//        // Optional: Set expirationDate if subscription / timed access
//        repository.save(entitlement);
//
//        System.out.println("Granted entitlement for " + userId + " product " + productId);
//    }
}
