package com.intouch.IntouchApps.utils;

import java.security.SecureRandom;

public final class RandomCodeGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String NUMERIC = "0123456789";

    // Removed confusing characters like O, 0, I, 1 if you want human-friendly codes.
    private static final String ALPHA_NUMERIC =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private RandomCodeGenerator() {
    }

    public static String numericCode(int length) {
        return generate(length, NUMERIC);
    }

    public static String alphaNumericCode(int length) {
        return generate(length, ALPHA_NUMERIC);
    }

    private static String generate(int length, String characters) {
        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be greater than zero");
        }

        StringBuilder codeBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}
