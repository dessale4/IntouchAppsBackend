package com.intouch.IntouchApps.payments.mobile_store;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IAPRequest {
    @NotBlank
    private String platform;
    @NotBlank
    private String productId;
    @NotBlank
    private String transactionId;
    @NotBlank
    private String rcAppUserId;
    @NotBlank
    private String purchaserPublicUserName;
    @NotBlank
    private String beneficiary;//can be email or public username
    @JsonProperty("isAGift")
    private Boolean isAGift;
    private Integer noOfDaysToAccess;
//    @NotBlank
//    private String payingEmail;
//    @NotBlank
//    private String beneficiaryEmail;
}
