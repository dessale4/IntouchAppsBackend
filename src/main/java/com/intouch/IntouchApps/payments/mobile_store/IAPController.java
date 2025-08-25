package com.intouch.IntouchApps.payments.mobile_store;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/iap")
@AllArgsConstructor
public class IAPController {
    private final RevenueCatService revenueCatService;
    @PostMapping("/verify-purchase")
    public ResponseEntity<String> verifyPurchase(@Validated @RequestBody IAPRequest payload) throws JsonProcessingException {
//        String appUserId = payload.getAppUserId();
//        String productId = payload.getProductId();
//        String platform = payload.getPlatform();
//        String transactionId = payload.getTransactionId();

        return revenueCatService.verifyPurchase(payload);
//        if (appUserId == null || productId == null) {
//            return ResponseEntity.badRequest().body("Missing parameters");
//        }
        // Call RevenueCat to verify purchase

    }
    @GetMapping("/beneficiary")
    public boolean isBeneficiaryAppUser(@PathParam("userIdentity") String userIdentity){
        return revenueCatService.checkIfUserExists(userIdentity);
    }
}
