package com.intouch.IntouchApps.utils;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.util.StringUtils;

public final class AppPhoneUtil {

    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    private AppPhoneUtil() {
    }

    public static String normalizeToE164OrNull(String rawPhone) {
        if (!StringUtils.hasText(rawPhone)) {
            return null;
        }

        String cleaned = rawPhone.trim();

        try {
            // null region is okay when number includes country code like +251..., +1..., etc.
            Phonenumber.PhoneNumber parsed = PHONE_UTIL.parse(cleaned, null);

            if (!PHONE_UTIL.isValidNumber(parsed)) {
                throw new IllegalArgumentException("Invalid phone number.");
            }

            return PHONE_UTIL.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid phone number format.", e);
        }
    }
}