package com.intouch.IntouchApps.payments.mobile_store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intouch.IntouchApps.auth.AuthenticationService;
import com.intouch.IntouchApps.utils.AppDateUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@RefreshScope
@Transactional
public class RevenueCatService {
    private final UserEntitlementRepository userEntitlementRepository;
    private final RestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    @Value("${application.payment.revenueCat.secret-key}")
    private String rcApiKey;
    @Value("${application.payment.revenueCat.project_id}")
    private String projectId;
    private final EntitlementService entitlementService;
    @Value("${application.payment.revenueCat.subscriptionUrl}")
    private String rcSubUrl;
    private String rcTransactionStatus;

    public boolean checkIfUserExists(String userIdentity) {
        return authenticationService.doesUserExist(userIdentity);
    }

    public ResponseEntity<String> verifyPurchase(IAPRequest payload) throws JsonProcessingException {
//        System.out.println("===IAPRequest===");
//        System.out.println(payload);

//        String subscriberInfo = getSubscriberInfo(payload.getAppUserId());
        String transactionInfo = getPurchaseByTransactionId(payload.getTransactionId());
//        System.out.println("subscriberInfo===>");
//        System.out.println(transactionInfo);
        boolean isValidTransaction = validateTransaction(transactionInfo, payload.getTransactionId());
        entitlementService.recordTransactionAttempt(isValidTransaction, transactionInfo, payload, rcTransactionStatus);
//        System.out.println("isValidTransaction===>" + isValidTransaction);
        if (isValidTransaction) {
            return ResponseEntity.ok("Purchase verified and entitlement granted");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Purchase not verified");
        }
    }
    private boolean validateTransaction(String transactionInfo, String rcTransactionId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode transactionInfoNode = objectMapper.readTree(transactionInfo);
        // For non-subscription transactions
        JsonNode purchaseItems = transactionInfoNode.path("items");
        boolean transactionFound = false;
        for (JsonNode transaction : purchaseItems) {
            if (transaction.path("store_purchase_identifier").asText().equals(rcTransactionId)) {
                transactionFound = true;
//                System.out.println(transaction.path("status").asText());
                rcTransactionStatus = transaction.path("status").asText();
                break;
            }
        }
        return transactionFound;
    }

    //    private boolean validateTransactionInCustomerPurchases(String subscriberInfo, String transactionId) throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode customerInfoNode = objectMapper.readTree(subscriberInfo);
//        // For non-subscription transactions
//        JsonNode purchaseItems = customerInfoNode.path("items");
//        boolean transactionFound = false;
//        for (JsonNode transaction : purchaseItems) {
//            if (transaction.path("store_purchase_identifier").asText().equals(transactionId)) {
//                transactionFound = true;
//                break;
//            }
//        }
//        return transactionFound;
//    }
    private String getPurchaseByTransactionId(String transactionId) {
        String url = "https://api.revenuecat.com/v2/projects/" + projectId + "/purchases?store_purchase_identifier=" + transactionId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(rcApiKey); // v2 uses Bearer token
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
//    private String getSubscriberInfo(String appUserId) {
////        String url = rcSubUrl + appUserId;
//        String url = "https://api.revenuecat.com/v2/projects/"+projectId+"/customers/"+appUserId+"/purchases";
//        System.out.println("rcApiKey");
//        System.out.println(rcApiKey);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(rcApiKey); // v2 uses Bearer token
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//        return response.getBody();
//    }

}
