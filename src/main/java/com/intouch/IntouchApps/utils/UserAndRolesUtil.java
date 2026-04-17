package com.intouch.IntouchApps.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAndRolesUtil {
    public static Map<String, String> subscriptionMap() {
        Map<String, String> appSubscriptionMap = new HashMap<>();
        appSubscriptionMap.put("basic_access_1", "ACCESS_L1");
        appSubscriptionMap.put("adminAssigned", "ACCESS_L0");
        return appSubscriptionMap;
    }
    public static List<String> adminSetEmails = List.of("support@amail.com", "storeappreviewer@amail.com", "test@amail.com", "dev@amail.com", "appstoreappreviewer@amail.com", "playstoreappreviewer@amail.com");
    public static String adminSetVerifyingEmail = "desalemfu@gmail.com";
}